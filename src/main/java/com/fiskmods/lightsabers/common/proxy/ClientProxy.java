package com.fiskmods.lightsabers.common.proxy;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.command.CommandExportIcon;
import com.fiskmods.lightsabers.client.gui.GuiCrystalPouch;
import com.fiskmods.lightsabers.client.gui.GuiLightsaberForge;
import com.fiskmods.lightsabers.client.gui.GuiDisassemblyStation;
import com.fiskmods.lightsabers.client.gui.GuiForcePowers;
import com.fiskmods.lightsabers.client.gui.GuiSithCoffin;
import com.fiskmods.lightsabers.client.gui.GuiOverlay;
import com.fiskmods.lightsabers.client.input.ALKeyMappings;
import com.fiskmods.lightsabers.client.input.ClientInputHandler;
import com.fiskmods.lightsabers.client.model.ModelSithGhost;
import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.client.model.tile.ModelSithCoffin;
import com.fiskmods.lightsabers.client.model.tile.ModelSithStoneCoffin;
import com.fiskmods.lightsabers.client.render.entity.RenderSithGhost;
import com.fiskmods.lightsabers.client.render.entity.RenderLightsaber;
import com.fiskmods.lightsabers.client.render.entity.RenderForceLightning;
import com.fiskmods.lightsabers.client.render.entity.ClientForceEffectRenderer;
import com.fiskmods.lightsabers.client.render.HolocronObjRenderer;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.DeferredGlowRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberRenderTypes;
import com.fiskmods.lightsabers.client.render.lightsaber.SpearLightsaberObjRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.SpinningLightsaberObjRenderer;
import com.fiskmods.lightsabers.client.render.item.LightsaberItemDecorator;
import com.fiskmods.lightsabers.client.render.item.CrystalClientItemExtensions;
import com.fiskmods.lightsabers.client.render.item.CrystalColorTintSource;
import com.fiskmods.lightsabers.client.render.item.CrystalItemRenderer;
import com.fiskmods.lightsabers.client.render.item.HolocronClientItemExtensions;
import com.fiskmods.lightsabers.client.render.item.HolocronItemRenderer;
import com.fiskmods.lightsabers.client.render.item.LightsaberClientItemExtensions;
import com.fiskmods.lightsabers.client.render.item.LightsaberItemRenderer;
import com.fiskmods.lightsabers.client.render.tile.RenderCrystal;
import com.fiskmods.lightsabers.client.render.tile.RenderLightsaberStand;
import com.fiskmods.lightsabers.client.render.tile.RenderCrystalDisplayStand;
import com.fiskmods.lightsabers.client.render.tile.RenderHolocron;
import com.fiskmods.lightsabers.client.render.tile.RenderSithCoffin;
import com.fiskmods.lightsabers.client.render.tile.RenderSithStoneCoffin;
import com.fiskmods.lightsabers.client.sound.MovingSoundLightning;
import com.fiskmods.lightsabers.client.sound.MovingSoundStatusEffect;
import com.fiskmods.lightsabers.client.sound.ClientSoundHandler;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.container.ModMenus;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.entity.ModEntities;
import com.fiskmods.lightsabers.common.event.ClientEventHandler;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystal;
import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

