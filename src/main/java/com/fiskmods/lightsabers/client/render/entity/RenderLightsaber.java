package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberBladeRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberRenderer;
import com.fiskmods.lightsabers.common.entity.EntityLightsaber;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class RenderLightsaber extends EntityRenderer<EntityLightsaber> {
    private static final float SCALE = 0.2F;
    private static final float SPIN_DEGREES_PER_TICK = 40.0F;
    private static final float VERTICAL_OFFSET = 0.03F;
    // 自转时剑尖的运动方向是模型空间 -Z，方向敏感的剑刃（如武士刀，刃在 -X）
    // 需绕剑轴滚转 -90°，让刀刃转入旋转平面并领先旋转方向。
    private static final float SPIN_EDGE_ROLL = -90.0F;

    public RenderLightsaber(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            EntityLightsaber entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, VERTICAL_OFFSET, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot())
        ));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(
                (entity.tickCount + partialTick) * SPIN_DEGREES_PER_TICK
        ));
        poseStack.scale(SCALE, SCALE, SCALE);

        LightsaberBladeRenderer.bladeRoll = SPIN_EDGE_ROLL;
        if (stack.getItem() instanceof ItemDoubleLightsaber) {
            LightsaberRenderer.render(
                    ItemDoubleLightsaber.get(stack),
                    stack,
                    poseStack,
                    buffer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    true
            );
        } else {
            LightsaberRenderer.render(
                    LightsaberData.get(stack),
                    stack,
                    poseStack,
                    buffer,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    true
            );
        }
        LightsaberBladeRenderer.bladeRoll = 0.0F;
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLightsaber entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
