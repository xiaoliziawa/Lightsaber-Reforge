# Advanced Lightsabers：Forge 1.20.1 迁移笔记

本文档用于持续记录 Advanced Lightsabers 从 Minecraft Forge 1.7.10 迁移到 Forge 1.20.1 时确认过的 API 变化、源码位置、替代方案和当前进度。

## 一、源码查阅优先级

遇到不熟悉、已删除或签名不确定的 API 时，必须按以下顺序查阅，不得凭猜测编写：

1. Minecraft 1.20.1 Parchment 源码：
   `F:\projects\1.20.1-parchment-source`
2. CodeChickenLib 1.20.1 源码：
   `F:\CloneProjects\CodeChickenLib`
3. AtomicStryker Dynamic Lights 1.20.1 源码：
   `F:\CloneProjects\atomicstrykers-minecraft-mods\DynamicLights`
4. 只有前三处确实找不到 Forge 或第三方实现时，才进入：
   `D:\GradleHome`

## 二、当前构建环境

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.21 |
| Java | 17 |
| Gradle | 8.8 |
| Parchment | 2023.09.03-1.20.1 |
| JEI | 15.20.0.130 |
| CodeChickenLib | 4.4.0.516 |
| Dynamic Lights | 1.20.1.2 |

依赖配置状态：

- JEI API：`compileOnly`
- JEI Forge 实现：`runtimeOnly`
- CodeChickenLib：`implementation`
- Dynamic Lights：`implementation`，模组元数据中为可选依赖
- Battlegear 2：没有 1.20.1 版本，不再接入

## 三、模组加载与生命周期

| 1.7.10 API | 1.20.1 对应方式 | 状态 |
|---|---|---|
| `cpw.mods.fml.common.Mod` | `net.minecraftforge.fml.common.Mod` | 已迁移 |
| `FMLPreInitializationEvent` | `@Mod` 构造函数、注册表和模组事件总线 | 已迁移入口 |
| `FMLInitializationEvent` | `FMLCommonSetupEvent`、客户端注册事件等 | 待按模块接入 |
| `FMLPostInitializationEvent` | 通常不再需要；按具体功能使用 setup/注册事件 | 待拆分 |
| `FMLServerStartingEvent` | `ServerStartingEvent` | 待迁移命令 |
| `Loader.isModLoaded(id)` | `ModList.get().isLoaded(id)` | 已确认 |
| `@SidedProxy` | `DistExecutor.safeRunForDist` 或客户端事件订阅 | 已迁移基础代理 |
| `Side` | 物理侧使用 `Dist`；网络方向使用 `NetworkDirection` | 按模块迁移 |
| `@SideOnly(Side.CLIENT)` | `@OnlyIn(Dist.CLIENT)`，优先通过客户端包和事件隔离 | 待批量迁移 |

Forge 47.4.21 已支持向模组构造函数注入上下文：

```java
public Lightsabers(
        FMLJavaModLoadingContext javaModLoadingContext,
        ModLoadingContext modLoadingContext
) {
    IEventBus modEventBus = javaModLoadingContext.getModEventBus();
    modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ModConfig.SPEC);
}
```

这替代了已标记为待删除的：

```java
FMLJavaModLoadingContext.get();
ModLoadingContext.get();
```

## 四、配置系统

| 1.7.10 API | 1.20.1 对应方式 | 状态 |
|---|---|---|
| `net.minecraftforge.common.config.Configuration` | `ForgeConfigSpec` | 已迁移 |
| `ConfigElement`、旧 FML 配置 GUI | 没有直接等价物；使用配置模组集成或自定义 Screen | 待处理 GUI |
| 配置变化事件 | `ModConfigEvent.Loading` / `ModConfigEvent.Reloading` | 已迁移 |

当前保留的配置逻辑：

- Dynamic Lights 支持开关
- Dynamic Lights 更新间隔
- 光剑全局、宽度、透明度、平滑和亮度倍率
- Force Power shader 开关

渲染热路径继续读取缓存后的 primitive 字段，避免每帧访问配置对象。

## 五、注册系统

### 游戏注册表

| 1.7.10 API | 1.20.1 对应方式 |
|---|---|
| `GameRegistry.registerItem` | `DeferredRegister<Item>` / `RegistryObject<Item>` |
| `GameRegistry.registerBlock` | `DeferredRegister<Block>` / `RegistryObject<Block>` |
| `GameRegistry.registerTileEntity` | `DeferredRegister<BlockEntityType<?>>` |
| `EntityRegistry.registerModEntity` | `DeferredRegister<EntityType<?>>` |
| `RenderingRegistry.registerEntityRenderingHandler` | `EntityRenderersEvent.RegisterRenderers` |
| `ClientRegistry.bindTileEntitySpecialRenderer` | `EntityRenderersEvent.RegisterRenderers` |
| `MinecraftForgeClient.registerItemRenderer` | `BlockEntityWithoutLevelRenderer`、模型扩展或客户端渲染事件 |

当前已建立第一批现代游戏注册表：

- `DeferredRegister<Item>`：单刃光剑、双刃光剑、电路、聚焦水晶、四类剑柄零件
- `DeferredRegister<EntityType<?>>`：投掷光剑实体
- `BuildCreativeModeTabContentsEvent`：将核心物品和各数据变体加入原版分类页
- `EntityRenderersEvent.RegisterRenderers`：为投掷光剑注册临时物品实体渲染器

方块、方块实体、其他物品和 Sith Ghost 实体注册仍未完成。

### 光剑物品 API

| 1.7.10 | 1.20.1 |
|---|---|
| `ItemSword(ToolMaterial)` | `SwordItem(Tier, damage, speed, Properties)` |
| `onItemRightClick` | `use(Level, Player, InteractionHand)` |
| `onEntitySwing(EntityLivingBase, ItemStack)` | `IForgeItem.onEntitySwing(ItemStack, LivingEntity)` |
| `onLeftClickEntity` 旧实体类型 | `onLeftClickEntity(ItemStack, Player, Entity)` |
| `hasTagCompound/getTagCompound` | `hasTag/getTag/getOrCreateTag` |
| 物品 metadata 子类型 | 自定义 NBT、模型 predicate 或独立注册项 |
| `addInformation` | `appendHoverText`，使用 `Component` |

`ItemLightsaberBase` 继续保持无耐久、8 点基础攻击、激活标签和主手点火逻辑。Battlegear 接口已经从单刃/双刃物品删除，后续只使用原版主手/副手。

### 投掷光剑实体

旧 `EntityThrowable` 和 `IEntityAdditionalSpawnData` 已替换为 `ThrowableItemProjectile`：

- 物品堆由原版 `SynchedEntityData` 自动同步
- 返回状态和原力等级使用额外 `EntityDataAccessor`
- 保存使用 `addAdditionalSaveData/readAdditionalSaveData`
- 投出 20 tick 后自动返回，靠近施法者时回到主手、背包或掉落
- 命中实体使用现代自定义光剑 `DamageType`
- 方块命中切换为返回状态

### 模组内部自定义注册表

旧代码使用的以下原版内部类已删除：

- `RegistrySimple`
- `ObjectIntIdentityMap`
- `RegistryDelegate`

剑柄、状态效果和交互使用的自定义注册表不属于游戏注册表。现已改为：

- `LinkedHashMap<String, T>` 保存稳定注册顺序
- `Map<Integer, T>` 保存序列化 ID
- `IdentityHashMap<T, Integer>` 保存对象到 ID 的反向映射
- `ResourceLocation` 保存名称

剑柄注册已改为显式顺序，避免反射字段顺序改变旧存档中的 6 位剑柄 ID。

## 六、常用原版类名与包变化

| 1.7.10 | 1.20.1 |
|---|---|
| `net.minecraft.entity.Entity` | `net.minecraft.world.entity.Entity` |
| `EntityLivingBase` | `LivingEntity` |
| `EntityPlayer` | `Player` |
| `EntityPlayerMP` | `ServerPlayer` |
| `World` | `Level` |
| `TileEntity` | `BlockEntity` |
| `Item` | `net.minecraft.world.item.Item` |
| `ItemStack` | `net.minecraft.world.item.ItemStack` |
| `ResourceLocation` | `net.minecraft.resources.ResourceLocation` |
| `AxisAlignedBB` | `AABB` |
| `Vec3` | `net.minecraft.world.phys.Vec3` |
| `MathHelper` | `Mth` |
| `IBlockAccess` | `BlockGetter` / `LevelReader` / `BlockAndTintGetter`，按用途选择 |
| `ChunkCoordinates` | `BlockPos`，跨维度时额外保存 `ResourceKey<Level>` |
| `EnumChatFormatting` | `ChatFormatting` |
| `StatCollector.translateToLocal` | `Component.translatable(...).getString()` |

不能只做包名替换。实体、方块、物品和 GUI 方法签名也已发生结构性变化，必须逐个查源码确认。

## 七、NBT API

