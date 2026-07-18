package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.client.render.HolocronObjRenderer;
import com.fiskmods.lightsabers.common.block.BlockHolocron;
import com.fiskmods.lightsabers.common.tileentity.TileEntityHolocron;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

public class RenderHolocron implements BlockEntityRenderer<TileEntityHolocron> {
    private static final float MODEL_CENTER_Y = 0.25F;
    private static final float OPEN_HEIGHT = 0.25F;
    private static final float HOVER_SPEED = 10.0F;
    private static final float HOVER_HEIGHT = 0.05F;

    public RenderHolocron(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            TileEntityHolocron holocron,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!(holocron.getBlockState().getBlock() instanceof BlockHolocron block)) {
            return;
        }
        float openProgress = holocron.getOpenProgress(partialTick);
        float hoverOffset = Mth.sin(holocron.getOpenTicks(partialTick) / HOVER_SPEED)
                * HOVER_HEIGHT;

        poseStack.pushPose();
        poseStack.translate(
                0.5F,
                MODEL_CENTER_Y + (OPEN_HEIGHT + hoverOffset) * openProgress,
                0.5F
        );
        HolocronObjRenderer.renderModel(
                block.getType(),
                openProgress,
                holocron.getOpenTicks(partialTick),
                poseStack,
                buffer,
                packedOverlay
        );
        poseStack.popPose();
    }
}
