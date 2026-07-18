package com.fiskmods.lightsabers.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;

public class LegacyModelBase {
    public int textureWidth = 64;
    public int textureHeight = 32;

    public void render(
            Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            float scale
    ) {
    }

    public final void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay
    ) {
        render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public final void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        LegacyRenderContext.begin(
                poseStack,
                consumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
        try {
            render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F / 16.0F);
        } finally {
            LegacyRenderContext.end();
        }
    }
}
