package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.client.render.HolocronObjRenderer;
import com.fiskmods.lightsabers.common.block.BlockHolocron;
import com.fiskmods.lightsabers.common.block.HolocronType;
import com.fiskmods.lightsabers.common.tileentity.TileEntityHolocron;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderHolocron implements BlockEntityRenderer<
        TileEntityHolocron,
        RenderHolocron.RenderState
> {
    private static final float MODEL_CENTER_Y = 0.25F;
    private static final float OPEN_HEIGHT = 0.25F;
    private static final float HOVER_SPEED = 10.0F;
    private static final float HOVER_HEIGHT = 0.05F;

    public RenderHolocron(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            TileEntityHolocron holocron,
            RenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                holocron,
                state,
                partialTicks,
                cameraPosition,
                breakProgress
        );
        if (holocron.getBlockState().getBlock() instanceof BlockHolocron block) {
            state.type = block.getType();
        } else {
            state.type = null;
        }
        state.openProgress = holocron.getOpenProgress(partialTicks);
        state.openTicks = holocron.getOpenTicks(partialTicks);
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        if (state.type == null) {
            return;
        }
        float hoverOffset = Mth.sin(state.openTicks / HOVER_SPEED) * HOVER_HEIGHT;

        poseStack.pushPose();
        poseStack.translate(
                0.5F,
                MODEL_CENTER_Y
                        + (OPEN_HEIGHT + hoverOffset) * state.openProgress,
                0.5F
        );
        HolocronObjRenderer.renderModel(
                state.type,
                state.openProgress,
                state.openTicks,
                poseStack,
                collector,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    public static final class RenderState extends BlockEntityRenderState {
        private HolocronType type;
        private float openProgress;
        private float openTicks;
    }
}
