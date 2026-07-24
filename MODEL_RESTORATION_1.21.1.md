# Advanced Lightsabers 1.21.1 NeoForge 模型完整还原方案

## 一、目标

本方案的目标不是继续使用近似方盒模型，而是把 1.7.10 的硬编码模型、运行时矩阵、UV、发光层和动画
迁移到 Minecraft 1.21.1 NeoForge，并建立可重复执行的转换工具。

“完整还原”包含：

- 顶点位置、法线、UV 和面朝向一致
- `ModelRenderer` 父子层级、旋转点、镜像和局部缩放一致
- `GL11.glTranslate/Rotate/Scale` 的调用顺序一致
- 普通纹理、透明层、加法混合和全亮层分离正确
- 方块、物品、手持、GUI、实体和 BlockEntity 使用同一份网格数据
- 动态部件继续由游戏数据驱动，而不是烘焙成固定姿态
- 资源重载后模型缓存可安全重建

普通 Minecraft `elements` JSON 只用于简单方盒。复杂模型的目标格式为 NeoForge OBJ、自定义 baked model、
现代 `ModelPart` 或直接 `VertexConsumer`，不会为了“必须是 elements JSON”而牺牲精度。

## 二、模型分类与目标技术

| 旧模型类型 | 典型对象 | 1.21.1 NeoForge 目标 |
|---|---|---|
| 静态 `ModelRenderer` 方盒层级 | 锻造台、展示架静态本体 | OBJ + `neoforge:obj` model JSON |
| 有活动部件的 `ModelRenderer` | Sith Coffin、Sith Stone Coffin | `LayerDefinition` / `ModelPart` + BER |
| 程序化三角形/四边形 | Holocron、光剑刃、闪电 | `PoseStack` + `VertexConsumer` |
| 大量可组合零件 | 60+ 光剑剑柄部件 | 转换后的共享 mesh 库 + 自定义物品 renderer |
| 生物模型 | Sith Ghost | `EntityModel` + `ModelLayerLocation` |
| 发光覆盖层 | 拆解台灯光、光剑刃 | 独立 `RenderType` 与 full-bright pass |
| GUI 内 3D 预览 | 锻造台、原力界面 | 与世界/物品共用 renderer，不复制模型逻辑 |

NeoForge 1.21.1 已确认提供 `neoforge:obj` loader，支持：

```json
{
  "loader": "neoforge:obj",
  "model": "lightsabers:models/block/example.obj",
  "automatic_culling": false,
  "shade_quads": true,
  "flip_v": true,
  "emissive_ambient": true
}
```

对应实现位于本地 1.21.1 NeoForge 源码的：

- `net.neoforged.neoforge.client.model.obj.ObjLoader`
- `net.neoforged.neoforge.client.model.obj.ObjModel`
- `net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder`

## 三、转换工具架构

计划新增独立工具目录：

```text
tools/model_migrator/
├─ stubs/
│  ├─ net/minecraft/client/model/ModelBase.java
│  ├─ net/minecraft/client/model/ModelRenderer.java
│  └─ org/lwjgl/opengl/GL11.java
├─ recorder/
│  ├─ MatrixRecorder.java
│  ├─ ModelRendererRecorder.java
│  ├─ MeshBuilder.java
│  └─ VanillaBoxUv.java
├─ exporter/
│  ├─ ObjExporter.java
│  ├─ ModernModelPartExporter.java
│  └─ ManifestExporter.java
└─ fixtures/
```

### 1. 记录式 stub，而不是正则猜源码

被选中的旧模型类在隔离工具模块中编译。工具提供同包名的 1.7.10 API stub：

- `ModelBase`
- `ModelRenderer`
- `ModelBox`
- `TexturedQuad`
- `GL11`

旧构造函数和 `render()` 会真实执行，但不会调用 OpenGL。stub 会记录：

- `setTextureOffset()`
- `addBox()`
- `setRotationPoint()`
- `rotateAngleX/Y/Z`
- `mirror`
- `addChild()`
- `glPushMatrix/glPopMatrix`
- `glTranslate/glRotate/glScale`
- 每次部件 `render(scale)` 时的完整世界矩阵

这样可以保留 Java 条件分支和实际矩阵顺序，避免用正则表达式推断复杂父子变换。

### 2. 中间场景格式

所有模型先输出内部场景文件：

```text
build/model-migrator/scenes/<model>.almodel.json
```

每个节点保存：

- 稳定名称
- 父节点 ID
- pivot
- local matrix
- boxes/triangles
- texture width/height
- UV
- material/render pass
- 动态参数名称

