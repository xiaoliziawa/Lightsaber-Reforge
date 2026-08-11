package com.fiskmods.lightsabers.common.event;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.input.ALKeyMappings;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.ALPlayerData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerType;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectActive;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.fiskmods.lightsabers.helper.ALHelper;
import com.fiskmods.lightsabers.helper.ALRenderHelper;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public final class ClientEventHandler {
    private static final ContextKey<LivingEntity> RENDERED_ENTITY_KEY =
            new ContextKey<>(Identifier.fromNamespaceAndPath(
                    Lightsabers.MODID,
                    "rendered_entity"
            ));

    public static void registerRenderStateModifiers(
            RegisterRenderStateModifiersEvent event
    ) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<
                        LivingEntity,
                        LivingEntityRenderState,
                        ?
                >>() {
                },
                ClientEventHandler::extractLivingRenderData
        );
    }

    private static void extractLivingRenderData(
            LivingEntity entity,
            LivingEntityRenderState renderState
    ) {
        renderState.setRenderData(RENDERED_ENTITY_KEY, entity);
        if (entity instanceof Player
                && renderState instanceof HumanoidRenderState humanoidState
                && StatusEffect.has(entity, Effect.STEALTH)) {
            humanoidState.headEquipment = ItemStack.EMPTY;
            humanoidState.chestEquipment = ItemStack.EMPTY;
            humanoidState.legsEquipment = ItemStack.EMPTY;
            humanoidState.feetEquipment = ItemStack.EMPTY;
        }
    }

    public static LivingEntity getRenderedEntity(
            LivingEntityRenderState renderState
    ) {
        return renderState.getRenderData(RENDERED_ENTITY_KEY);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null
                || !player.isAlive()
                || player.isRemoved()
                || !ALPlayerData.hasData(player)
                || !ALEntityData.hasData(player)) {
            resetClientEffects(minecraft);
            return;
        }
        updateStoredLightsaber(player);
        updateContinuousPower(minecraft, player);
        updatePostEffect(minecraft, player);
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        playLightsaberSwingSound(event.getEntity());
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        playLightsaberSwingSound(event.getEntity());
    }

    private static void playLightsaberSwingSound(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemLightsaberBase
                && ItemLightsaberBase.isActive(stack)
                && Lightsabers.proxy.isClientPlayer(player)) {
            Lightsabers.proxy.playLocalSound(
                    player,
                    ALSounds.player_lightsaber_swing,
                    1.0F,
                    1.0F
            );
        }
    }

    private static void updateStoredLightsaber(LocalPlayer player) {
        ItemStack lightsaber = ItemStack.EMPTY;
        if (player.getOffhandItem().is(ModItems.lightsaber)) {
            lightsaber = player.getOffhandItem();
        } else {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.is(ModItems.lightsaber)) {
                    lightsaber = stack;
                    break;
                }
            }
        }
        ALData.LIGHTSABER.set(player, LightsaberData.get(lightsaber));
    }

    private static void updateContinuousPower(Minecraft minecraft, LocalPlayer player) {
        Power power = PowerManager.getSelectedPower(player);
        boolean using = minecraft.screen == null
                && ALKeyMappings.ACTIVATE_POWER.isDown()
                && power != null
                && PowerManager.hasPowerUnlocked(player, power)
                && power.powerStats.powerType == PowerType.PER_SECOND
                && ALData.USE_POWER_COOLDOWN.get(player) == 0
                && ALHelper.getForcePowerMax(player) > 0
                && ALDataInterp.FORCE_POWER.get(player) >= power.getUseCost(player);
        if (!using && ALData.USING_POWER.get(player)) {
            ALData.USE_POWER_COOLDOWN.set(player, ALConstants.FORCE_POWER_COOLDOWN);
        }
        ALData.USING_POWER.set(player, using);
    }

    private static void updatePostEffect(Minecraft minecraft, LocalPlayer player) {
        StatusEffect gaze = StatusEffect.get(player, Effect.GAZE);
        ALRenderHelper.setGazeAmplifier(gaze == null ? -1 : gaze.amplifier);

        Identifier requested = null;
        if (ModConfig.enableShaders && gaze != null) {
            requested = ALRenderHelper.SHADER_BLUE;
        } else if (ModConfig.enableShaders && StatusEffect.has(player, Effect.STEALTH)) {
            requested = ALRenderHelper.SHADER_GRAY;
        } else if (ModConfig.enableShaders && StatusEffect.has(player, Effect.SPEED)) {
            requested = ALRenderHelper.SHADER_BLUR;
        }

        if (requested != null) {
            ALRenderHelper.startShaders(requested);
            return;
        }
        stopPostEffect(minecraft);
    }

    private static void resetClientEffects(Minecraft minecraft) {
        ALRenderHelper.setGazeAmplifier(-1);
        stopPostEffect(minecraft);
    }

    private static void stopPostEffect(Minecraft minecraft) {
        Identifier active = minecraft.gameRenderer.currentPostEffect();
        if (active != null) {
            if (active.equals(ALRenderHelper.SHADER_BLUE)
                    || active.equals(ALRenderHelper.SHADER_GRAY)
                    || active.equals(ALRenderHelper.SHADER_BLUR)) {
                ALRenderHelper.stopShaders();
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post<?> event) {
        LivingEntity renderedEntity = event.getRenderState()
                .getRenderData(RENDERED_ENTITY_KEY);
        if (!(renderedEntity instanceof Player player)) {
            return;
        }
        if (!StatusEffect.has(player, Effect.STEALTH)) {
            renderStoredLightsaber(event, player);
        }
        for (StatusEffect status : StatusEffect.get(player)) {
            Power power = status.effect.getPower(status.amplifier);
            if (power != null && power.powerEffect instanceof PowerEffectActive active) {
                active.render(player, event.getPartialTick());
            }
        }
    }

    private static final float BELT_OFFSET_X = 0.2F;
    private static final float BELT_OFFSET_Y = -0.55F;
    private static final float BELT_OFFSET_Z = 0.15F;
    private static final float BELT_ROT_Z = 15.0F;
    private static final float BELT_ROT_X = 10.0F;
    private static final float BELT_SCALE = 0.15F;

    private static void renderStoredLightsaber(
            RenderPlayerEvent.Post<?> event,
            Player player
    ) {
        LightsaberData data = ALData.LIGHTSABER.get(player);
        if (data == null
                || data == LightsaberData.EMPTY
                || player.getMainHandItem().is(ModItems.lightsaber)
                || player.getOffhandItem().is(ModItems.lightsaber)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(
                180.0F - event.getRenderState().bodyRot
        ));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        event.getRenderer().getModel().body.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(BELT_OFFSET_X, BELT_OFFSET_Y, BELT_OFFSET_Z);
        poseStack.mulPose(Axis.ZP.rotationDegrees(BELT_ROT_Z));
        poseStack.mulPose(Axis.XP.rotationDegrees(BELT_ROT_X));
        poseStack.scale(BELT_SCALE, BELT_SCALE, BELT_SCALE);
        poseStack.translate(
                0.0F,
                -(data.getPart(PartType.BODY).height
                        + data.getPart(PartType.POMMEL).height / 2.0F) / 16.0F,
                0.0F
        );
        HiltModelRenderer.render(
                data,
                poseStack,
                event.getSubmitNodeCollector(),
                event.getRenderState().lightCoords,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderLivingPre(RenderLivingEvent.Pre<?, ?, ?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer viewer = minecraft.player;
        LivingEntity entity = event.getRenderState()
                .getRenderData(RENDERED_ENTITY_KEY);
        if (entity == null) {
            return;
        }
        if (viewer == null || entity == viewer) {
            return;
        }

        StatusEffect stealth = StatusEffect.get(entity, Effect.STEALTH);
        if (stealth == null) {
            return;
        }
        StatusEffect gaze = StatusEffect.get(viewer, Effect.GAZE);
        if (gaze == null || !ALRenderHelper.canGazeEntity(viewer, entity, gaze.amplifier)) {
            event.setCanceled(true);
        }
    }
}
