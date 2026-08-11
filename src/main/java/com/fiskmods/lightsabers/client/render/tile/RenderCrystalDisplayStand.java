package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystalDisplayStand;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RenderCrystalDisplayStand implements BlockEntityRenderer<
        TileEntityCrystalDisplayStand,
        RenderCrystalDisplayStand.RenderState
> {
    private static final float DISPLAY_Y = 1.12F;
    private static final float DISPLAY_RAISE = 10.0F / 16.0F;
    private static final float DISPLAY_SCALE = 0.65F;
    private static final float BOB_AMPLITUDE = 0.035F;
    private static final float BOB_SPEED = 0.08F;

    private final ItemModelResolver itemModelResolver;

    public RenderCrystalDisplayStand(BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            TileEntityCrystalDisplayStand stand,
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
        itemModelResolver.updateForTopItem(
                state.item,
                stand.getDisplayStack(),
                ItemDisplayContext.FIXED,
                stand.getLevel(),
                null,
                (int) stand.getBlockPos().asLong()
        );
        float time = stand.getLevel() == null
                ? partialTicks
                : stand.getLevel().getGameTime() + partialTicks;
        float phase = (float) (stand.getBlockPos().getX() * 0.37D
                + stand.getBlockPos().getZ() * 0.23D);
        state.bob = Mth.sin(time * BOB_SPEED + phase) * BOB_AMPLITUDE;
        state.lightsaber = stand.getDisplayStack().getItem()
                instanceof ItemLightsaberBase;
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
        poseStack.translate(0.5F, DISPLAY_Y + DISPLAY_RAISE + state.bob, 0.5F);
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        if (state.lightsaber) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        }
        state.item.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }

    public static final class RenderState extends BlockEntityRenderState {
        private final ItemStackRenderState item = new ItemStackRenderState();
        private float bob;
        private boolean lightsaber;
    }
}
