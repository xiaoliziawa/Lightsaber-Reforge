package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.client.model.tile.ModelCrystal;
import com.fiskmods.lightsabers.client.render.CrystalRenderHelper;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class CrystalItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float ITEM_ALPHA = 0.6F;
    private static final float GUI_SCALE = 1.8F;
    private static final float GUI_VERTICAL_OFFSET = 0.18F;
    private static final float FIRST_PERSON_SCALE = 1.8F;
    private static final float THIRD_PERSON_SCALE = 1.5F;
    private static final float GROUND_SCALE = 1.5F;
    private static final float FIXED_SCALE = 2.0F;

    private ModelCrystal model;

    public CrystalItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack);
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        CrystalRenderHelper.render(
                getModel(),
                poseStack,
                buffer,
                ItemCrystal.get(stack),
                ITEM_ALPHA
        );
        poseStack.popPose();
    }

    private ModelCrystal getModel() {
        if (model == null) {
            model = new ModelCrystal(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelCrystal.LAYER)
            );
        }
        return model;
    }

    private static void applyDisplayTransform(
            ItemDisplayContext displayContext,
            PoseStack poseStack
    ) {
        switch (displayContext) {
            case GUI -> {
                beginTransform(poseStack, GUI_VERTICAL_OFFSET);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                finishTransform(poseStack, GUI_SCALE);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                beginTransform(poseStack, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(35.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
                finishTransform(poseStack, FIRST_PERSON_SCALE);
            }
            case FIRST_PERSON_RIGHT_HAND -> {
                beginTransform(poseStack, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees(-35.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
                finishTransform(poseStack, FIRST_PERSON_SCALE);
            }
            case THIRD_PERSON_LEFT_HAND -> {
                beginTransform(poseStack, 0.1F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F));
                finishTransform(poseStack, THIRD_PERSON_SCALE);
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                beginTransform(poseStack, 0.1F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));
                finishTransform(poseStack, THIRD_PERSON_SCALE);
            }
            case GROUND -> centerAndScale(poseStack, GROUND_SCALE, 0.0F);
            case FIXED -> {
                beginTransform(poseStack, 0.04F);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                finishTransform(poseStack, FIXED_SCALE);
            }
            default -> centerAndScale(poseStack, FIXED_SCALE, 0.0F);
        }
    }

    private static void beginTransform(PoseStack poseStack, float verticalOffset) {
        poseStack.translate(0.5F, verticalOffset, 0.5F);
    }

    private static void finishTransform(PoseStack poseStack, float scale) {
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
    }

    private static void centerAndScale(
            PoseStack poseStack,
            float scale,
            float verticalOffset
    ) {
        beginTransform(poseStack, verticalOffset);
        finishTransform(poseStack, scale);
    }
}