public final class ClientProxy extends CommonProxy {
    private static final int OPAQUE_WHITE = 0xFFFFFFFF;
    private static final BlockTintSource CRYSTAL_BLOCK_TINT = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return OPAQUE_WHITE;
        }

        @Override
        public int colorInWorld(
                BlockState state,
                BlockAndTintGetter level,
                BlockPos pos
        ) {
            return level.getBlockEntity(pos) instanceof TileEntityCrystal crystal
                    ? ARGB.opaque(crystal.getColor().getRenderColor())
                    : OPAQUE_WHITE;
        }
    };

    @Override
    public void registerModEvents(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(LightsaberRenderTypes::registerPipelines);
        modEventBus.addListener(HolocronObjRenderer::registerModels);
        modEventBus.addListener(SpinningLightsaberObjRenderer::registerModels);
        modEventBus.addListener(SpearLightsaberObjRenderer::registerModels);
        modEventBus.addListener(this::registerItemModels);
        modEventBus.addListener(this::registerItemTintSources);
        modEventBus.addListener(this::registerBlockTintSources);
        modEventBus.addListener(this::registerItemDecorations);
        modEventBus.addListener(this::registerClientExtensions);
        modEventBus.addListener(ClientEventHandler::registerRenderStateModifiers);
        modEventBus.addListener(this::registerMenuScreens);
        modEventBus.addListener(ALKeyMappings::register);
        NeoForge.EVENT_BUS.register(ClientInputHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(ClientSoundHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(ClientForceEffectRenderer.INSTANCE);
        NeoForge.EVENT_BUS.register(DeferredGlowRenderer.INSTANCE);
        NeoForge.EVENT_BUS.register(new GuiOverlay());
        NeoForge.EVENT_BUS.register(new ClientEventHandler());
        NeoForge.EVENT_BUS.register(new CommandExportIcon());
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public float getRenderTick() {
        return Minecraft.getInstance()
                .getDeltaTracker()
                .getGameTimeDeltaPartialTick(true);
    }

    @Override
    public Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public boolean isClientPlayer(LivingEntity entity) {
        return entity == Minecraft.getInstance().player;
    }

    @Override
    public Iterable<Entity> getLoadedEntities(Level level) {
        return level instanceof ClientLevel clientLevel
                ? clientLevel.entitiesForRendering()
                : super.getLoadedEntities(level);
    }

    @Override
    public void playStatusEffectSound(Player player, Effect effect, String soundName) {
        Minecraft.getInstance().getSoundManager().play(
                new MovingSoundStatusEffect(player, effect, soundName)
        );
    }

    @Override
    public void playLightningSound(Player player) {
        Minecraft.getInstance().getSoundManager().play(new MovingSoundLightning(player));
    }

    @Override
    public void playLocalSound(Player player, String soundName, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                Identifier.parse(soundName),
                SoundSource.PLAYERS,
                volume,
                pitch,
                RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.LINEAR,
                player.getX(),
                player.getY(),
                player.getZ(),
                false
        ));
    }

    @Override
    public void spawnHealParticles(LivingEntity entity) {
        RandomSource random = entity.getRandom();
        for (int i = 0; i < 16; i++) {
            entity.level().addParticle(
                    ParticleTypes.HAPPY_VILLAGER,
                    entity.getX() + (random.nextFloat() * 2 - 1) * entity.getBbWidth(),
                    entity.getBoundingBox().minY
                            + entity.getBbHeight() / 3
                            + entity.getBbHeight() / 3 * 2 * random.nextFloat(),
                    entity.getZ() + (random.nextFloat() * 2 - 1) * entity.getBbWidth(),
                    0,
                    0,
                    0
            );
        }
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.LIGHTSABER.get(),
                RenderLightsaber::new
        );
        event.registerEntityRenderer(ModEntities.SITH_GHOST.get(), RenderSithGhost::new);
        event.registerEntityRenderer(
                ModEntities.FORCE_LIGHTNING.get(),
                RenderForceLightning::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.CRYSTAL.get(),
                RenderCrystal::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.LIGHTSABER_STAND.get(),
                RenderLightsaberStand::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.CRYSTAL_DISPLAY_STAND.get(),
                RenderCrystalDisplayStand::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.HOLOCRON.get(),
                RenderHolocron::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.SITH_COFFIN.get(),
                RenderSithCoffin::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.SITH_STONE_COFFIN.get(),
                RenderSithStoneCoffin::new
        );
    }

    private void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModelCrystal.LAYER, ModelCrystal::createBodyLayer);
        event.registerLayerDefinition(ModelSithGhost.LAYER, ModelSithGhost::createBodyLayer);
        event.registerLayerDefinition(ModelSithCoffin.LAYER, ModelSithCoffin::createBodyLayer);
        event.registerLayerDefinition(
                ModelSithStoneCoffin.LAYER,
                ModelSithStoneCoffin::createBodyLayer
        );
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            HiltModelRenderer.registerModels();
        });
    }

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CRYSTAL_POUCH.get(), GuiCrystalPouch::new);
        event.register(ModMenus.LIGHTSABER_FORGE.get(), GuiLightsaberForge::new);
        event.register(ModMenus.DISASSEMBLY_STATION.get(), GuiDisassemblyStation::new);
        event.register(ModMenus.HOLOCRON.get(), GuiForcePowers::new);
        event.register(ModMenus.SITH_COFFIN.get(), GuiSithCoffin::new);
    }

    private void registerItemModels(RegisterItemModelsEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(Lightsabers.MODID, "lightsaber"),
                LightsaberItemRenderer.Unbaked.MAP_CODEC
        );
        event.register(
                Identifier.fromNamespaceAndPath(Lightsabers.MODID, "crystal"),
                CrystalItemRenderer.Unbaked.MAP_CODEC
        );
        event.register(
                Identifier.fromNamespaceAndPath(Lightsabers.MODID, "holocron"),
                HolocronItemRenderer.Unbaked.MAP_CODEC
        );
    }

    private void registerItemDecorations(RegisterItemDecorationsEvent event) {
        LightsaberItemDecorator decorator = new LightsaberItemDecorator();
        event.register(ModItems.LIGHTSABER.get(), decorator);
        event.register(ModItems.DOUBLE_LIGHTSABER.get(), decorator);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                LightsaberClientItemExtensions.INSTANCE,
                ModItems.LIGHTSABER.get(),
                ModItems.DOUBLE_LIGHTSABER.get(),
                ModItems.EMITTER.get(),
                ModItems.SWITCH_SECTION.get(),
                ModItems.GRIP.get(),
                ModItems.POMMEL.get()
        );
        event.registerItem(
                CrystalClientItemExtensions.INSTANCE,
                ModBlocks.LIGHTSABER_CRYSTAL_BLOCK_ITEM.get()
        );
        event.registerItem(
                HolocronClientItemExtensions.INSTANCE,
                ModBlocks.JEDI_HOLOCRON_ITEM.get(),
                ModBlocks.HOLOCRON_ITEM.get()
        );
    }

    private void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(
                Identifier.fromNamespaceAndPath(Lightsabers.MODID, "crystal_color"),
                CrystalColorTintSource.MAP_CODEC
        );
    }

    private void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(
                List.of(CRYSTAL_BLOCK_TINT),
                ModBlocks.LIGHTSABER_CRYSTAL.get()
        );
    }
}