静态和动态导出器都只读取该中间格式，避免每种目标格式重复解析旧 Java。

### 3. 坐标与矩阵

- `ModelRenderer` 的模型单位按原 `render(0.0625F)` 转换为方块单位
- 不重新排列 Euler 角；按记录到的实际矩阵顺序计算
- 父子矩阵使用 `parentWorld * local`
- 非等比缩放后的法线使用逆转置矩阵重新计算
- 负缩放会检测 winding，并在需要时反转三角形顶点顺序
- OBJ 使用右手坐标；导出前只进行一次明确的 Minecraft 坐标系转换

### 4. UV

`ModelRenderer.addBox()` 的 UV 展开必须依据 1.7.10 `ModelBox/TexturedQuad` 源码实现，不能按印象重写。

转换结果：

- 像素 UV 除以原 `textureWidth/textureHeight` 得到 OBJ 的 `0-1` UV
- 是否翻转 V 统一由导出 manifest 和 `neoforge:obj` 的 `flip_v` 控制
- `mirror=true` 按旧 `ModelBox` 的面顺序处理
- 每个材质保留原纹理路径，不重新打图集，除非后续明确做 atlas 优化

## 四、不同模型的具体迁移方式

### 静态方块模型

适用：旧锻造台、展示架静态本体、拆解台静态本体。

输出：

```text
assets/lightsabers/models/generated/block/*.obj
assets/lightsabers/models/generated/block/*.mtl
assets/lightsabers/models/block/*.json
```

静态矩阵全部烘焙进 OBJ 顶点。BlockState 只负责方向和 variant，不再运行 BER。

### Coffin 系列

棺材底座可输出 OBJ；盖板、内部角色和开启动画保留节点层级，输出现代 `ModelPart`。

- 方块本体：baked model
- 活动盖板：BER
- 开启角度：BlockEntity 插值字段
- 物品：`IClientItemExtensions#getCustomRenderer()` 返回 `BlockEntityWithoutLevelRenderer`

### Holocron

Holocron 的旧实现直接生成三角形和四边形，不经过 `ModelRenderer`。

迁移方式：

- 将 Jedi 六面体、角片和 Sith 金字塔拆成不可变顶点数组
- 初始化时只构建一次局部顶点和 UV
- 每帧仅通过 `PoseStack` 应用 `openTimer/openTicks` 矩阵
- 使用 `VertexConsumer` 提交顶点
- 普通纹理和发光层分 pass
- 禁止每帧创建数组、列表或 `ItemStack`

当前 `RenderHolocron` 是兼容预览，不是最终 1:1 renderer。最终实现应替换为上述缓存网格。

### 光剑剑柄

现有剑柄由 emitter、switch section、body、pommel 四类部件和多个 Hilt 组合而成。

转换后每个部件生成一个稳定资源 ID：

```text
lightsabers:hilt/<hilt>/<part>
```

运行时 renderer：

1. 从 `LightsaberData` 读取四个部件 ID
2. 从模型缓存取得四份不可变 mesh
3. 按旧部件高度和连接点拼接矩阵
4. 绘制剑柄普通 pass
5. 绘制光刃透明/发光 pass

单刃、双刃、投掷实体、展示架、锻造 GUI 和玩家手持必须调用同一 `LightsaberRenderer`，禁止各自实现一套。

### 光剑刃与透明效果

- 核心使用 full-bright additive pass
- 外层光晕使用透明 pass
- 长度和颜色从 `LightsaberData` 获取
- 批量写入 `VertexConsumer`，不使用即时 GL 状态切换
- RenderType 缓存为静态常量或按纹理缓存

## 五、运行时公共结构

建议新增：

```text
client/render/model/
├─ LegacyMesh.java
├─ LegacyMeshPart.java
├─ LegacyMeshLoader.java
├─ LegacyRenderTypes.java
└─ ModelTransformUtil.java

client/render/lightsaber/
├─ LightsaberRenderer.java
├─ HiltMeshRegistry.java
└─ LightsaberItemRenderer.java
```

要求：

- mesh、材质和 RenderType 在资源重载时缓存
- 每帧只计算必要矩阵，不解析 JSON、不反射、不拼接资源字符串
- GUI、BER、实体和物品共享渲染入口
- 客户端类只从 `ClientProxy` 或客户端扩展加载

## 六、验证方式

### 几何验证

- 顶点数、三角形数、包围盒和 pivot 自动对比
- 每个节点输出 local/world matrix 快照
- UV 超出范围、退化三角形和反向法线视为失败

### 图像验证

每个模型建立固定相机测试：

- front/back/left/right/top/bottom
- inventory、ground、third person、first person
- 动画进度 `0/0.25/0.5/0.75/1`

