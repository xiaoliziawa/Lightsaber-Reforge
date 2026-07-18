package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.client.render.HolocronObjRenderer;
import com.fiskmods.lightsabers.common.block.HolocronType;
import com.fiskmods.lightsabers.common.item.ItemHolocron;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class HolocronItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float GUI_SCALE = 0.9F;
    private static final float GROUND_SCALE = 1.0F;
    private static final float FIXED_SCALE = 1.25F;

    public HolocronItemRenderer() {
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
        HolocronType type = stack.getItem() instanceof ItemHolocron holocron
                ? holocron.getType()
                : HolocronType.SITH;
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(GUI_SCALE, GUI_SCALE, GUI_SCALE);
            HolocronObjRenderer.renderItemIcon(
                    type,
                    poseStack,
                    buffer,
                    packedOverlay
            );
        } else {
            applyDisplayTransform(displayContext, poseStack);
            HolocronObjRenderer.renderModel(
                    type,
                    0,
                    0,
                    poseStack,
                    buffer,
                    packedOverlay
            );
        }
        poseStack.popPose();
    }

    private static void applyDisplayTransform(
            ItemDisplayContext displayContext,
            PoseStack poseStack
    ) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        switch (displayContext) {
            case FIRST_PERSON_LEFT_HAND,
                    FIRST_PERSON_RIGHT_HAND,
                    THIRD_PERSON_LEFT_HAND,
                    THIRD_PERSON_RIGHT_HAND -> {
            }
            case GROUND -> poseStack.scale(GROUND_SCALE, GROUND_SCALE, GROUND_SCALE);
            case FIXED -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(FIXED_SCALE, FIXED_SCALE, FIXED_SCALE);
            }
            default -> poseStack.scale(FIXED_SCALE, FIXED_SCALE, FIXED_SCALE);
        }
    }
}