| 1.7.10 | 1.20.1 |
|---|---|
| `NBTBase` | `Tag` |
| `NBTBase.NBTPrimitive` | `NumericTag` |
| `NBTTagCompound` | `CompoundTag` |
| `NBTTagList` | `ListTag` |
| `NBTTagByte` | `ByteTag` |
| `NBTTagShort` | `ShortTag` |
| `NBTTagInt` | `IntTag` |
| `NBTTagLong` | `LongTag` |
| `NBTTagFloat` | `FloatTag` |
| `NBTTagDouble` | `DoubleTag` |
| `NBTTagString` | `StringTag` |
| `new NBTTagInt(value)` | `IntTag.valueOf(value)` |
| `func_150291_c()` 等混淆方法 | `NumericTag.getAsLong()` 等明确方法 |
| `hasKey(name, type)` | `contains(name, type)` |
| `setInteger` / `setLong` | `putInt` / `putLong` |
| `getCompoundTag` | `getCompound` |
| `getTagList` | `getList` |
| `removeTag` | `remove` |
| `JsonToNBT.func_150315_a` | `TagParser.parseTag` |
| `ItemStack.writeToNBT` | `ItemStack.save` |
| `ItemStack.loadItemStackFromNBT` | `ItemStack.of` |
| `hasTagCompound` | `hasTag` |
| `getTagCompound` | `getTag` / `getOrCreateTag` |

`NBTHelper` 和 `DimensionalCoords` 已完成基础迁移：

- `DimensionalCoords` 不再继承已删除的 `ChunkCoordinates`，坐标使用兼容字段并可转换为 `BlockPos`
- 维度由旧整数 ID 改为 `ResourceKey<Level>`，NBT 和网络写入维度 `ResourceLocation`
- 读取旧 NBT 时仍兼容 `dim` 整数：`-1` 为下界、`0` 为主世界、`1` 为末地
- `ByteBufUtils` 已替换为 `FriendlyByteBuf` 的字符串、物品和资源位置读写方法
- 保存适配器只在注册阶段反射构造一次并缓存，序列化热路径不使用反射

`LightsaberData` 已完成基础迁移：

- 光剑哈希继续使用相同的 `long` 位布局，保留旧剑柄、颜色和聚焦水晶编码
- 新物品数据通过 `ItemStack.getOrCreateTag()` 写入
- 读取时继续兼容旧版 `Lightsaber` 复合标签、数字哈希和字符串哈希
- `NBTPrimitive` 已替换为 `NumericTag`，网络仍只传输一个 `long`

`PowerData` 与其 `Container` 也已完成基础序列化迁移：

- `CompoundTag` / `ListTag` 替代旧 NBT 类
- 字符串网络读写统一复用 `NBTHelper`
- 读取未知或已删除的 Power 名称时跳过该项，避免损坏整个玩家数据容器
- `PowerData` 和 `PowerData.Container` 保存适配器已在模组入口缓存注册

## 八、玩家数据 Capability

| 1.7.10 API | 1.20.1 对应方式 |
|---|---|
| `IExtendedEntityProperties` | Forge Capability |
| `registerExtendedProperties` | `AttachCapabilitiesEvent<Entity>` |
| `getExtendedProperties` | `entity.getCapability(...)` |
| `saveNBTData/loadNBTData` | `ICapabilitySerializable<CompoundTag>` |
| 旧玩家克隆事件字段 | `PlayerEvent.Clone#getEntity/getOriginal` |

`ALPlayerData` 已改为自动注册的玩家 Capability，并在克隆玩家时复制数据。Capability 的 `LazyOptional` 会随实体失效，避免旧实体引用泄漏。

`ALEntityData` 也已改为挂载到所有 `LivingEntity` 的 Capability，用于保存：

- 原力推撞墙状态
- 当前 Advanced Lightsabers 状态效果列表
- 状态效果施法者 UUID

状态效果 UUID 新数据使用 `CompoundTag.putUUID/getUUID`，读取时继续兼容旧版 `CasterUUIDMost/CasterUUIDLeast`。

## 九、网络 API

| 1.7.10 API | 1.20.1 对应方式 |
|---|---|
| `SimpleNetworkWrapper` | `SimpleChannel` |
| `IMessage` | 普通消息类，提供 encode/decode/handle 方法 |
| `IMessageHandler` | 注册时传入处理 lambda/方法引用 |
| `MessageContext` | `NetworkEvent.Context` |
| `ByteBufUtils` | `FriendlyByteBuf` 的 `writeUtf/readUtf/writeItem/readItem` 等 |
| `Side.CLIENT/SERVER` | `NetworkDirection.PLAY_TO_CLIENT/PLAY_TO_SERVER` |

网络包只传必要数据；处理逻辑必须通过 `context.enqueueWork` 回到正确线程，并设置 `setPacketHandled(true)`。

当前已迁移 `MessagePlayerData`：

- 使用 `SimpleChannel.messageBuilder` 注册双向消息
- `FriendlyByteBuf` 使用变长整数传玩家实体 ID 和内部数据类型 ID
- 客户端发往服务端时强制校验包内实体 ID 必须等于真实发送者，阻止修改其他玩家数据
- 服务端更新通过 `PacketDistributor.TRACKING_ENTITY_AND_SELF` 发送给追踪者和玩家自己
- 旧 `Side` 权限位改为 `LogicalSide`

`MessageUpdateEffects` 也已迁移为服务端到客户端的 `SimpleChannel` 消息：

- 使用变长整数传实体 ID 和效果数量
- 服务端通过 `TRACKING_ENTITY_AND_SELF` 只同步给实际追踪该实体的客户端
- 客户端处理时复制列表，避免网络消息和 Capability 共用可变集合

`MessagePlayerJoin` 和 `MessageBroadcastState` 已迁移为仅服务端到客户端的全量状态消息：

- `MessageSyncBase` 去除旧 `AbstractMessage` 和 FML `MessageContext`
- 玩家数据与状态效果统一通过 `FriendlyByteBuf` 编解码
- 登录时同步本地玩家状态，开始追踪时按实体 ID 同步其他玩家状态
- 服务端从实际玩家 Capability 构造快照，不接受客户端指定玩家后再转播

旧 `PacketRightClick`、`PacketUnlockPower` 和空的 `MessageForcePower` 在项目中没有调用点，已删除。
原力施放与能力解锁后续由现代输入/GUI 流程重新接入，并由服务端验证真实发送者、解锁状态、
消耗与冷却，不能沿用旧包信任客户端玩家 ID 和能力 ID 的模型。

其他旧消息仍需逐个改为现代 encode/decode/handle 结构。

### 按键与原力输入

旧 `FiskKeyHandler`、`InteractionHandler`、`MessageInteraction` 和 `AbstractMessage` 整套输入层已删除，
替换为：

| 1.7.10 | 1.20.1 |
|---|---|
| `ClientRegistry.registerKeyBinding` | `RegisterKeyMappingsEvent` |
| LWJGL 2 `Keyboard.KEY_*` | GLFW `GLFW_KEY_*` |
| `KeyInputEvent` | 客户端 `TickEvent.ClientTickEvent` + `KeyMapping.consumeClick/isDown` |
| 通用 Interaction 注册表 | 明确的客户端输入处理器 |
| `IMessage` 按玩家 ID 执行 | `SimpleChannel` 服务端包，通过 `Context.getSender()` 获取真实玩家 |

当前输入逻辑：

- `R`：客户端立即预测光剑开关声音和状态，服务端重新验证主手确为光剑后设置状态
- `C`：施放当前选中的 `PER_USE` 原力；服务端重新验证已解锁、能量、最大原力和冷却
- `F` 短按：在三槽已选原力间循环
- `F` 长按 5 tick：撤销短按产生的那次循环并打开现代 `GuiSelectPowers`
- 原力选择界面保留 4×4 可用能力区、三个快捷槽、Shift 快速加入/移除和拖放替换
- 选择结果继续写入 `ALData.SELECTED_POWERS`，关闭界面时通过现有玩家数据消息同步

新增 `MessageToggleLightsaber` 和 `MessageUsePower` 均为仅客户端到服务端消息，不接受客户端指定实体 ID。

### 客户端声音过滤

旧 `PlaySoundEvent17` 和 `PositionedSound` 包装已替换为 `PlaySoundEvent` 与委托式
`ScaledSoundInstance`。潜行效果存在时，除潜行开关/环境声外的声音仍按旧逻辑缩放为 5%，且包装器
直接委托原声音解析、流式加载、衰减、循环和坐标，不重新抽取随机音高或音量。

## 十、旧 Coremod / ASM

1.7.10 使用了 LaunchWrapper 和 `IFMLLoadingPlugin`，这些机制在 1.20.1 已不可用。旧 Coremod 文件已经移除。

### 已确认的旧注入功能

| 旧 Transformer | 原功能 | 1.20.1 迁移方案 |
|---|---|---|
| `ClassTransformerEntityPlayer` | 光剑攻击替换伤害源 | Forge 伤害/攻击事件 |
| `ClassTransformerEntityMob` | 生物持光剑攻击时替换伤害源 | Forge 伤害/攻击事件 |
| `ClassTransformerModelBiped` | 修改玩家/生物手臂姿势 | 客户端实体渲染事件或模型准备阶段 |
| `ClassTransformerModelBipedMultiLayer` | 第三方多层模型兼容 | Battlegear/旧 Heroes 兼容不再保留 |
| `ClassTransformerEffectRenderer` | 扩展旧粒子图层 | 自定义 `ParticleType`、`ParticleProvider`、粒子渲染类型 |
| `ClassTransformerColor` | 阻止其他渲染器覆盖 GL 颜色 | 用 `PoseStack/VertexConsumer` 显式传递颜色，不再全局修改字节码 |

