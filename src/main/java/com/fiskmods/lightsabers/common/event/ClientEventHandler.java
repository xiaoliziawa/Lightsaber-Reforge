package com.fiskmods.lightsabers.common.event;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.client.input.ALKeyMappings;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.ALPlayerData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerType;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectActive;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.fiskmods.lightsabers.helper.ALHelper;
import com.fiskmods.lightsabers.helper.ALRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientEventHandler {
    private static final ItemStack[] STASHED_ARMOR = new ItemStack[4];
    private static boolean armorHidden;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
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

    private static void updateStoredLightsaber(LocalPlayer player) {
        ItemStack lightsaber = ItemStack.EMPTY;
        if (player.getOffhandItem().is(ModItems.lightsaber)) {
            lightsaber = player.getOffhandItem();
        } else {
            for (ItemStack stack : player.getInventory().items) {
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
                && ALData.FORCE_POWER.get(player) >= power.getUseCost(player);
        if (!using && ALData.USING_POWER.get(player)) {
            ALData.USE_POWER_COOLDOWN.set(player, ALConstants.FORCE_POWER_COOLDOWN);
        }
        ALData.USING_POWER.set(player, using);
    }

    private static void updatePostEffect(Minecraft minecraft, LocalPlayer player) {
        StatusEffect gaze = StatusEffect.get(player, Effect.GAZE);
        ALRenderHelper.setGazeAmplifier(gaze == null ? -1 : gaze.amplifier);

        ResourceLocation requested = null;
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
        if (minecraft.gameRenderer.currentEffect() != null) {
            String active = minecraft.gameRenderer.currentEffect().getName();
            if (active.equals(ALRenderHelper.SHADER_BLUE.toString())
                    || active.equals(ALRenderHelper.SHADER_GRAY.toString())
                    || active.equals(ALRenderHelper.SHADER_BLUR.toString())) {
                ALRenderHelper.stopShaders();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!StatusEffect.has(player, Effect.STEALTH)) {
            return;
        }

        NonNullList<ItemStack> armor = player.getInventory().armor;
        for (int slot = 0; slot < armor.size(); slot++) {
            STASHED_ARMOR[slot] = armor.get(slot);
            armor.set(slot, ItemStack.EMPTY);
        }
        armorHidden = true;
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        restoreHiddenArmor(player);
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

    private static void restoreHiddenArmor(Player player) {
        if (!armorHidden) {
            return;
        }
        NonNullList<ItemStack> armor = player.getInventory().armor;
        for (int slot = 0; slot < armor.size(); slot++) {
            armor.set(slot, STASHED_ARMOR[slot]);
            STASHED_ARMOR[slot] = ItemStack.EMPTY;
        }
        armorHidden = false;
    }

    private static final float BELT_OFFSET_X = 0.2F;
    private static final float BELT_OFFSET_Y = -0.55F;
    private static final float BELT_OFFSET_Z = 0.15F;
    private static final float BELT_ROT_Z = 15.0F;
    private static final float BELT_ROT_X = 10.0F;
    private static final float BELT_SCALE = 0.15F;

    private static void renderStoredLightsaber(RenderPlayerEvent.Post event, Player player) {
        LightsaberData data = ALData.LIGHTSABER.get(player);
        if (data == null
                || data == LightsaberData.EMPTY
                || player.getMainHandItem().is(ModItems.lightsaber)
                || player.getOffhandItem().is(ModItems.lightsaber)) {
            return;
        }

        float bodyYaw = Mth.rotLerp(
                event.getPartialTick(),
                player.yBodyRotO,
                player.yBodyRot
        );
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
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
                event.getMultiBufferSource(),
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer viewer = minecraft.player;
        LivingEntity entity = event.getEntity();
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
