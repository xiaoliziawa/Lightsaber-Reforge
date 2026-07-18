package com.fiskmods.lightsabers.client.render;

import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class CrystalRenderHelper {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("forge", "textures/white.png");
    private static final RenderType RENDER_TYPE =
            RenderType.entityTranslucentEmissive(WHITE_TEXTURE);

    private CrystalRenderHelper() {
    }

    public static void render(
            ModelCrystal model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            CrystalColor color,
            float alpha
    ) {
        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        int packedColor = color.getRenderColor();
        float red = (packedColor >> 16 & 0xFF) / 255.0F;
        float green = (packedColor >> 8 & 0xFF) / 255.0F;
        float blue = (packedColor & 0xFF) / 255.0F;
        model.render(
                poseStack,
                consumer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                red,
                green,
                blue,
                alpha
        );
    }
}