修改原版行为时仍遵循：Forge 事件优先；没有事件钩子时才使用 Mixin。

### DamageSource / DamageType

1.20.1 的自定义伤害不再通过继承旧 `DamageSource` 并运行时添加枚举完成。现已改为：

- `ResourceKey<DamageType>` 定义 `force`、`lightning`、`lightsaber`、`into_wall`
- `data/lightsabers/damage_type/*.json` 提供数据驱动伤害类型
- `DamageTypeTags` JSON 声明穿甲和闪电属性
- 从当前 `Level.registryAccess()` 获取 `Holder<DamageType>` 后构造 `DamageSource`

### 原力射线目标

旧版四个效果类重复使用逐点扫描和 `MovingObjectPosition`。现已抽取为公共 `ForceTargeting`：

- `Level.clip(ClipContext)` 限制方块遮挡距离
- `ProjectileUtil.getEntityHitResult` 查找视线中的生物
- 推力、锁喉、眩晕、吸血和闪电统一复用
- `VectorHelper` 已迁移为不可变 `Vec3`、`AABB` 和现代实体旋转/位置 API

## 十一、渲染 API

旧版即时 OpenGL API已经不能作为主要渲染方式继续使用：

- `GL11.glTranslatef/glRotatef/glColor*`
- `Tessellator.startDrawingQuads`
- `IItemRenderer`
- `ModelBase`
- 旧版 `ModelRenderer`
- `TileEntitySpecialRenderer`

1.20.1 主要使用：

- `PoseStack`
- `VertexConsumer`
- `MultiBufferSource`
- `RenderType`
- `EntityModel` / `ModelPart`
- `EntityRendererProvider.Context`
- `BlockEntityRenderer`
- `BlockEntityWithoutLevelRenderer`

### Force Lightning 渲染实体

旧 `EntityForceLightning` 只负责提供一个存活 2 tick 的客户端渲染锚点，真正伤害由原力效果处理。
当前迁移保持这一职责边界：

| 1.7.10 | 1.20.1 |
|---|---|
| 裸 `Entity` + 公共施法者字段 | `SynchedEntityData` 保存施法者实体 ID |
| `setSize/setDead` | 注册 `EntityType` + `discard()` |
| 客户端 `spawnEntityInWorld` | `ClientLightningSpawner` 管理每名施法玩家的本地锚点 |
| `Render` / `doRender` | `EntityRenderer<EntityForceLightning>` |
| `GL11` 和线宽状态 | `PoseStack` + `VertexConsumer` 四边形线段 |
| `ALRenderHelper.drawLightningLine` | 外层加法光晕与白色核心两个 `RenderType` |
| 旧方块射线 | `Level.clip(ClipContext)` |

当前保留：

- 闪电状态从左右手各生成多束蓝色闪电，数量继续随 amplifier 增加
- Drain 状态向所有由当前施法者标记的目标生成橙色闪电
- 有生物目标时连接目标身体中心，否则连接 7 格视线内的方块命中点
- 第一人称继续使用更靠近镜头的手部起点和加粗闪电
- 随机折线以 tick 和 bolt 索引为稳定种子，并在前后 tick 间插值，避免每帧跳变
- 渲染实体不参与伤害、碰撞或服务端玩法判定

旧递归 `Lightning` 树和 `ALHelper.createLightning/branchLightning` 已无调用并删除。当前闪电已完成现代
缓冲区迁移，但尚未进行旧版固定相机像素差校准，因此不能标记为视觉 `1:1 verified`。

仓库内大量 Tabula 生成模型仍使用旧 `ModelBase/ModelRenderer`。计划提供模组内部兼容模型层或批量转换，避免手工重写约 68 个模型文件，同时最终渲染必须落到现代缓冲区 API。

## 十二、粒子 API

| 1.7.10 | 1.20.1 |
|---|---|
| `EntityFX` | `Particle` / `TextureSheetParticle` |
| `EffectRenderer` | `ParticleEngine` |
| 修改 `fxLayers` 数组 | 注册自定义 `ParticleType` 和 `ParticleProvider` |
| 手动绑定旧粒子图层 | 选择或实现 `ParticleRenderType` |

不再通过 ASM 增加粒子层数量。

## 十三、Dynamic Lights 1.20.1

源码位置：

`F:\CloneProjects\atomicstrykers-minecraft-mods\DynamicLights`

已确认变化：

| 1.7.10 | 1.20.1 |
|---|---|
| `atomicstryker.dynamiclights.client.DynamicLights` | `atomicstryker.dynamiclights.server.DynamicLights` |
| `atomicstryker.dynamiclights.client.IDynamicLightSource` | `atomicstryker.dynamiclights.server.IDynamicLightSource` |
| 模组 ID `DynamicLights` | 模组 ID `dynamiclights` |

`CommonEventHandlerDL` 已按新源码迁移：

- 使用服务端 `TickEvent.LevelTickEvent`，不再从客户端线程扫描 `loadedEntityList`
- 手持或装备激活光剑的 `LivingEntity` 继续发出 15 级动态光
- 投出的 `EntityLightsaber` 继续发出 15 级动态光
- 通过 `DynamicLights.addLightSource/removeLightSource` 注册和清理适配器
- 每个 `ServerLevel` 使用 `IdentityHashMap` 缓存实体适配器，扫描间隔由旧毫秒配置换算为 tick
- 配置关闭或维度卸载时立即移除全部已注册光源
- 删除旧后台 `Thread`，避免异步读取实体和 Dynamic Lights 世界状态

该事件处理器只在 `ModList` 确认 `dynamiclights` 已加载时实例化，保持依赖可选。

## 十四、NEI → JEI 15

旧 NEI API不能映射为同名类：

- `IConfigureNEI`
- `TemplateRecipeHandler`
- `PositionedStack`
- `GuiRecipe`
- `codechicken.lib.gui.GuiDraw`

1.20.1 已改为 JEI 插件结构：

```java
@JeiPlugin
public final class LightsabersJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ...;
    }
}
```

当前通过 JEI 的 recipe category、recipe type、recipe registration 和 catalyst registration 重建：

- 光剑锻造台配方
- 双刃光剑配方
- 拆解台配方

已确认并使用的 JEI 15.20 API：

| 用途 | JEI 15.20 API |
|---|---|
| 插件入口 | `@JeiPlugin` + `IModPlugin` |
| 自定义类别 | `RecipeType<T>` + `IRecipeCategory<T>` |
| 槽位布局 | `IRecipeLayoutBuilder.addInputSlot/addOutputSlot` |
| 概率提示 | `IRecipeSlotBuilder.addRichTooltipCallback` |
| 配方注册 | `IRecipeRegistration.addRecipes` |
| 工作站入口 | `IRecipeCatalystRegistration` |
| GUI 点击区域 | `IGuiHandlerRegistration.addRecipeClickArea` |
| 锻造台转移 | `IRecipeTransferRegistration.addRecipeTransferHandler` |
| NBT 子类型 | `ISubtypeRegistration` |

当前 JEI 行为：

- 光剑锻造类别按已注册剑柄生成可制作的默认组合，8 个输入槽直接复用
  `ContainerLightsaberForge.SLOTS`，输出继续使用激活状态预览
- 双刃类别显示两把竖直相邻的单刃光剑及完整 NBT 合成结果
- 拆解类别直接调用 `TileEntityDisassemblyStation.getOutput`，不复制拆解算法；输出 tooltip 显示真实概率
- 明暗锻造台、拆解台和工作台已注册为对应类别 catalyst
- 光剑锻造 GUI 已注册 8 输入槽的 JEI 自动转移，锻造台和拆解台 GUI 已注册点击区域
- 光剑与双刃光剑使用只包含 `LightsaberData` 哈希的 subtype interpreter，避免预览用 `Active` NBT
  导致 JEI 无法从玩家持有的未激活光剑查询配方
- 颜色水晶、水晶袋、原力石和半砖继续按 NBT 区分 JEI 子类型

旧 `NEILightsabersConfig`、三个 `TemplateRecipeHandler` 以及全部 `codechicken.nei` 引用已删除。

### 配方系统

| 1.7.10 | 1.20.1 |
|---|---|
| `GameRegistry.addRecipe` | `data/<modid>/recipes/*.json` |
| `GameRegistry.addShapelessRecipe` | `minecraft:crafting_shapeless` JSON |
| `GameRegistry.addSmelting` | `minecraft:smelting` JSON |
| `IRecipe` / `InventoryCrafting` | `CustomRecipe` / `CraftingContainer` |
| 物品 metadata 原料 | `forge:nbt` 精确匹配现代 NBT |
| metadata 配方输出 | Forge 扩展的 result `nbt` 字段 |
| 运行时直接注册特殊配方 | `DeferredRegister<RecipeSerializer<?>>` + 配方类型 JSON |

当前共迁移 38 个配方资源：

- 1 个保留两把单刃全部 `LightsaberData` 的双刃特殊配方
- 明暗原力石的基础、刻纹、柱、半砖、楼梯、激活和烧制配方
- 电路板、明暗锻造台、展示架和拆解台配方
- 18 种 `CrystalColorId` 水晶到同颜色水晶袋的精确 NBT 配方

