package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.client.render.CrystalRenderHelper;
import com.fiskmods.lightsabers.common.block.BlockCrystal;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RenderCrystal
        implements BlockEntityRenderer<TileEntityCrystal, RenderCrystal.RenderState> {
    private static final float CRYSTAL_ALPHA = 0.6F;

    private final ModelCrystal model;

    public RenderCrystal(BlockEntityRendererProvider.Context context) {
        model = new ModelCrystal(context.bakeLayer(ModelCrystal.LAYER));
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            TileEntityCrystal crystal,
            RenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                crystal,
                state,
                partialTicks,
                cameraPosition,
                breakProgress
        );
        state.facing = crystal.getBlockState().getValue(BlockCrystal.FACING);
        state.rotation = crystal.getRenderRotation();
        state.offset = crystal.getRenderOffset();
        state.color = crystal.getColor();
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        applyFacing(poseStack, state.facing);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        poseStack.translate(0.0F, state.offset, 0.0F);
        CrystalRenderHelper.render(
                model,
                poseStack,
                collector,
                state.color,
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

    public static final class RenderState extends BlockEntityRenderState {
        private Direction facing = Direction.UP;
        private float rotation;
        private float offset;
        private CrystalColor color = CrystalColor.WHITE;
    }
}
