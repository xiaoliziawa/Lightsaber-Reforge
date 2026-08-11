package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberBladeRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberRenderer;
import com.fiskmods.lightsabers.common.entity.EntityLightsaber;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

public class RenderLightsaber
        extends EntityRenderer<EntityLightsaber, RenderLightsaber.RenderState> {
    private static final float SCALE = 0.2F;
    private static final float SPIN_DEGREES_PER_TICK = 40.0F;
    private static final float VERTICAL_OFFSET = 0.03F;
    private static final float SPIN_EDGE_ROLL = -90.0F;
    private static final float SPEAR_SCALE = 3.5F;

    public RenderLightsaber(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        if (state.stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, VERTICAL_OFFSET, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        if (!state.spear) {
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    state.ageInTicks * SPIN_DEGREES_PER_TICK
            ));
        }
        poseStack.scale(SCALE, SCALE, SCALE);
        if (state.spear) {
            poseStack.scale(SPEAR_SCALE, SPEAR_SCALE, SPEAR_SCALE);
        }

        LightsaberBladeRenderer.bladeRoll = state.spear ? 0.0F : SPIN_EDGE_ROLL;
        if (state.stack.getItem() instanceof ItemDoubleLightsaber) {
            LightsaberRenderer.render(
                    ItemDoubleLightsaber.get(state.stack),
                    state.stack,
                    poseStack,
                    collector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    true,
                    true
            );
        } else {
            LightsaberRenderer.render(
                    LightsaberData.get(state.stack),
                    state.stack,
                    poseStack,
                    collector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    true,
                    true
            );
        }
        LightsaberBladeRenderer.bladeRoll = 0.0F;
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            EntityLightsaber entity,
            RenderState state,
            float partialTicks
    ) {
        super.extractRenderState(entity, state, partialTicks);
        state.stack = entity.getItem().copy();
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.spear = ItemLightsaberBase.isSpearLightsaber(state.stack);
    }

    public static final class RenderState extends EntityRenderState {
        private ItemStack stack = ItemStack.EMPTY;
        private float yRot;
        private float xRot;
        private boolean spear;
    }
}
