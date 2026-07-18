package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.client.render.CrystalRenderHelper;
import com.fiskmods.lightsabers.common.block.BlockCrystal;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public final class RenderCrystal implements BlockEntityRenderer<TileEntityCrystal> {
    private static final float CRYSTAL_ALPHA = 0.6F;

    private final ModelCrystal model;

    public RenderCrystal(BlockEntityRendererProvider.Context context) {
        model = new ModelCrystal(context.bakeLayer(ModelCrystal.LAYER));
    }

    @Override
    public void render(
            TileEntityCrystal crystal,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        applyFacing(poseStack, crystal.getBlockState().getValue(BlockCrystal.FACING));
        poseStack.mulPose(Axis.YP.rotationDegrees(crystal.getRenderRotation()));
        poseStack.translate(0.0F, crystal.getRenderOffset(), 0.0F);
        CrystalRenderHelper.render(
                model,
                poseStack,
                buffer,
                crystal.getColor(),
                CRYSTAL_ALPHA
        );
        poseStack.popPose();
    }

    private static void applyFacing(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0.0F, 2.0F, 0.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case EAST -> applyWallRotation(poseStack, 0.0F);
            case WEST -> applyWallRotation(poseStack, 180.0F);
            case SOUTH -> applyWallRotation(poseStack, 90.0F);
            case NORTH -> applyWallRotation(poseStack, 270.0F);
            default -> {
            }
        }
    }

    private static void applyWallRotation(PoseStack poseStack, float rotation) {
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(1.0F, 1.0F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
    }
}