原力石/半砖变体使用 `BlockStateTag.variant`，水晶/水晶袋使用 `CrystalColorId`；这些字段与当前
`ItemForcestone`、`ItemForcestoneSlab` 和 `ItemCrystal` 的真实读写格式一致。烧制配方按玩法语义将
`cracked` 修复为 `default` 变体。

## 十五、CodeChickenLib 1.20.1

源码位置：

`F:\CloneProjects\CodeChickenLib`

已确认：

- 模组 ID：`codechickenlib`
- 版本：`4.4.0.516`
- 旧 `codechicken.lib.gui.GuiDraw` 已不存在
- 新版 GUI集中在 `codechicken.lib.gui.modular` 下

旧 NEI handler 中对 `GuiDraw` 的使用不应机械替换；JEI recipe category 应直接使用 `GuiGraphics` 和 JEI drawable API。CCL 只在确实需要其现代渲染、模型、网络或配置工具时使用。

## 十六、Battlegear 2

Battlegear 2 没有 1.20.1 版本，因此以下内容不再迁移：

- `mods.battlegear2.*` 导包
- 双持战斗模式检测
- Battlegear 专属渲染事件
- `IBattlegearWeapon`、`IOffhandWield`、`IAllowItem` 接口

原版 1.20.1 已有主手/副手系统。需要保留的双持逻辑应改为检查：

```java
player.getMainHandItem();
player.getOffhandItem();
```

## 十七、基础方块与 BlockState

旧原力石方块使用一个 metadata 同时表示纹理变体和柱体方向。1.20.1 已保留单个明/暗方块注册项，
并改为数据明确的方块状态：

| 1.7.10 | 1.20.1 |
|---|---|
| metadata `0/1/2/3/4/5/6` | `variant + axis` BlockState 属性 |
| `BlockStairs` | `StairBlock`，基础状态使用 `Supplier<BlockState>` |
| 半砖/双层半砖两个方块 | 一个 `SlabBlock`，使用 `SlabType.BOTTOM/TOP/DOUBLE` |
| `ItemMultiTexture` | `BlockItem` + `BlockStateTag` |
| `IIcon/registerBlockIcons` | blockstate、block model 和 item model JSON |
| `setLightLevel(1.0F)` | `lightLevel(state -> 15)` |
| `setResistance(100)` | 爆炸抗性 `300`；旧方法内部会乘以 `3` |

`BlockForcestone` 提供旧 metadata 与现代状态的双向转换，后续迁移遗迹结构模板时统一调用，
避免在世界生成代码中重复散落转换判断。原映射保持：

- `0`：默认
- `1`：雕刻
- `2/3/4`：Y/X/Z 轴柱体
- `5`：裂纹
- `6`：苔藓/破损

掉落表使用 `minecraft:copy_state` 把 `variant` 写入物品的 `BlockStateTag`，放置时由
`BlockItem` 恢复。原力石半砖额外校验手中物品与已有半砖的明暗变体，禁止不同变体错误合并成双层半砖。

基础方块现已使用：

- `DeferredRegister<Block>` / `RegistryObject`
- 自动注册对应 `BlockItem`
- `BuildCreativeModeTabContentsEvent`
- `mineable/pickaxe` 方块标签
- 完整 blockstate、模型、掉落表与语言键

### 水晶方块与 BlockEntity

水晶方块已恢复为可放置方块，颜色继续保存在方块实体中：

| 1.7.10 | 1.20.1 |
|---|---|
| `ITileEntityProvider` | `BaseEntityBlock` |
| `TileEntity` | `BlockEntity` |
| 放置方向 metadata `1-6` | `DirectionProperty FACING` |
| `createNewTileEntity()` | `newBlockEntity(BlockPos, BlockState)` |
| `S35PacketUpdateTileEntity` | `ClientboundBlockEntityDataPacket` |
| `getDescriptionPacket()` | `getUpdatePacket()` / `getUpdateTag()` |
| 返回 `null` 碰撞箱 | `noCollission()` / `Shapes.empty()` |
| `TileEntitySpecialRenderer` / `IItemRenderer` | 染色 baked block/item model |

保留的原玩法：

- 可附着在六个方向的完整支撑面上
- 支撑方块消失时掉落对应颜色水晶
- 玩家直接破坏时不产生掉落
- 右键水晶方块会拾取对应颜色水晶并移除方块
- 方块实体继续使用旧 `color` NBT 键保存颜色
- 颜色变化通过方块实体更新包同步并触发客户端重绘

旧 `RenderCrystal`、`RenderItemCrystal` 和 `ModelCrystal` 已删除。当前 baked model 是轻量兼容渲染，
后续若恢复原版更复杂的半透明几何外观，应使用现代模型或 `BlockEntityRenderer`，不能重新引入即时 OpenGL。

### 光剑展示架

展示架已从 metadata、`BlockContainer` 和旧 TESR 迁移为现代方块状态、方块实体和渲染器：

| 1.7.10 | 1.20.1 |
|---|---|
| `BlockContainer` | `BaseEntityBlock` |
| metadata `0/1` 表示地面 X/Z 朝向 | `Direction.Axis` 的 `axis` BlockState |
| metadata `2-5` 表示四面墙 | 排除 `DOWN` 的 `DirectionProperty facing` |
| `canPlaceBlockOnSide()` / `canBlockStay()` | `getStateForPlacement()` / `canSurvive()` |
| `isSideSolid()` | `Block.canSupportCenter()` |
| `onNeighborBlockChange()` | `updateShape()` |
| `breakBlock()` 手动生成 `EntityItem` | `onRemove()` / `Block.popResource()` |
| `ChatComponentTranslation` / `ChatStyle` | `Component.translatable()` / `ChatFormatting` |
| `S35PacketUpdateTileEntity` | `ClientboundBlockEntityDataPacket` |
| `TileEntitySpecialRenderer` / `GL11` | `BlockEntityRenderer` / `PoseStack` / `ItemRenderer` |
| `IItemRenderer` | baked item model |

保留的原玩法：

- 仅可安装在地面和四面墙，不能安装在天花板
- 地面安装时仍按玩家朝向在 X/Z 两种摆放方向间切换
- 支撑面消失时展示架自身和内部光剑分别正常掉落
- 放置者 UUID 仍保存为所有者；创造模式玩家仍可绕过所有权限制
- 所有者右键时可在空手或整把 `ItemLightsaberBase` 与展示内容之间交换
- 非所有者交互继续显示红色 `message.lightsaberStand.notOwner` 翻译文本
- 展示光剑掉落时完整保留 `ItemStack` NBT
- 方块实体继续读取和写入旧 `DisplayStack`、`Owner.UUIDMost`、`Owner.UUIDLeast` 键

展示架本体现在使用轻量 baked block/item model；内部光剑由现代
`BlockEntityRenderer` 调用 `ItemRenderer.renderStatic()` 绘制。旧 `ModelLightsaberStand` 和
`RenderItemLightsaberStand` 已删除，不再加载旧即时 OpenGL。光剑自身的完整现代物品模型仍属于后续统一光剑渲染迁移批次。

### 光剑锻造台

明暗两种光剑锻造台已恢复为现代双方块工作站：

| 1.7.10 | 1.20.1 |
|---|---|
| metadata `0-3` 主块方向 | `HorizontalDirectionalBlock.FACING + part=base` |
| metadata `4-7` 面板半块 | `HorizontalDirectionalBlock.FACING + part=panel` |
| `ItemBlock.onItemUse()` 手动放置两个方块 | `BlockItem.placeBlock()` 原子放置主块和面板 |
| 越界方块碰撞箱 | 继续使用可跨方块的 `VoxelShape` 保持原 2×1 范围 |
| `getMobilityFlag() == 2` | `PushReaction.BLOCK` |
| `onNeighborBlockChange()` | `onRemove()` 联动移除另一半 |
| `player.openGui()` / `IGuiHandler` | `NetworkHooks.openScreen()` / 注册的 `MenuType` |
| `IInventory` | `SimpleContainer` |
| `Container` / `Slot` | `AbstractContainerMenu` / 现代 `Slot` |
| `transferStackInSlot()` | `quickMoveStack()` |
| `InventoryCraftResult` | `ResultContainer` |
| `Items.fish` metadata | `ItemTags.FISHES` 中的四种原始鱼类 |
| `GuiContainer` | `AbstractContainerScreen` / `GuiGraphics` |
| TESR、`ModelBase`、`IItemRenderer` | baked block/item model |

保留的制作逻辑：

- 8 个输入槽和 1 个输出槽的位置、槽容量与组件兼容判断保持不变
- 发射器、开关、握柄、尾盖、电路和颜色水晶仍为必需组件
- 两个聚焦水晶槽仍为可选槽
- 组件 fingerprint 仍合并为原 `LightsaberData` 长整数编码，并拒绝重复 bit
- 输出预览使用激活状态；玩家实际取走后自动恢复为未激活状态
- 剑柄总长度小于 `19 cm` 时仍显示“Too short”并禁止取出
- Shift 点击会优先把组件放入首个兼容空槽
- 菜单关闭时未使用的输入材料仍掉落给玩家，而不是保存在方块中
- 使用任意 `minecraft:fishes` 原始鱼替代颜色水晶时，继续写入实际鱼物品 ID 到旧 `Special` NBT 键

