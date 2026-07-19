package com.fiskmods.lightsabers.common.proxy;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.command.CommandExportIcon;
import com.fiskmods.lightsabers.client.gui.GuiCrystalPouch;
import com.fiskmods.lightsabers.client.gui.GuiLightsaberForge;
import com.fiskmods.lightsabers.client.gui.GuiDisassemblyStation;
import com.fiskmods.lightsabers.client.gui.GuiForcePowers;
import com.fiskmods.lightsabers.client.gui.GuiSithCoffin;
import com.fiskmods.lightsabers.client.gui.GuiOverlay;
import com.fiskmods.lightsabers.client.integration.epicfight.EpicFightClientIntegration;
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
import com.fiskmods.lightsabers.client.render.lightsaber.SpinningLightsaberObjRenderer;
import com.fiskmods.lightsabers.client.render.item.LightsaberItemDecorator;
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
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemForcestone;
import com.fiskmods.lightsabers.common.item.ItemForcestoneSlab;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystal;
import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientProxy extends CommonProxy {
    private static final ResourceLocation FORCESTONE_VARIANT_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, "forcestone_variant");
    private static final ResourceLocation FORCESTONE_SLAB_VARIANT_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, "forcestone_slab_variant");

    @Override
    public void registerModEvents(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(HolocronObjRenderer::registerModels);
        modEventBus.addListener(SpinningLightsaberObjRenderer::registerModels);
        modEventBus.addListener(this::registerItemColors);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemDecorations);
        modEventBus.addListener(ALKeyMappings::register);
        MinecraftForge.EVENT_BUS.register(ClientInputHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ClientSoundHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ClientForceEffectRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new GuiOverlay());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new CommandExportIcon());
        if (Lightsabers.isEpicFightLoaded) {
            EpicFightClientIntegration.register();
        }
    }

    @Override
    public Dist getSide() {
        return Dist.CLIENT;
    }

    @Override
    public float getRenderTick() {
        return Minecraft.getInstance().getFrameTime();
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
                ResourceLocation.parse(soundName),
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
            MenuScreens.register(ModMenus.CRYSTAL_POUCH.get(), GuiCrystalPouch::new);
            MenuScreens.register(ModMenus.LIGHTSABER_FORGE.get(), GuiLightsaberForge::new);
            MenuScreens.register(
                    ModMenus.DISASSEMBLY_STATION.get(),
                    GuiDisassemblyStation::new
            );
            MenuScreens.register(ModMenus.HOLOCRON.get(), GuiForcePowers::new);
            MenuScreens.register(ModMenus.SITH_COFFIN.get(), GuiSithCoffin::new);
            HiltModelRenderer.registerModels();
            registerItemProperties();
        });
    }

    private void registerItemProperties() {
        ItemProperties.register(
                ModBlocks.LIGHT_FORCESTONE_ITEM.get(),
                FORCESTONE_VARIANT_PROPERTY,
                (stack, level, entity, seed) -> ItemForcestone.getModelPropertyValue(stack)
        );
        ItemProperties.register(
                ModBlocks.DARK_FORCESTONE_ITEM.get(),
                FORCESTONE_VARIANT_PROPERTY,
                (stack, level, entity, seed) -> ItemForcestone.getModelPropertyValue(stack)
        );
        ItemProperties.register(
                ModBlocks.FORCESTONE_SLAB_ITEM.get(),
                FORCESTONE_SLAB_VARIANT_PROPERTY,
                (stack, level, entity, seed) ->
                        ItemForcestoneSlab.getModelPropertyValue(stack)
        );
    }

    private void registerItemDecorations(RegisterItemDecorationsEvent event) {
        LightsaberItemDecorator decorator = new LightsaberItemDecorator();
        event.register(ModItems.LIGHTSABER.get(), decorator);
        event.register(ModItems.DOUBLE_LIGHTSABER.get(), decorator);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> tintIndex == 0
                        ? ItemCrystal.get(stack).getRenderColor()
                        : 0xFFFFFF,
                ModBlocks.LIGHTSABER_CRYSTAL_ITEM.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? ItemCrystal.get(stack).getRenderColor()
                        : 0xFFFFFF,
                ModItems.CRYSTAL_POUCH.get()
        );
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> {
                    if (level != null
                            && pos != null
                            && level.getBlockEntity(pos) instanceof TileEntityCrystal crystal) {
                        return crystal.getColor().getRenderColor();
                    }
                    return 0xFFFFFF;
                },
                ModBlocks.LIGHTSABER_CRYSTAL.get()
        );
    }
}
