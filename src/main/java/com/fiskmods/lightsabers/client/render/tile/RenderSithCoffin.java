package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.tile.ModelSithCoffin;
import com.fiskmods.lightsabers.client.render.RenderSubmissionHelper;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithCoffin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderSithCoffin implements BlockEntityRenderer<
        TileEntitySithCoffin,
        RenderSithCoffin.RenderState
> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/models/sith_coffin.png"
    );

    private final ModelSithCoffin model;

    public RenderSithCoffin(BlockEntityRendererProvider.Context context) {
        model = new ModelSithCoffin(context.bakeLayer(ModelSithCoffin.LAYER));
    }

    @Override
    public AABB getRenderBoundingBox(TileEntitySithCoffin coffin) {
        Direction facing = coffin.getBlockState().getValue(
                HorizontalDirectionalBlock.FACING
        );
        var pos = coffin.getBlockPos();
        var frontPos = pos.relative(facing);
        return new AABB(
                Math.min(pos.getX(), frontPos.getX()),
                pos.getY(),
                Math.min(pos.getZ(), frontPos.getZ()),
                Math.max(pos.getX(), frontPos.getX()) + 1.0D,
                pos.getY() + 1.0D,
                Math.max(pos.getZ(), frontPos.getZ()) + 1.0D
        );
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            TileEntitySithCoffin coffin,
            RenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(
                coffin,
                state,
                partialTicks,
                cameraPosition,
                breakProgress
        );
        state.facing = coffin.getBlockState().getValue(
                HorizontalDirectionalBlock.FACING
        );
        state.openProgress = coffin.getLidOpenProgress(partialTicks);
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
        poseStack.mulPose(Axis.YP.rotationDegrees(state.facing.toYRot() + 180.0F));
        RenderSubmissionHelper.submitGeometry(
                collector,
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                (renderPose, consumer) -> model.render(
                        renderPose,
                        consumer,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        state.openProgress
                )
        );
        poseStack.popPose();
    }

    public static final class RenderState extends BlockEntityRenderState {
        private Direction facing = Direction.NORTH;
        private float openProgress;
    }
}