旧 `TileEntityLightsaberForge` 不保存数据，只负责旧 TESR 的渲染锚点；在本体改为 baked model 后已经删除，
菜单现在直接以主块 `BlockPos` 校验距离和方块有效性。旧 `RenderLightsaberForge`、
`RenderItemLightsaberForge`、`ModelLightsaberForge` 和未注册的 `GuiHandlerAL` 也已删除。

当前 GUI 保留原背景、输入布局、组件提示、长度/过短状态和输出预览。原先依赖即时 OpenGL 的旋转 3D 光剑预览
暂以现代物品渲染预览替代，后续统一迁移完整光剑模型时可直接增强该预览，不重新引入 `GL11`。

### 拆解台

拆解台已从 metadata 多方块、`ISidedInventory` 和旧进度包迁移为现代四部分结构与方块实体：

| 1.7.10 | 1.20.1 |
|---|---|
| metadata 方向与 `BASE/SIDE/TOP` | `facing + BASE/SIDE/TOP_BASE/TOP_SIDE` BlockState |
| `ItemBlock.onItemUse()` 依次放置四块 | `BlockItem.placeBlock()` 原子放置 2×2 结构 |
| `BlockContainer` | `BaseEntityBlock`，仅 base 创建 BlockEntity |
| `ITickable.updateEntity()` | 服务端 `BlockEntityTicker` |
| `ISidedInventory` | `WorldlyContainer` |
| `ItemStack[]` / `null` | `NonNullList<ItemStack>` / `ItemStack.EMPTY` |
| 手写 `Items` NBT 列表 | `ContainerHelper.saveAllItems/loadAllItems` |
| `ICrafting.sendProgressBarUpdate()` | `ContainerData` / `addDataSlots()` |
| `Container.calcRedstoneFromInventory()` | `AbstractContainerMenu.getRedstoneSignalFromContainer()` |
| `GuiContainer` | `AbstractContainerScreen` / `GuiGraphics` |
| TESR、`ModelBase`、`IItemRenderer` | baked block/item model |

保留的运行逻辑：

- base、side、两个 top 仍组成同方向的 2×2 结构，任一部分损坏都会清除完整结构
- 生存模式破坏任意部分只掉落一个拆解台物品；创造模式不掉落
- base 仍保存 17 个槽：输入、燃料和 15 个输出槽
- 一次拆解仍需要 `2400 tick`
- 红石粉仍提供 `300 tick`，红石块仍提供 `2700 tick`
- 旧 `registerFuel(ItemStack, int)` 和 `getFuels()` 公共入口继续保留
- 顶面、底面和侧面的自动化可访问槽及燃料抽取限制保持不变
- 单刃、双刃光剑的部件、聚焦水晶、颜色水晶和电路板回收概率保持原数值
- 带旧 `Special` NBT 的单刃光剑仍不会回收颜色水晶
- 输出槽满时仍把无法放入的回收物掉落到世界中
- `BurnTime`、`DisassemblyTime` 和 `Items/Slot` 旧 NBT 键继续兼容
- 比较器仍读取 base 的 17 槽库存占用程度

旧碰撞范围中 base 高 `14/16`、side 高 `17/16`、top 的负 Y 延伸和四向宽度均已转换为
`VoxelShape`。当前拆解台使用的是轻量 baked 几何，并非旧 `ModelDisassemblyStation` 的 1:1 模型转换；
旧 `RenderDisassemblyStation`、`RenderItemDisassemblyStation` 和 `ModelDisassemblyStation` 已删除。

### Holocron 与原力升级界面

Jedi/Sith Holocron 已从物品 metadata、客户端主动发送 `PacketTileAction` 和旧 `GuiScreen` 迁移为现代状态：

| 1.7.10 | 1.20.1 |
|---|---|
| metadata `0/1` | `variant=jedi/sith` BlockState |
| `ItemBlockWithMetadata` | `BlockItem + BlockStateTag` |
| GUI 打开/关闭时发送 `PacketTileAction` | 无槽 `MenuType` 的服务端生命周期 |
| `playersUsing++/--` 客户端请求 | `ContainerHolocron` 服务端构造/移除时更新 |
| `TileEntity.updateEntity()` | 双逻辑侧 `BlockEntityTicker` |
| 动态 `setBlockBounds()` | 预缓存 101 档动态 `VoxelShape` |
| `GuiScreen` / Achievement 背景 API | `AbstractContainerScreen` / `GuiGraphics` |
| `MathHelper` | `Mth` |
| 旧 TESR、Tessellator、即时 GL | 现代 BER 兼容预览 |

保留的逻辑：

- Jedi 和 Sith 两个物品变体仍放置同一个 Holocron 方块
- 方块仍提供光照，并保持原极高爆炸抗性
- 非潜行右键打开原力升级界面，潜行右键继续返回给其他交互
- 多个玩家同时打开时继续累计使用人数
- `openTimer` 的 `0.85` 衰减、`+0.05` 后乘 `1.05` 的开启曲线和 `openTicks` 浮动计时保持不变
- 开启人数通过 BlockEntity 更新包同步，但不会写入世界存档，避免重启后残留使用人数
- Jedi/Sith 动态碰撞尺寸仍按旧公式变化；形状已缓存，热路径不重复创建 `VoxelShape`
- 原力界面继续显示 Force XP、Base Power、父级要求、剩余 XP 和解锁状态
- 按住鼠标投资原力能力时继续设置并同步 `ALData.DRAINING_XP_TO`
- 鼠标移出、松开、能力解锁或界面关闭时会立即清空投资目标

旧图状原力技能树当前改为可滚动列表界面，保留升级功能但不宣称视觉 1:1。旧 Holocron renderer 是程序化
三角形/四边形模型，当前 `RenderHolocron` 仅为现代 full-bright BER 兼容预览。其完整还原方式、缓存网格、
UV 和验证流程已记录在 `MODEL_RESTORATION_1.20.1.md`。

### Sith 棺材、石棺与 Sith Ghost

旧棺材家族依赖 metadata 位、`BlockContainer`、`IInventory`、TESR、客户端发起的
`PacketTileAction`，石棺还会在服务端错误调用 `sendToServer`。1.20.1 已改为：

| 1.7.10 | 1.20.1 |
|---|---|
| 方向 metadata + front/upper 位 | `FACING` + `PART` 枚举 BlockState |
| `BlockContainer` / `ITileEntityProvider` | `BaseEntityBlock` |
| `TileEntity.updateEntity()` | 注册的 `BlockEntityTicker` |
| `IInventory` + `ItemStack[]` | `Container` + `NonNullList<ItemStack>` |
| `S35PacketUpdateTileEntity` | `ClientboundBlockEntityDataPacket` |
| `player.openGui()` | `NetworkHooks.openScreen()` |
| `Container` / `GuiContainer` | `AbstractContainerMenu` / `AbstractContainerScreen` |
| `EntityMob` + `EntityAIBase` | `Monster` + `Goal` |
| `SharedMonsterAttributes` | `EntityAttributeCreationEvent` + `Attributes` |
| `setCurrentItemOrArmor()` | `setItemSlot()` |
| `onSpawnWithEgg()` | `finalizeSpawn()` |
| `PacketTileAction` 双端转发 | 服务端方块/方块实体直接修改并发送更新包 |
| `TileEntitySpecialRenderer` / `ModelRenderer` | BER + `ModelPart`/`LayerDefinition` |

现有逻辑保持：

- Sith Coffin 仍为朝向相关的水平双格结构，任意部分破坏均安全定位 base，生存只掉一个物品，创造不掉
- 28 槽旧库存和 `Items`/`Slot` NBT 保留；界面继续显示旧版实际可见的 3×9 槽位
- 首次开盖烟雾、60 tick 开合、潜行切换盖板、完全打开后进入库存、开关音效和比较器输出保留
- Sith Stone Coffin 仍为上下双格结构，红石或墓穴主棺附近玩家均可唤醒 Sith Ghost
- `Equipment`、`BaseplateOnly`、`TaskFinished`、`CoffinX/Y/Z` 旧 NBT 键保留
- 唤醒时装备转移到幽灵主手并关闭光剑，石棺只留下 3/16 高底座；幽灵完成战斗返回后会恢复上半部分
- 非创造玩家左击尚未唤醒且 `taskFinished` 的石棺时，继续立即回收带 `Equipment` NBT 的物品
- Sith Ghost 的生命、伤害、速度、抗击退、跟随距离、近战、横移、投掷光剑、回墓和 60 tick 障碍破坏 AI 已迁移
- `PacketTileAction` 已删除，棺材流程不再信任客户端提交的实体 ID、坐标或动作类型
- 棺材、石棺和 Sith Ghost 的旧 `ModelRenderer` 几何已人工转换为现代模型层；尚未完成固定相机像素差验证，
  因此目前标记为“几何迁移完成、未 1:1 verified”
- 两种方块物品当前使用稳定的 baked cube 兼容模型，物品形态的旧 TESR 外观仍按
  `MODEL_RESTORATION_1.20.1.md` 中的批量模型还原流程继续处理

## 十八、当前迁移状态

### 水晶与水晶袋

旧水晶颜色依赖物品 metadata，水晶袋库存依赖 `IInventory`、`Container`、`IGuiHandler` 和 `player.openGui`。1.20.1 已改为：

