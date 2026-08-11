package com.fiskmods.lightsabers.client.render;

import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class CrystalRenderHelper {
    private static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath("neoforge", "textures/white.png");
    private static final RenderType RENDER_TYPE =
            RenderTypes.entityTranslucentEmissive(WHITE_TEXTURE);

    private CrystalRenderHelper() {
    }

    public static void render(
            ModelCrystal model,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CrystalColor color,
            float alpha
    ) {
        int packedColor = color.getRenderColor();
        float red = (packedColor >> 16 & 0xFF) / 255.0F;
        float green = (packedColor >> 8 & 0xFF) / 255.0F;
        float blue = (packedColor & 0xFF) / 255.0F;
        collector.submitModelPart(
                model.root(),
                poseStack,
                RENDER_TYPE,
                LightCoordsUtil.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                null,
                ARGB.colorFromFloat(alpha, red, green, blue),
                null
        );
    }
}
