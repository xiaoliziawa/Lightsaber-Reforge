package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.common.block.BlockLightsaberStand;
import com.fiskmods.lightsabers.common.tileentity.TileEntityLightsaberStand;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderLightsaberStand implements BlockEntityRenderer<
        TileEntityLightsaberStand,
        RenderLightsaberStand.RenderState
> {
    private static final float DISPLAY_SCALE = 0.65F;
    private static final float DISPLAY_NORMAL_OFFSET = -0.3425F;

    private final ItemModelResolver itemModelResolver;

    public RenderLightsaberStand(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            TileEntityLightsaberStand stand,
            RenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                stand,
                state,
                partialTicks,
                cameraPosition,
                breakProgress
        );
        state.blockState = stand.getBlockState();
        itemModelResolver.updateForTopItem(
                state.item,
                stand.getDisplayStack(),
                ItemDisplayContext.NONE,
                stand.getLevel(),
                null,
                (int) stand.getBlockPos().asLong()
        );
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        if (state.item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        applyStandRotation(poseStack, state.blockState);
        poseStack.translate(0.0F, DISPLAY_NORMAL_OFFSET, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        state.item.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
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

    public static final class RenderState extends BlockEntityRenderState {
        private final ItemStackRenderState item = new ItemStackRenderState();
        private BlockState blockState;
    }
}