| 1.7.10 | 1.20.1 |
|---|---|
| metadata / `getItemDamage()` | `ItemStack` 的 `CrystalColorId` NBT |
| `IInventory` + `ItemStack[]` | `SimpleContainer` + `ItemStack.EMPTY` |
| `getSizeInventory()` | `getContainerSize()` |
| `getStackInSlot()` | `getItem()` |
| `markDirty()` | `setChanged()` |
| `isUseableByPlayer()` | `stillValid()` |
| `Container` | `AbstractContainerMenu` |
| `transferStackInSlot()` | `quickMoveStack()` |
| `mergeItemStack()` | `moveItemStackTo()` |
| `Slot.isItemValid()` | `Slot.mayPlace()` |
| `Slot.canTakeStack()` | `Slot.mayPickup()` |
| `IGuiHandler` / GUI 数字 ID | 注册的 `MenuType` |
| `player.openGui()` | `NetworkHooks.openScreen()` |
| `GuiContainer` | `AbstractContainerScreen` |
| `drawGuiContainerBackgroundLayer()` | `renderBg(GuiGraphics, ...)` |

现有逻辑保持：

- 18 种水晶颜色各自绑定一个固定槽位
- 每个袋内槽最多保存 16 个水晶
- 袋内物品继续保存到袋子自身的 `Slots` NBT 列表，兼容旧键名
- `PouchID` 字符串 UUID 继续保留，打开菜单时禁止移动当前袋子，也禁止把自身嵌套进去
- 主手打开时通过额外菜单数据传输当前热栏槽号；按旧版语义不从副手打开
- 水晶和水晶袋的颜色均通过 `RegisterColorHandlersEvent.Item` 渲染

`ItemCrystal` 现已重新继承 `BlockItem` 并绑定 `BlockCrystal`，颜色仍使用 `CrystalColorId` NBT，
因此水晶袋、配方组件和可放置方块共用同一个注册物品。

### 已完成