使用旧版截图作为 golden image。新版截图执行像素差异：

- 静态不透明模型目标误差低于 `1%`
- 半透明/发光模型单独比较 alpha 和亮度层
- 超出阈值时输出差异热图

### 游戏逻辑验证

- 物品与世界模型使用相同 variant
- 朝向旋转与碰撞箱一致
- 资源包重载后模型不丢失
- 专用服务端不会加载 renderer 类

## 七、当前实施状态

已完成第一阶段运行时兼容层：

- 新增 `LegacyModelBase`，保留旧模型统一 `render(Entity, ..., scale)` 入口，并向现代
  `PoseStack`、`VertexConsumer`、packed light 和 overlay 转接
- 新增 `LegacyModelRenderer`，支持本模组 60 个光剑部件模型实际使用的：
  - `addBox(x, y, z, width, height, depth, inflation)`
  - `setRotationPoint()`
  - `rotateAngleX/Y/Z`
  - `mirror`
  - `addChild()`
  - `isHidden` / `showModel`
- 旧 `GL11.glPushMatrix/glPopMatrix/glTranslatef/glScaled/glRotatef` 已替换为
  `LegacyGlState`，矩阵操作直接作用于当前 `PoseStack`
- 60 个 emitter、switch section、body、pommel Java 模型已整体切换到兼容层，目标编译错误为 0
- `HiltModelRenderer` 已成为四类部件的共享组合入口，继续使用旧高度、pommel 矩阵指令和每部件纹理
- 单刃、双刃和四种部件物品已接入 `IClientItemExtensions` +
  `BlockEntityWithoutLevelRenderer`，并使用 `builtin/entity` item model
- 旧 `IItemRenderer` 光剑、双刃光剑和部件 renderer 已删除；投掷实体使用同一物品渲染入口
- 新增无纹理 `POSITION_COLOR` 光刃 RenderType：核心 pass 写入深度，光晕 pass 使用加法混合且不写深度
- 主光刃和 crossguard 已统一使用 `LightsaberBladeRenderer`，顶点直接批量写入 `VertexConsumer`
- `COMPRESSED`、`FINE_CUT`、`CRACKED`、`INVERTING`、`PRISMATIC` 和 `jeb_` 颜色/平滑度逻辑已接入
- 光刃继续读取 `renderGlobalMultiplier`、width、opacity 和 smoothing 配置，并限制最大层数，避免异常配置造成无界顶点量
- 旧 `ModelLightsaberBlade` 和即时 `Tessellator`/GL 状态光刃实现已删除

兼容层会缓存静态部件旋转四元数，模型构造时记录不可变 box/UV 数据；每帧不会重新解析模型源码、
反射或重新构建几何。

当前限制：

- 此阶段保持硬编码模型可运行和可验证，尚未输出 `.almodel.json`/OBJ
- 新光刃几何和特殊水晶 pass 尚未完成旧版 golden image 像素差校准；当前重点是保持算法分支和安全的现代批处理
- 旧版中已注释停用的 charged lightning blade pass 未恢复
- GUI、第一人称、第三人称、地面和固定展示已提供现代兼容变换，但尚未完成旧版 golden image 像素差验证
- 因此当前只能标记为“剑柄兼容层与现代光刃运行链完成”，不能标记为完整 `1:1 verified`
- 同屏大量光剑时无每帧模型解析和明显对象分配

## 七、实施顺序

1. 建立 `model_migrator` stub 和中间场景格式
2. 用已删除的 `ModelLightsaberStand` 作为首个静态样本
3. 转换 `ModelLightsaberForge` 和 `ModelDisassemblyStation`
4. 转换 Sith Coffin 两套层级模型并建立现代 BER
5. 手工迁移 Holocron 程序化网格，建立透明/全亮 RenderType
6. 批量转换全部光剑剑柄部件
7. 建立统一 `LightsaberRenderer` 和 BEWLR
8. 迁移 Sith Ghost 与剩余实体模型
9. 建立截图回归和像素差异检查
10. 删除所有剩余 `GL11`、旧 Tessellator 和 `IItemRenderer`

## 八、完成标准

模型迁移只有同时满足以下条件才标记完成：

- 不再引用旧 `ModelBase/ModelRenderer/GL11/IItemRenderer`
- 与旧模型的几何、UV、层级和动画测试通过
- 世界、物品、GUI 和实体视图一致
- 客户端资源重载正常
- 专用服务端启动不加载客户端类
- `compileJava` 和资源验证通过
- 本文档对应模型的状态更新为“1:1 verified”，而不是“兼容预览”
