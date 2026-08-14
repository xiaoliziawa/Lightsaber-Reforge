# 硬编码模型 Blockbench/OBJ 导出工具

该工具会实例化当前 `LegacyModelRenderer` 与现代 `LayerDefinition` 硬编码模型，并导出：

- **Blockbench 原生 `.bbmodel`**（可编辑层级模型）
- **运行时烘焙 OBJ**（`neoforge:obj` 可直接加载的游戏模型，精确还原 `render()` 中的全部 `glScaled`/平移/旋转）

当前覆盖：

- `client/model/lightsaber` 下的 60 个 emitter、body、switch section、pommel 模型
- `ModelSithCoffin`
- `ModelSithStoneCoffin`
- `ModelSithGhost`
- 当前资源目录中的全部 Java 方块模型 JSON 与方块纹理
- 为导出的方块 JSON 自动补齐 Blockbench/Minecraft `display` 槽位，可编辑 GUI、第一/第三人称、地面和展示框变换

## 修复记录（26.1.2 适配）

- 修复 `PartPose` 字段在 26.1.2 中为 record(private) 导致的编译失败，改用访问器 `x()`/`yRot()` 等
- **新增运行时烘焙 OBJ 导出**：直接调用 `LegacyModelBase.render()` 捕获最终顶点，`render()` 方法中的 `glScaled` 非等比缩放、offset 平移、镜像全部精确还原（旧导出器只读构造函数数据，完全忽略 `glScaled`，导致导出结果比例错乱）
- 导出顶点以模型单位（×16）、Y 轴向上输出，与 `models/item/spear` 的 OBJ 约定一致
- `.bbmodel` 导出现在解析 `render()` 方法源码，把 `glScaled` 写入 group 的 `scale` 字段（52/60 个模型包含非等比缩放）
- 复合方块模型导出对缺失文件容错跳过，不再中断整个导出

## 使用方式

导出全部硬编码光剑模型（Blockbench + OBJ）：

```powershell
.\gradlew.bat --no-daemon exportBlockbenchModels
```

只导出 OBJ（游戏可直接加载的 `neoforge:obj` 模型）：

```powershell
.\gradlew.bat --no-daemon exportObjModels
```

只导出一个模型：

```powershell
.\gradlew.bat --no-daemon exportBlockbenchModels -Pmodel=ModelBodyGraflex
```

一次导出多个模型：

```powershell
.\gradlew.bat --no-daemon exportBlockbenchModels `
  '-Pmodel=ModelEmitterGraflex,ModelBodyGraflex,ModelPommelGraflex'
```

指定输出目录：

```powershell
.\gradlew.bat --no-daemon exportBlockbenchModels `
  -PmodelOutput=F:\Blockbench\AdvancedLightsabers
```

默认输出目录为：

```text
model_exports/blockbench/
```

其中：

```text
lightsaber/        60 个光剑零件 .bbmodel
obj/lightsaber/    60 个光剑零件 .obj + .mtl + .json（neoforge:obj 可直接加载）
tile/              Sith Coffin 与 Stone Sith Coffin .bbmodel
block_json/        展示架、锻造台、拆解台等原始方块 JSON（用于核对游戏资源）
block/             合并后的完整方块 Blockbench 工程，包含父模型展开、贴图和 Display 变换
```

OBJ 模型使用方式：把 `obj/lightsaber/` 下的文件复制到
`assets/lightsabers/models/lightsaber/`（与 JSON 中 `lightsabers:models/lightsaber/` 路径对应），
即可被 `neoforge:obj` loader 加载；或复制到任意 `assets/lightsabers/models/` 子目录后修改 JSON 中的 `model` 路径。

棺材等实体模型会在原有 `modded_entity` 动画格式中保留 `animations` 与 `animation_controllers`，并附带同一套 `display` 变换字段，因此可以在同一个工程里继续编辑动画，同时记录手持、GUI、掉落物和展示框姿态。

每个模型会生成两个文件：

```text
ModelBodyGraflex.bbmodel
ModelBodyGraflex.mapping.json
```

## Blockbench 编辑约定

- 直接打开 `.bbmodel`，不要转换成其他项目格式。
- 可以修改 group 原点、旋转、缩放以及 cube 的位置、尺寸和 inflation。
- 尽量不要重命名 group，也不要让 Blockbench 重新生成 UUID。
- 不要删除同名 `.mapping.json`；反向修改 Java 时需要同时提供这两个文件。
- Vaid 模型会载入 ancient 与 modern 两张纹理，默认显示第一张，可在纹理列表切换。
- 坐标系保持旧 `ModelRenderer` 原始坐标，`modded_entity_flip_y` 为 `false`。
- 棺材等现代模型会把 Minecraft 向下的 Y 轴转换成 Blockbench 向上的 Y 轴，并同步反转 X/Z 旋转；mapping 仍保存 `LayerDefinition` 原始 `PartPose`，标记为 `source_kind: layer_definition`。

Blockbench 保存后，将修改后的 `.bbmodel` 和原始 `.mapping.json` 一起交回，即可按 UUID 和 Java 字段将改动写回对应的 `setRotationPoint`、`setRotateAngle` 和 `addBox`。

## 输出文件说明

`.mapping.json` 保存：

- 原 Java 类名
- 每个 `LegacyModelRenderer` 字段名和父字段
- group 与 cube 的稳定 UUID
- 原始 rotation point、offset 和弧度旋转
- 原始 `addBox` 坐标、尺寸、UV、inflation 与 mirror

这份映射不会参与游戏运行，只用于把 Blockbench 修改精确还原到硬编码模型。

## 已经是 JSON 的方块模型

光剑展示架、Holocron 和水晶仍会复制为 Minecraft Java 方块模型 JSON；拆解台和锻造台另外会生成合并后的 `.bbmodel`，不会再把多方块结构拆成互不关联的工程。

`block/DisassemblyStation.bbmodel`、`block/LightsaberForgeLight.bbmodel` 和 `block/LightsaberForgeDark.bbmodel` 可以直接用 Blockbench 的 Java Block/Item 格式打开。它们会把 `parent` 继承的元素展开，按游戏中的相邻方块位置合并，并在工程根部写入 `display`，可调整 GUI、第一/第三人称、地面和展示框姿态。Minecraft 1.21.1 原版贴图从 NeoForge 的客户端资源包自动提取后内嵌，模组贴图则直接使用项目资源。也可以通过 `-PminecraftResourcesJar=...` 指定资源包路径。
导出任务会把它们复制到：

```text
model_exports/blockbench/block_json/models/block/
```

其中 `lightsaber_stand.json` 就是展示架/石碑。棺材目录下的普通方块 JSON 只是粒子占位，真正外观在 `tile/*.bbmodel`。
