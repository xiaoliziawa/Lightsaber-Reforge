package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.tile.ModelSithStoneCoffin;
import com.fiskmods.lightsabers.client.render.RenderSubmissionHelper;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithStoneCoffin;
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

public class RenderSithStoneCoffin implements BlockEntityRenderer<
        TileEntitySithStoneCoffin,
        RenderSithStoneCoffin.RenderState
> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/models/sith_stone_coffin.png"
    );

    private final ModelSithStoneCoffin model;

    public RenderSithStoneCoffin(BlockEntityRendererProvider.Context context) {
        model = new ModelSithStoneCoffin(
                context.bakeLayer(ModelSithStoneCoffin.LAYER)
        );
    }

    @Override
    public AABB getRenderBoundingBox(TileEntitySithStoneCoffin coffin) {
        return new AABB(coffin.getBlockPos()).expandTowards(0.0D, 1.0D, 0.0D);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            TileEntitySithStoneCoffin coffin,
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
        state.baseplateOnly = coffin.isBaseplateOnly();
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
        poseStack.mulPose(Axis.YP.rotationDegrees(state.facing.toYRot()));
        RenderSubmissionHelper.submitGeometry(
                collector,
                poseStack,
                RenderTypes.entityCutout(TEXTURE),
                (renderPose, consumer) -> model.render(
                        renderPose,
                        consumer,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        state.baseplateOnly
                )
        );
        poseStack.popPose();
    }

    public static final class RenderState extends BlockEntityRenderState {
        private Direction facing = Direction.NORTH;
        private boolean baseplateOnly;
    }
}
