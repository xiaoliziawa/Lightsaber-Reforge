package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.common.block.BlockLightsaberStand;
import com.fiskmods.lightsabers.common.tileentity.TileEntityLightsaberStand;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RenderLightsaberStand implements BlockEntityRenderer<TileEntityLightsaberStand> {
    private static final float DISPLAY_SCALE = 0.65F;
    private static final float DISPLAY_NORMAL_OFFSET = -0.3425F;

    private final ItemRenderer itemRenderer;

    public RenderLightsaberStand(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            TileEntityLightsaberStand stand,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack displayStack = stand.getDisplayStack();
        if (displayStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        applyStandRotation(poseStack, stand.getBlockState());
        poseStack.translate(0.0F, DISPLAY_NORMAL_OFFSET, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        itemRenderer.renderStatic(
                displayStack,
                ItemDisplayContext.NONE,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                stand.getLevel(),
                (int) stand.getBlockPos().asLong()
        );
        poseStack.popPose();
    }

    private static void applyStandRotation(PoseStack poseStack, BlockState state) {
        Direction facing = state.getValue(BlockLightsaberStand.FACING);
        if (facing == Direction.UP) {
            if (state.getValue(BlockLightsaberStand.AXIS) == Direction.Axis.X) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            return;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    }
}
