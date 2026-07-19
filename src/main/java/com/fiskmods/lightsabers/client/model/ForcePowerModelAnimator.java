package com.fiskmods.lightsabers.client.model;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class ForcePowerModelAnimator {
    private static final int CHOKE_DURATION_TICKS = 60;
    private static final float CHOKE_FADE_TICKS = 3.0F;
    private static final float ARM_RAISE_SPEED = 3.0F;
    private static final float DRAIN_RAISE_SPEED = 3.0F;

    private ForcePowerModelAnimator() {
    }

    public static void apply(
            HumanoidModel<?> model,
            LivingEntity entity,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        applyCasterPose(model, entity, netHeadYaw, headPitch);
        applyChokeTargetPose(model, entity, ageInTicks, partialTick);
    }

    private static void applyCasterPose(
            HumanoidModel<?> model,
            LivingEntity entity,
            float netHeadYaw,
            float headPitch
    ) {
        float drainTimer = ALData.DRAIN_LIFE_TIMER.interpolate(entity);
        float drain = Mth.clamp(
                Mth.sin(drainTimer * DRAIN_RAISE_SPEED) * 4.0F,
                0.0F,
                1.0F
        );
        float right = Mth.clamp(ALData.RIGHT_ARM_TIMER.interpolate(entity), 0.0F, 1.0F);
        float left = Mth.clamp(ALData.LEFT_ARM_TIMER.interpolate(entity), 0.0F, 1.0F);
        applyRaisedArm(model.rightArm, Math.max(drain, right), netHeadYaw, headPitch);
        applyRaisedArm(model.leftArm, left, netHeadYaw, headPitch);
    }

    private static void applyRaisedArm(
            ModelPart arm,
            float amount,
            float netHeadYaw,
            float headPitch
    ) {
        if (amount <= 0.0F) {
            return;
        }
        float targetX = headPitch * Mth.DEG_TO_RAD - Mth.HALF_PI;
        float targetY = netHeadYaw * Mth.DEG_TO_RAD;
        arm.xRot = Mth.lerp(amount, arm.xRot, targetX);
        arm.yRot = Mth.lerp(amount, arm.yRot, targetY);
        arm.zRot = Mth.lerp(amount, arm.zRot, 0.0F);
    }

    private static void applyChokeTargetPose(
            HumanoidModel<?> model,
            LivingEntity entity,
            float ageInTicks,
            float partialTick
    ) {
        StatusEffect choke = StatusEffect.get(entity, Effect.CHOKE);
        if (choke == null) {
            return;
        }

        float duration = choke.duration - partialTick;
        float fadeIn = Math.min(CHOKE_DURATION_TICKS - duration, CHOKE_FADE_TICKS)
                / CHOKE_FADE_TICKS;
        float fadeOut = Math.min(duration, CHOKE_FADE_TICKS) / CHOKE_FADE_TICKS;
        float amount = Mth.clamp(Math.min(fadeIn, fadeOut), 0.0F, 1.0F);
        if (choke.amplifier == 0) {
            applyWoundPose(model, amount);
        } else if (choke.amplifier == 1) {
            applyChokePose(model, ageInTicks, amount);
        } else {
            applyTormentPose(model, ageInTicks, amount);
        }
    }

    private static void applyWoundPose(HumanoidModel<?> model, float amount) {
        weakenPart(model.leftArm, amount);
        model.leftArm.xRot -= 1.2F * amount;
        model.leftArm.yRot += 1.3F * amount;
        model.leftArm.x += 5.0F * amount;
        model.leftArm.y += 2.0F * amount;
    }

    private static void applyChokePose(
            HumanoidModel<?> model,
            float ageInTicks,
            float amount
    ) {
        weakenUpperBody(model, amount);
        weakenLeg(model.rightLeg, amount);
        weakenLeg(model.leftLeg, amount);

        model.head.xRot -= 0.5F * amount;
        model.hat.xRot -= 0.5F * amount;
        poseArm(model.rightArm, -2.1F, -1.3F, 0.0F, -6.0F, 3.5F, 1.0F, amount);
        poseArm(model.leftArm, -2.1F, 1.3F, 0.0F, 6.0F, 3.5F, 1.0F, amount);
        model.rightLeg.y += 12.0F * amount;
        model.leftLeg.y += 12.0F * amount;
        model.rightLeg.xRot = Mth.cos(ageInTicks * 0.6662F) * 1.1F * amount;
        model.leftLeg.xRot = Mth.cos(ageInTicks * 0.6662F + Mth.PI) * 1.1F * amount;
    }

    private static void applyTormentPose(
            HumanoidModel<?> model,
            float ageInTicks,
            float amount
    ) {
        weakenUpperBody(model, amount);
        weakenLeg(model.rightLeg, amount);
        weakenLeg(model.leftLeg, amount);

        float headShake = Mth.cos(ageInTicks * ARM_RAISE_SPEED) * 0.2F * amount;
        model.head.xRot = -0.8F * amount;
        model.hat.xRot = -0.8F * amount;
        model.head.yRot = headShake;
        model.hat.yRot = headShake;
        poseArm(model.rightArm, -3.5F, -0.1F, 0.3F, -5.25F, 3.0F, 0.0F, amount);
        poseArm(model.leftArm, -3.5F, 0.1F, -0.3F, 5.25F, 3.0F, 0.0F, amount);
        model.rightLeg.y += 12.0F * amount;
        model.leftLeg.y += 12.0F * amount;
        model.rightLeg.xRot = Mth.cos(ageInTicks * 0.8662F) * 1.1F * amount;
        model.leftLeg.xRot = Mth.cos(ageInTicks * 0.8662F + Mth.PI) * 1.1F * amount;
    }

    private static void weakenUpperBody(HumanoidModel<?> model, float amount) {
        weakenPosition(model.head, amount);
        weakenPosition(model.hat, amount);
        weakenPart(model.body, amount);
        weakenPart(model.rightArm, amount);
        weakenPart(model.leftArm, amount);
    }

    private static void weakenPosition(ModelPart part, float amount) {
        float remaining = 1.0F - amount;
        part.x *= remaining;
        part.y *= remaining;
        part.z *= remaining;
    }

    private static void weakenPart(ModelPart part, float amount) {
        float remaining = 1.0F - amount;
        part.x *= remaining;
        part.y *= remaining;
        part.z *= remaining;
        part.xRot *= remaining;
        part.yRot *= remaining;
        part.zRot *= remaining;
    }

    private static void weakenLeg(ModelPart leg, float amount) {
        float remaining = 1.0F - amount;
        leg.y *= remaining;
        leg.z *= remaining;
        leg.xRot *= remaining;
        leg.yRot *= remaining;
        leg.zRot *= remaining;
    }

    private static void poseArm(
            ModelPart arm,
            float xRot,
            float yRot,
            float zRot,
            float x,
            float y,
            float z,
            float amount
    ) {
        arm.xRot += xRot * amount;
        arm.yRot += yRot * amount;
        arm.zRot += zRot * amount;
        arm.x += x * amount;
        arm.y += y * amount;
        arm.z += z * amount;
    }
}
