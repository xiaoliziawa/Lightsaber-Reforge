package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.client.render.RenderSubmissionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class DeferredGlowRenderer {
    public static void submitGeometry(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType worldRenderType,
            RenderType directRenderType,
            boolean deferInWorld,
            RenderSubmissionHelper.GeometryRenderer renderer
    ) {
        RenderSubmissionHelper.submitShaderEffectGeometry(
                collector,
                poseStack,
                deferInWorld ? worldRenderType : directRenderType,
                renderer
        );
    }
}
