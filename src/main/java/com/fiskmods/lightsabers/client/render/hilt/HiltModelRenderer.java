package com.fiskmods.lightsabers.client.render.hilt;

import com.fiskmods.lightsabers.client.model.legacy.LegacyModelBase;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.hilt.Hilt.Part;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public final class HiltModelRenderer {
    private static boolean registered;

    private HiltModelRenderer() {
    }

    public static void registerModels() {
        if (!registered) {
            HiltRendererManager.register();
            registered = true;
        }
    }

    public static void render(
            LightsaberData data,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        registerModels();
        poseStack.pushPose();
        poseStack.translate(
                0.0F,
                -(data.getPart(PartType.BODY).height
                        + data.getPart(PartType.POMMEL).height
                        - data.getHeight() / 2.0F) / 16.0F,
                0.0F
        );

        for (PartType type : PartType.values()) {
            HiltRenderer renderer = HiltRenderer.get(data.get(type));
            if (renderer == null) {
                continue;
            }

            poseStack.pushPose();
            applyPartTransform(data, type, poseStack);
            renderModel(
                    renderer,
                    type,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static void render(
            LightsaberData[] sabers,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        for (int index = 0; index < sabers.length; index++) {
            LightsaberData data = sabers[index];
            poseStack.pushPose();
            if (index == 1) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
            poseStack.translate(0.0F, -data.getHeight() / 32.0F, 0.0F);
            render(data, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    public static void renderPart(
            Hilt hilt,
            PartType type,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        registerModels();
        HiltRenderer renderer = HiltRenderer.get(hilt);
        if (renderer == null) {
            return;
        }
        Part part = hilt.getPart(type);
        float direction = type.isLowerPart() ? -1.0F : 1.0F;
        poseStack.translate(0.0F, part.height * direction / 32.0F, 0.0F);
        renderModel(renderer, type, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void applyPartTransform(
            LightsaberData data,
            PartType type,
            PoseStack poseStack
    ) {
        if (type == PartType.EMITTER) {
            poseStack.translate(
                    0.0F,
                    -data.getPart(PartType.SWITCH_SECTION).height / 16.0F,
                    0.0F
            );
            return;
        }
        if (type != PartType.POMMEL) {
            return;
        }

        Part body = data.getPart(PartType.BODY);
        float[] instructions = body.glInstructions;
        if (instructions == null || instructions.length == 0) {
            poseStack.translate(0.0F, body.height / 16.0F, 0.0F);
            return;
        }
        for (int index = 0; index < instructions.length; index++) {
            float instruction = instructions[index];
            if ((index & 1) == 0) {
                poseStack.translate(0.0F, instruction / 16.0F, 0.0F);
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(instruction));
            }
        }
    }

    private static void renderModel(
            HiltRenderer renderer,
            PartType type,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        LegacyModelBase model = renderer.getModel(type);
        VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityCutoutNoCull(renderer.getTexture(type))
        );
        model.render(poseStack, consumer, packedLight, packedOverlay);
    }
}
