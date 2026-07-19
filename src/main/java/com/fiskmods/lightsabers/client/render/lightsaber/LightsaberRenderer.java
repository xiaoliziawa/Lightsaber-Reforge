package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.common.hilt.Hilt.Part;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

public final class LightsaberRenderer {
    private static final float BLADE_MODEL_SCALE = 3.0F;
    private static final float BLADE_BASE_OFFSET = 0.095F;

    private LightsaberRenderer() {
    }

    public static float getMainBladeModelLength() {
        return LightsaberBladeRenderer.getBladeLength(
                LightsaberBladeRenderer.MAIN_BLADE_LENGTH
        ) * BLADE_MODEL_SCALE;
    }

    public static void render(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            boolean inWorld
    ) {
        if (SpinningLightsaberObjRenderer.isSupported(data)) {
            SpinningLightsaberObjRenderer.renderHilt(
                    data,
                    stack,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            if (ItemLightsaberBase.isActive(stack)) {
                SpinningLightsaberObjRenderer.renderBlades(
                        data,
                        stack,
                        poseStack,
                        buffer,
                        inWorld
                );
            }
            return;
        }
        HiltModelRenderer.render(data, poseStack, buffer, packedLight, packedOverlay);
        if (!ItemLightsaberBase.isActive(stack)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(
                0.0F,
                -(data.getPart(PartType.BODY).height
                        + data.getPart(PartType.POMMEL).height
                        - data.getHeight() / 2.0F) / 16.0F,
                0.0F
        );
        poseStack.scale(BLADE_MODEL_SCALE, BLADE_MODEL_SCALE, BLADE_MODEL_SCALE);
        poseStack.translate(
                0.0F,
                -(data.getPart(PartType.SWITCH_SECTION).height
                        + data.getPart(PartType.EMITTER).height) * 0.0234375F,
                0.0F
        );
        poseStack.translate(0.0F, BLADE_BASE_OFFSET, 0.0F);
        renderCrossguard(data, stack, poseStack, buffer, inWorld);
        LightsaberBladeRenderer.render(
                data,
                stack,
                poseStack,
                buffer,
                inWorld,
                LightsaberBladeRenderer.MAIN_BLADE_LENGTH,
                false
        );
        poseStack.popPose();
    }

    public static void render(
            LightsaberData[] sabers,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            boolean inWorld
    ) {
        boolean flipped = ItemDoubleLightsaber.isFlipped(stack);
        for (int index = 0; index < sabers.length; index++) {
            int dataIndex = flipped ? sabers.length - index - 1 : index;
            LightsaberData data = sabers[dataIndex];
            poseStack.pushPose();
            if (index == 1) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            poseStack.translate(0.0F, -data.getHeight() / 32.0F, 0.0F);
            render(
                    data,
                    stack,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    inWorld
            );
            poseStack.popPose();
        }
    }

    private static void renderCrossguard(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            boolean inWorld
    ) {
        Part emitter = data.getPart(PartType.EMITTER);
        if (!emitter.hasCrossguard()) {
            return;
        }

        float[] crossguard = emitter.crossguard;
        for (int direction = -1; direction <= 1; direction += 2) {
            poseStack.pushPose();
            poseStack.translate(
                    crossguard[0],
                    crossguard[1],
                    crossguard[2] * -direction
            );
            poseStack.mulPose(Axis.XP.rotationDegrees(direction * 90.0F));
            if (direction == 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            LightsaberBladeRenderer.render(
                    data,
                    stack,
                    poseStack,
                    buffer,
                    inWorld,
                    LightsaberBladeRenderer.CROSSGUARD_BLADE_LENGTH,
                    true
            );
            poseStack.popPose();
        }
    }
}
