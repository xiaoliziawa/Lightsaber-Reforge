package com.fiskmods.lightsabers.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

public final class LegacyGlState {
    private LegacyGlState() {
    }

    public static void glPushMatrix() {
        LegacyRenderContext.get().poseStack().pushPose();
    }

    public static void glPopMatrix() {
        LegacyRenderContext.get().poseStack().popPose();
    }

    public static void glTranslatef(float x, float y, float z) {
        LegacyRenderContext.get().poseStack().translate(x, y, z);
    }

    public static void glScaled(double x, double y, double z) {
        LegacyRenderContext.get().poseStack().scale((float) x, (float) y, (float) z);
    }

    public static void glRotatef(float angle, float axisX, float axisY, float axisZ) {
        LegacyRenderContext.State context = LegacyRenderContext.get();
        PoseStack poseStack = context.poseStack();
        Quaternionf rotation = context.temporaryRotation()
                .identity()
                .rotationAxis(
                        (float) Math.toRadians(angle),
                        axisX,
                        axisY,
                        axisZ
                );
        poseStack.mulPose(rotation);
    }
}
