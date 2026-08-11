package com.fiskmods.lightsabers.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.world.item.ItemDisplayContext;

public final class RenderSubmissionHelper {
    private static final int[] NO_TINTS = new int[0];

    private RenderSubmissionHelper() {
    }

    public static void submitGeometry(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType renderType,
            GeometryRenderer renderer
    ) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            PoseStack renderPose = new PoseStack();
            renderPose.last().set(pose);
            renderer.render(renderPose, consumer);
        });
    }

    public static void submitQuads(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            QuadCollection model,
            int packedLight,
            int packedOverlay,
            boolean hasFoil,
            int outlineColor
    ) {
        if (model == null || model.getAll().isEmpty()) {
            return;
        }
        collector.submitItem(
                poseStack,
                ItemDisplayContext.NONE,
                packedLight,
                packedOverlay,
                outlineColor,
                NO_TINTS,
                model.getAll(),
                hasFoil
                        ? ItemStackRenderState.FoilType.STANDARD
                        : ItemStackRenderState.FoilType.NONE
        );
    }

    @FunctionalInterface
    public interface GeometryRenderer {
        void render(PoseStack poseStack, VertexConsumer consumer);
    }
}