- Forge 1.20.1 / Gradle 8.8 / Java 17 构建环境
- Parchment 1.20.1 映射
- JEI、CodeChickenLib、Dynamic Lights 依赖解析
- `mods.toml` 和 `pack.mcmeta`
- 现代 `@Mod` 入口基础结构
- 模组构造函数上下文注入
- `DistExecutor` 客户端代理基础结构
- `ForgeConfigSpec` 配置
- Dynamic Lights 新模组 ID
- 内部自定义注册表重写
- 剑柄显式稳定注册顺序
- 删除失效的 LaunchWrapper/Coremod Transformer
- `NBTHelper` 现代 NBT/网络基础序列化
- `DimensionalCoords` 的 `BlockPos`、`ResourceKey<Level>` 与旧维度 ID 兼容
- `LightsaberData` 的现代 NBT、物品标签和 `long` 网络序列化
- `ALData` / `ALDataInterp` 的现代实体、`Mth`、NBT 和逻辑侧 API
- `ALPlayerData` Forge Capability 与玩家克隆复制
- `MessagePlayerData` / `SimpleChannel` 首个双向数据同步包
- `MessagePlayerJoin` / `MessageBroadcastState` 全量玩家状态同步
- `PowerData` / `PowerData.Container` 现代 NBT 与网络序列化
- `Power`、`ForceSide`、`PowerDesc` 的现代组件、玩家与序列化 API
- `Effect` / `StatusEffect` 注册、NBT 和逻辑
- `ALEntityData` 生物 Capability
- `MessageUpdateEffects` 状态效果同步包
- 全部 `PowerEffect` 的现代玩家、逻辑侧和目标射线 API
- 数据驱动 `DamageType` / `DamageSource`
- `MovingSoundStatusEffect` / `MovingSoundLightning` 现代可 tick 声音实例
- 核心物品和投掷光剑 `DeferredRegister`
- `ItemLightsaberBase`、`ItemLightsaber`、`ItemDoubleLightsaber`
- `ItemCircuitry`、`ItemFocusingCrystal`、`ItemLightsaberPart`
- `EntityLightsaber` 返回、命中、保存和物品同步逻辑
- `PowerEffectBladeThrow` 与现代物品/实体对接
- `ItemCrystal` 颜色 NBT、稀有度和组件指纹
- `ItemCrystalPouch` UUID、打开菜单和物品颜色
- `InventoryCrystalPouch` 袋内 NBT 库存
- `ContainerCrystalPouch`、`MenuType` 和 `GuiCrystalPouch`
- `ModBlocks` 现代基础注册骨架
- 明暗原力石全部纹理变体和旧 metadata 转换
- 激活原力石柱、明暗楼梯和明暗半砖
- 基础方块 blockstate、模型、掉落表与挖掘标签
- `BlockCrystal` 六向附着、无碰撞、拾取和支撑掉落逻辑
- `TileEntityCrystal` / `ModBlockEntities` 颜色保存与客户端同步
- `ItemCrystal` 重新接入现代 `BlockItem` 放置流程
- `BlockLightsaberStand` 五向附着、地面朝向、所有权与物品交换逻辑
- `TileEntityLightsaberStand` 旧 NBT 兼容和客户端同步
- 展示架 baked model、掉落表、挖掘标签与现代 `BlockEntityRenderer`
- 明暗光剑锻造台双方块放置、跨块碰撞与联动拆除
- `InventoryLightsaberForge` / `ContainerLightsaberForge` 现代制作流程
- `GuiLightsaberForge`、`MenuType`、baked model 和旧鱼类彩蛋兼容
- 拆解台 2×2 多方块结构、跨块碰撞、联动拆除和比较器输出
- `TileEntityDisassemblyStation` 17 槽 `WorldlyContainer`、燃料与拆解 tick
- `ContainerDisassemblyStation` / `GuiDisassemblyStation` 现代同步和界面
- Jedi/Sith Holocron variant、动态 BlockEntity 状态和菜单生命周期
- `GuiForcePowers` 可滚动现代升级界面与持续投资同步
- `PowerManager` 的现代 `Player` API
- `MODEL_RESTORATION_1.20.1.md` 完整模型还原实施方案
- Sith Coffin 水平双格结构、28 槽库存、菜单、GUI、开合动画和旧 NBT
- Sith Stone Coffin 上下双格结构、装备物品 NBT、红石/玩家靠近唤醒和底座状态
- Sith Ghost 现代实体注册、属性、AI、光剑投掷、回墓复原和模型渲染
- 删除已无调用方的旧 `PacketTileAction`
- 60 个光剑 emitter/switch/body/pommel 硬编码模型的现代兼容层
- `LegacyModelBase` / `LegacyModelRenderer` / `LegacyGlState` 的 PoseStack、UV、父子层级和矩阵转接
- `HiltModelRenderer` 共享剑柄组合入口
- 单刃、双刃和四种光剑部件的现代 `IClientItemExtensions` 自定义物品渲染入口
- 删除旧光剑、双刃光剑、光剑部件 `IItemRenderer` 和未使用的旧投掷实体 renderer
- `LightsaberRenderer` 统一单刃、双刃、投掷物品和展示用光剑渲染入口
- `LightsaberBladeRenderer` 的现代 full-bright 核心、加法光晕、crossguard 和光刃尖端几何
- `COMPRESSED`、`FINE_CUT`、`CRACKED`、`INVERTING`、`PRISMATIC`、`jeb_` 光刃分支
- 删除旧 `ModelLightsaberBlade`、即时 Tessellator 光刃和固定管线 GL 状态切换
- `ALRenderHelper` 缩减为现代 shader、scissor、插值和可见性公共工具
- `GameRegistry` 配方迁移为 38 个数据配方和现代双刃 `CustomRecipe` serializer
- JEI 15 光剑锻造、双刃光剑、拆解三类配方、catalyst、GUI 点击区和锻造台转移
- 删除全部旧 NEI handler 和 `codechicken.nei` 引用
- 删除无调用方的旧 `ContainerBasic`、`BlockRegistry`、`ModelHelper` 和 `FiskPredicates`
- `FiskServerUtils` 缩减为实际仍使用的模组信息、插值和现代 `DamageTypeTags` 近战判断
- Dynamic Lights 1.20.1 服务端实体光源扫描、缓存、注册和维度卸载清理
- 现代 R/C/F `KeyMapping` 输入、光剑开关包、原力施放包和三槽原力选择界面
- 删除旧 FiskUtils keybind/interaction/network 消息框架和无调用的旧投掷包
- 删除已由现代 Happy Villager 粒子替代的旧 `EntityFX` 治疗粒子链
- 删除未注册的旧水晶洞矿生成链；其玩法仍列入现代数据驱动世界生成待办
- `PlaySoundEvent` 潜行声音缩放和委托式 `SoundInstance` 包装
- `EntityForceLightning`、本地锚点生成器和现代双层缓冲区闪电渲染
- 删除无调用的旧递归 `Lightning` 树、Battlegear 客户端事件和旧 NBT 反射工具
- 删除已由数据驱动 `DamageType` 取代的 FiskUtils 扩展伤害源链
- `CommonEventHandler` 现代 Forge 玩家、LivingEntity、伤害、死亡、拾取与 tick 事件链
- 登录/追踪状态同步、原力持续消耗与恢复、状态效果、摔落减伤和水晶袋自动拾取
- `ALHelper` 现代经验、阵营、实体射线查询和物品实体掉落 API
- `ALPredicates` 从旧 Guava Predicate 迁移到 Java Predicate
- `/force xp|base|power` 管理命令迁移到 Brigadier，并通过 `RegisterCommandsEvent` 注册
- 删除无调用方的旧 FiskUtils 属性计算包装类
- `StructurePoint` 脱离已删除的 `ChunkCoordinates`，保留结构覆盖区的 X/Z 等价语义
- `EnumStructure` 迁移到现代 `Level`、`Holder<Biome>`、`ResourceKey<Biome>` 与 Java Predicate
- 结构方向辅助类改用现代 `StairBlock` / `ButtonBlock` 类型
- 删除 `ChestGenHooks` / `WeightedRandomChestContent`，以模组内现代加权战利品表替代
- Sith 墓穴 annex/treasury/coffin 与 Jedi 神殿宝箱保留旧权重、数量范围和特殊 NBT 标记
- `LegacyStructureBlocks` 统一转换旧方块 metadata 到现代 `BlockState`
- 保留楼梯朝向/上下半部、半砖类型、原力石 variant、染色玻璃、陶瓦、活塞和红石火把方向
- `Structure` 基类迁移到 `BlockPos`、`BlockState`、现代硬度检查和 `Container` 战利品填充
- Jedi Temple 与 Sith Tomb 主体结构坐标、镜像、废墟、树木、地形填充和棺材关联完成现代迁移
- `WorldGeneratorStructures` 迁移到新区块加载事件，并延迟到服务器任务队列执行以避开区块加载死锁
- `StructureLocator` 改为现代 `ServerLevel` / `ChunkPos` 环形坐标扫描，移除后台线程世界访问
- `/force structure locate|generate` 完成 Brigadier 迁移，支持结构补全、`*`、范围和可选坐标
- `GuiOverlay` 迁移到 `RenderGuiOverlayEvent`，恢复原力条、能力槽和状态效果 HUD
- `ClientEventHandler` 迁移到现代客户端 tick、`RenderPlayerEvent` 与 `RenderLivingEvent`
- 恢复持续型原力按键状态、客户端后处理、背部光剑柄和 Stealth/Gaze 可见性判断
- 删除仅剩注释、无任何调用方的旧水晶洞与重复 VectorHelper 源文件
- Forge 1.20.1 主类改回无参构造器，并为开发运行启用依赖 Mixin refmap 重映射
- Capability 事件类使用唯一类名，修复 Forge ASM 事件处理器在区块生成时的类型碰撞
- `sounds.json` 的声音资源补齐 `lightsabers:` namespace
- 补充 circuitry 与 focusing crystal 的缺失物品模型
- 资源目录从旧 `textures/blocks|items` 迁移到现代 `textures/block|item`，同步更新全部模型引用
- 添加 Forge `gameTestServer` 运行配置，用于无需代替用户接受 EULA 的专用服务端验证
- 客户端代理改用真正延迟的 Dist 分派，修复专用服务器校验阶段加载 `LocalPlayer`
- emitter、module、grip、pommel 共用的零件模型缩放提高到原来的 4 倍；保留短 pommel 的额外补偿和超长零件的高度限制，且不改变完整光剑的显示尺寸
- 完整单刃与双刃光剑在物品栏等 GUI 中额外旋转 180 度，恢复剑柄朝左下、光刃指向右上的原版图标方向；零件与非 GUI 场景不受影响
- 修正现代 `LivingEntity.pick` 未命中时仍返回 `BlockHitResult(MISS)` 的语义差异，仅对真正的 `BLOCK` 命中执行敲方块逻辑，恢复激活光剑左键挥动音效
- 挥剑声音改为在方块射线处理前播放，避免攻击生物时射线穿过实体命中后方方块而吞掉声音；空气、生物和方块挥击现在使用同一触发逻辑
- 第一人称光剑增加类似原版武器工具的右下握持位置校正，副手使用镜像水平偏移；GUI、第三人称、展示框和掉落物变换保持不变
- 第一人称变换进一步替换为原版 `item/handheld` 的 `Y=±90°`、`Z=±25°` 旋转和 `1.13/3.2/1.13` 标准位移，移除错误的 `X=-150°` 旧模型旋转，避免短剑柄偏出屏幕只剩光刃
- 撤回不适用于自定义透明光刃面的原版平面剑旋转（该旋转会让光刃侧对相机而不可见），恢复光刃正面角度，并反转主手水平校正方向、保留向下偏移
- 修复结构自然生成把区块中心方块坐标误当作区块坐标参与 spacing/separation 计算的问题；候选位置改为按区块计算，放置和生物群系检查时再转换到区块中心
- ForgeGradle 增加 `data` 运行配置，输出到现有 `src/generated/resources`；建筑方块布局保留为 Java-backed 自定义 Piece，生成位置、间距、生物群系和序列化均改由现代数据结构控制
- Jedi Temple 与 Sith Tomb 切换到现代 `StructureType`、`StructurePieceType` 和动态注册表资源；旧 `ChunkEvent.Load` 自然生成器与手写定位器已删除
- 结构 Piece 保存类型、原点和随机种子，并在 `postProcess` 中按当前区块 `BoundingBox` 裁剪旧建筑逻辑，避免跨区块同步加载和一次性写入
- Datagen 生成两个 structure、两个 random_spread structure_set 和两个 biome tag；`/force structure locate|generate` 改用原版结构注册表与分区放置路径
- 对照 `F:\CloneProject\AdvancedLightsabers-1.2` 恢复 Sith Stone Coffin 的方向低两位与上下半部 metadata、Sith Coffin 的方向低两位与 FRONT 位 `8`，修复重复完整棺材、错误朝向和 Stone Coffin 方块实体关联
- Sith Tomb annex 与 treasury 箱子按房间布局显式设置现代 `ChestBlock.FACING`，两侧箱子相向、后墙箱子朝向房间内部
- Stone Coffin 的主棺材坐标直接持久化到 BASE 方块实体；上下半部和主棺材 BASE/FRONT 正确后，玩家进入主棺材触发范围可恢复生成 Sith Ghost
- Sith Tomb 红石线放置前通过现代 `BlockState.updateShape` 计算四向连接；红石线、火把、压力板、活塞和红石块使用邻居更新标志，生成完成时立即建立正确连接和初始供电状态
- `WorldGenRegion` 不执行普通 `ServerLevel` 邻居传播，因此结构 Piece 记录红石坐标，并在新 `LevelChunk` 加载后统一重算线形、传播红石功率与活塞状态
- Stone Coffin 的自动接近触发增加 `taskFinished` 门控；幽灵返回可恢复棺材外观和装备，但不会因玩家仍在范围内而无限破坏上半部、重复生成幽灵
- 移除会在完整区块加载阶段造成级联更新和世界卡死的结构红石刷新器；旧结构中的黏性活塞、红石线、红石火把、压力板和红石块统一转换为空气，Sith Tomb 不再生成红石机关
- 旧结构的方块放置改为在坐标对象分配、metadata 转换和世界状态查询前检查当前区块边界；Jedi Temple 模拟层只记录覆盖坐标，不再读取每个待放置位置的世界状态
- Jedi Temple 覆盖列去重由 `ArrayList.indexOf` 改为哈希索引，保留原插入顺序和随机数消耗顺序，同时消除大型结构覆盖统计的平方级查找
- Jedi Temple 地基与上方清空只访问当前生成区块的覆盖列，并复用单个可变坐标；区块外覆盖列仍消费相同随机数，保证各区块生成结果一致
- 按实际水平布局收紧现代 Structure Piece 包围盒：Jedi Temple 由约 90 个候选区块降至约 30 个，Sith Tomb 由约 117 个降至约 33 个；墓穴纵向楼梯仍保留原有动态长度余量
- Sith Stone Coffin 的玩家范围查询由每 tick 改为每秒一次，并按方块坐标错峰，四座石棺不会集中在同一 tick 扫描实体
- Sith Tomb treasury 与 coffin 的附魔书不再直接生成空的 `Items.ENCHANTED_BOOK`；抽中时按现代 `EnchantedBookItem.addEnchantment` 写入 `StoredEnchantments`，从完成注册后的可发现附魔池随机选择附魔及合法等级
- 新增 `exportBlockbenchModels` 工具任务，运行时读取 60 个 `LegacyModelRenderer` 光剑零件模型，批量导出保留 box UV、mirror、inflation、旋转原点和父子层级的 Blockbench `.bbmodel`
- 每个 `.bbmodel` 同时生成稳定 UUID 的 `.mapping.json`，记录 Java 字段与原始 `addBox`/旋转参数，Blockbench 微调后可据此精确反向修改硬编码模型
- Blockbench 导出器扩展支持现代 `LayerDefinition`，补充导出 20-cube Sith Coffin、2-cube Stone Sith Coffin 与 Sith Ghost，并记录 `PartPose`、`CubeDeformation`、mirror 和现代层级路径
- 展示架/石碑、锻造台、拆解台、Holocron 等已经资源化的 Java 方块模型 JSON 与方块纹理会统一复制到 `model_exports/blockbench/block_json`；棺材的方块 JSON 仅为粒子占位，实际模型使用 `tile/*.bbmodel`
- 修正现代 `LayerDefinition` 直接使用 Minecraft 向下 Y 轴导致 Blockbench 中棺材倒置的问题；导出坐标改为 `Y=24-Y`，X/Z Euler 旋转同步取反，mapping 继续保存未转换的 Java 原始值
- 将 Blockbench 5 中微调后的 `ModelSithCoffin.bbmodel` 按稳定 group/cube UUID 反向写回 `ModelSithCoffin.createBodyLayer()`；同步底座两端挡板和左侧斜边三个 cube 的局部位移，保留原层级、旋转、尺寸、UV 与镜像设置
- Dynamic Lights 不再等待默认 1000 ms 全实体扫描才响应光剑状态；服务端通过 `LivingEquipmentChangeEvent` 在手持物 NBT/装备变化后的下一 tick 即时注册或移除光源，投掷光剑则在实体加入/离开世界时同步处理，同时保留低频全实体扫描作为状态修复兜底
- 恢复 1.7.10 `ModelCrystal` 的六簇硬编码水晶模型和 `RenderCrystal` 半透明全亮渲染；现代 BlockEntityRenderer 保留地面、天花板、四面墙朝向及按坐标固定的随机旋转/高度，`ItemCrystal` 使用 BEWLR 恢复 GUI、手持、掉落物和展示框中的 3D 彩色模型，18 种可放置水晶也加入建筑方块页
- 将配方用二维 `lightsaber_crystal` 材料与 3D `lightsaber_crystal_block` 可放置晶簇拆分，避免 JEI/合成表把材料全部渲染成方块；晶簇 GUI 缩放收进 16×16 slot，取消右键直接回收，并限制为下界合金或 Forge 排序中更高等级的镐才能破坏和掉落对应颜色材料
- 3D 水晶簇的 GUI 专用垂直偏移提高到 `0.18`，使模型在玩家物品栏 slot 中略微上移；世界方块、手持、掉落物、展示框和二维配方材料不受影响
- 水晶簇失去附着方块时直接随方块更新变为空气且不产生掉落，堵住破坏支撑方块绕过下界合金镐的采集路径；正常合格镐采集仍掉落对应颜色材料，方块声音由玻璃改为现代 `AMETHYST_CLUSTER`
- 新增保持旧 0–17 ID 不变的 `CrystalColor.RGB(18)`；客户端按 80 tick HSV 色相周期计算动态顶点颜色，二维水晶、水晶袋、3D 晶簇、光剑光刃和物品栏颜色标记同步循环变色，红/绿/深蓝三颗带 NBT 水晶可无序合成 RGB 水晶并在现有 Lightsaber Forge 中锻造 RGB 光剑
- 水晶袋容量和 GUI 从固定 18 槽扩展到 19 槽：新增第三行 RGB 槽，玩家背包与热栏整体下移 18 像素，背景通过复用原纹理分段绘制扩高，并补充 RGB 水晶袋的带 NBT 合成配方
- RGB 动画时间源由 `player.tickCount + partialTick` 改为客户端单调毫秒时钟；不再受 20 TPS 或 tick 波动限制，水晶与光剑颜色现在按每个渲染帧更新
- RGB 渐变不再使用会令加法光晕在双色区间亮度翻倍的满亮度 HSV 环；改为红→绿→蓝→红的恒定通道总量交叉渐变，并使用 smoothstep 缓入缓出，消除黄、青、紫过渡区间的周期性闪亮，同时保持约 4 秒一轮和逐帧更新
- 将旧版 `en_US.lang` 的剑柄型号、聚焦水晶、原力能力、状态效果、命令与界面文本完整转换到 1.20.1 使用的 `en_us.json`，同时补齐现代 `item.lightsabers.*`、`block.lightsabers.*` 和 `entity.lightsabers.*` 注册表键；新增等量 `zh_cn.json` 中文本地化，采用简洁的光剑部件和《星球大战》原力术语命名
- 新增 `crystal_display_stand` 水晶展示台：迁移 Blockbench 方块模型与 polished_blackstone 贴图，注册独立方块实体；右键可放置/取出光剑、二维水晶或水晶簇，展示物在四个尖柱围成的中心上方按坐标相位平滑上下浮动，破坏展示台时会掉落展示物
- 修正水晶展示台的展示姿态：展示物整体上移 10 像素；光剑额外抵消物品 `FIXED` 渲染的 45° Z 轴倾斜，使放置后的光剑保持竖直，水晶继续使用自己的固定朝向
- 修正展示台光剑上下方向反转，最终使用 225° Z 轴姿态使剑柄朝下、剑刃朝上；展示槽 NBT 新增显式 `DisplayPresent` 标记并改用完整方块更新标志，取出物品后客户端会清空旧的展示渲染
- 将 `sith_tomb` 宝库隐藏房间中心原本被旧版红石移除后留下的空位改为 `crystal_display_stand`；四个 `dark_forcestone_stairs` 围绕的展示台在结构生成时随机放入一把随机光剑，或随机颜色的 `lightsaber_crystal_block` 水晶簇方块物品，不使用二维水晶材料
- `sith_tomb` 隐藏房间中心改为钻石块底座（原红石块位置），展示台上移一格放置在钻石块上方；展示台加入 `mineable/pickaxe` 与 `needs_stone_tool` 标签，石质及更高等级镐可采集并掉落；新增 `ABA / 空C空 / AAA` 配方，A 为磨制黑石、B 为磨制黑石压力板、C 为任意颜色的物品水晶
- 移除宝库中心旧版随机全息仪逻辑；该逻辑原本与展示台同处 `(0, 1, -7)`，会覆盖展示台。展示台和钻石底座保持在四个楼梯围成的几何中心，不再发生覆盖或相对错位
- 修正水晶展示台结构调用的坐标参数顺序：`setBlock` 参数为 `(metadata, x, y, z)`，此前误写成 `(0, 1, 0, -7)` 导致展示台向 X 侧偏移一格且方块实体战利品写入找不到目标；现改为 `(0, 0, 1, -7)`，展示台位于钻石块正上方并能正常写入随机光剑或水晶簇
- 收窄光刃多层泛光：普通/十字/压缩光刃的外圈宽度和叠加透明度降低，避免光剑变成过粗的荧光棒；纯白光剑的外圈改为低亮度冷蓝色，核心仍保持纯白，在沙漠等明亮背景中能分辨出内部剑刃
- 白色光剑外圈由冷蓝色修正为三个通道一致的中性灰白，避免视觉上变成淡蓝色；层次仅依靠较暗泛光与纯白核心的亮度差体现，不改变白色光剑的色相
- 光剑锻造台预览增加交互：水平拖拽绕预览中心 Y 轴旋转剑身，可查看正面和背面；垂直拖拽只改变屏幕内旋转角，不改变模型位置，滚轮可缩放；缩放限制在 `0.75–2.25`，预览通过 GUI scissor 裁剪在 `43..157 × 17..64` 框内，不会溢出到槽位或界面外
- 按需求撤销光剑锻造台的拖拽旋转、滚轮缩放与裁剪交互，恢复原先固定 `1.5` 倍缩放、固定 `45°` 旋转和 `(75, 40)` 位置的静态预览
- 方块模型导出器不再只复制普通几何 JSON；导出副本缺少 `display` 时会自动补齐 GUI、第一/第三人称手持、地面与展示框变换，拆解台和光剑锻造台模型可在 Blockbench 的 Display 视图中直接调整

### 正在处理

- 剩余物品和方块注册
- Fortify 护盾的现代缓冲区渲染
- 其余网络消息

### 尚未完成

- 剩余 Forge 游戏注册表
- 剩余方块和方块实体
- 剩余实体与 AI
- 其余网络消息
- 容器和菜单
- GUI
- 世界生成
- 剩余粒子、声音和客户端渲染
- 其余旧模型导出、资源化和像素差验证
- 光刃和 crossguard 的固定相机截图、透明层亮度及像素差校准
- 资源文件路径、语言文件和 JSON 模型转换
- 完整 `compileJava` / `build` 验证

最近一次 `clean build` 已成功，包含 `compileJava`、`processResources`、`jar` 与 `reobfJar`；Java 编译错误为 0。开发客户端已完成模组构造、资源加载、整合服务器创建、新区块生成并进入实际世界，运行日志中的模组相关 ERROR、FATAL、缺失纹理、缺失模型和缺失声音计数均为 0。Forge GameTest 专用服务器也已完成模组构造、三维度世界加载、出生区生成、保存和正常关闭，0 个必需测试全部通过。当前仍需继续进行各玩法、GUI、结构外观和特效的人工验证。

## 十九、迁移原则

1. 保持原玩法、数据编码和存档兼容逻辑，除非新版本结构不允许。
2. 不凭猜测写 API，必须查指定源码。
3. 客户端类不能在服务端加载。
4. Forge 事件优先，Mixin 仅用于没有官方钩子的行为。
5. 热路径避免重复对象创建、反射和字符串处理。
6. 通用坐标、颜色、NBT 和数学逻辑统一复用。
7. 面向玩家的文字进入语言文件。
8. 每完成一个模块，更新本文档并运行编译验证。
- 方块模型导出已增加完整 Blockbench 工程：拆解台和光剑锻造台现在会展开 `parent`、合并多方块部件、内嵌模组及 Minecraft 1.20.1 贴图，并写入 Java Block/Item `display` 变换；原始分件 JSON 仍保留在 `block_json/` 供核对。
- 棺材等 `modded_entity` 工程现在也带有 `display`、`animations` 与 `animation_controllers` 根字段，在保留实体动画编辑能力的同时记录手持、GUI、掉落物和展示框变换。
