package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.integration.epicfight.EpicFightClientIntegration;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.SpinningLightsaberObjRenderer;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.hilt.HiltManager;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.item.ItemLightsaberPart;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LightsaberItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static boolean guiBladePreview;
    private static final float PART_MODEL_SCALE = 1.6F;
    private static final float SHORT_POMMEL_MODEL_SCALE = 3.2F;
    private static final float MAX_PART_MODEL_HEIGHT = 80.0F;
    private static final float MODEL_PIXELS_PER_BLOCK = 16.0F;
    private static final float GUI_SCALE = 0.28F;
    private static final float SPINNING_EMITTER_EXTENT_CM = 3.462F;
    private static final float SPINNING_SWITCH_EXTENT_CM = 1.740F;
    private static final float SPINNING_GRIP_EXTENT_CM = 13.947F;
    private static final float SPINNING_SMALL_PART_SCALE = 2.5F;
    private static final float FIRST_PERSON_SCALE = 0.30F;
    private static final float FIRST_PERSON_HORIZONTAL_OFFSET = 0.22F;
    private static final float FIRST_PERSON_VERTICAL_OFFSET = -0.30F;
    private static final float FIRST_PERSON_DEPTH_OFFSET = -0.35F;
    private static final float FIRST_PERSON_TILT = -12.0F;
    private static final float FIRST_PERSON_YAW = 65.0F;
    private static final float DOUBLE_FIRST_PERSON_VERTICAL_OFFSET = -0.32F;
    private static final float DOUBLE_FIRST_PERSON_DEPTH_OFFSET = -0.45F;
    private static final float DOUBLE_FIRST_PERSON_ROLL = -90.0F;
    private static final float DOUBLE_WALK_ROLL = -90.0F;
    private static final float DOUBLE_WALK_PUSH_X = 0.10F;
    private static final float DOUBLE_WALK_PUSH_Z = -0.10F;
    private static final float DOUBLE_SWING_PUSH_X = 0.05F;
    private static final float DOUBLE_SWING_PUSH_Y = 0.10F;
    private static final float DOUBLE_SWING_TILT = -30.0F;
    private static final float DOUBLE_SWING_SPIN = 360.0F;
    private static final float THIRD_PERSON_SCALE = 0.24F;
    private static final float THIRD_PERSON_VERTICAL_OFFSET = 0.25F;
    private static final float THIRD_PERSON_DEPTH_OFFSET = 0.15F;
    private static final float THIRD_PERSON_TILT = 75.0F;
    private static final float THIRD_PERSON_PITCH = 80.0F;
    private static final float THIRD_PERSON_GRIP_OFFSET = 0.35F;
    private static final float DOUBLE_THIRD_PERSON_ROT_X = 0F;
    private static final float DOUBLE_THIRD_PERSON_ROT_Y = 90F;
    private static final float DOUBLE_THIRD_PERSON_ROT_Z = 90.0F;
    private static final float DOUBLE_THIRD_PERSON_OFFSET_X = 0.4F;
    private static final float DOUBLE_THIRD_PERSON_OFFSET_Y = 0.25F;
    private static final float DOUBLE_THIRD_PERSON_OFFSET_Z = -0.05F;
    private static final float GROUND_SCALE = 0.20F;
    private static final float FIXED_SCALE = 0.24F;
    private static final float SPINNING_GUI_SCALE = 0.70F;
    private static final float SPINNING_GUI_ROTATION = 45.0F;
    private static final float SPINNING_GUI_FACE_ROTATION = 90.0F;
    private static final float SPINNING_GUI_TURN_ROTATION = 180.0F;
    private static final float SPINNING_THIRD_PERSON_TURN_ROTATION = 180.0F;
    // 右键旋转姿势的最终位置：水平、垂直、前后。
    private static final float SPINNING_DEFENSE_HORIZONTAL_OFFSET = 0.05F;
    private static final float SPINNING_DEFENSE_VERTICAL_OFFSET = -0.10F;
    private static final float SPINNING_DEFENSE_FORWARD_OFFSET = 0.05F;
    // 圆环朝向：90 度使旋转平面面向玩家正前方，只控制角度，不控制位置。
    private static final float SPINNING_DEFENSE_FACE_ROTATION = 90.0F;

    public LightsaberItemRenderer() {
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
        ItemLightsaberPart partItem = stack.getItem() instanceof ItemLightsaberPart item
                ? item
                : null;
        boolean spinning = isSpinningStack(stack, partItem);
        boolean rotateSpinningGui = spinning
                && (partItem == null || partItem.partType == PartType.BODY);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        boolean activeDoubleLightsaber = stack.getItem() instanceof ItemDoubleLightsaber
                && ItemLightsaberBase.isActive(stack);
        if (activeDoubleLightsaber
                && isThirdPerson(displayContext)
                && Lightsabers.isEpicFightLoaded
                && EpicFightClientIntegration.isBattleModeHeldStack(stack)) {
            activeDoubleLightsaber = false;
        }
        boolean spinningDefense = spinning
                && partItem == null
                && isThirdPerson(displayContext)
                && Lightsabers.isEpicFightLoaded
                && EpicFightClientIntegration.isBattleModeUsingStack(stack);
        applyDisplayTransform(
                displayContext,
                partItem != null,
                activeDoubleLightsaber,
                spinning,
                rotateSpinningGui,
                spinningDefense,
                poseStack
        );
        if (partItem != null) {
            renderPart(
                    stack,
                    partItem.partType,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
        } else if (displayContext == ItemDisplayContext.GUI && !guiBladePreview) {
            if (stack.getItem() instanceof ItemDoubleLightsaber) {
                HiltModelRenderer.render(
                        ItemDoubleLightsaber.get(stack),
                        stack,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay
                );
            } else {
                HiltModelRenderer.render(
                        LightsaberData.get(stack),
                        stack,
                        poseStack,
                        buffer,
                        packedLight,
                        packedOverlay
                );
            }
        } else if (stack.getItem() instanceof ItemDoubleLightsaber) {
            LightsaberRenderer.render(
                    ItemDoubleLightsaber.get(stack),
                    stack,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    displayContext != ItemDisplayContext.GUI
            );
        } else {
            LightsaberRenderer.render(
                    LightsaberData.get(stack),
                    stack,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    displayContext != ItemDisplayContext.GUI
            );
        }
        poseStack.popPose();
    }

    private void renderPart(
            ItemStack stack,
            PartType type,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Hilt hilt = ItemLightsaberPart.get(stack);
        if (hilt == HiltManager.SPINNING) {
            float scale = getSpinningPartScale(type);
            poseStack.scale(scale, scale, scale);
            HiltModelRenderer.renderPart(
                    hilt,
                    type,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            return;
        }
        float height = hilt.getPart(type).height;
        float scale = PART_MODEL_SCALE;
        if (type == PartType.POMMEL && height <= 4.0F) {
            scale = SHORT_POMMEL_MODEL_SCALE;
        } else if (height * scale > MAX_PART_MODEL_HEIGHT) {
            scale = MAX_PART_MODEL_HEIGHT / height;
        }
        poseStack.scale(scale, scale, scale);
        HiltModelRenderer.renderPart(
                hilt,
                type,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
    }

    private float getSpinningPartScale(PartType type) {
        float extent = switch (type) {
            case EMITTER, POMMEL -> SPINNING_EMITTER_EXTENT_CM;
            case SWITCH_SECTION -> SPINNING_SWITCH_EXTENT_CM;
            case BODY -> SPINNING_GRIP_EXTENT_CM;
        };
        float targetSize = HiltManager.SPINNING.getPart(type).height
                * PART_MODEL_SCALE / MODEL_PIXELS_PER_BLOCK;
        if (type != PartType.BODY) {
            targetSize *= SPINNING_SMALL_PART_SCALE;
        }
        return targetSize
                * SpinningLightsaberObjRenderer.MODEL_UNITS_PER_BLOCK
                / extent;
    }

    private void applyFirstPersonTransform(PoseStack poseStack, int handSide, boolean doubleLightsaber) {
        poseStack.translate(
                FIRST_PERSON_HORIZONTAL_OFFSET * handSide,
                doubleLightsaber ? DOUBLE_FIRST_PERSON_VERTICAL_OFFSET : FIRST_PERSON_VERTICAL_OFFSET,
                doubleLightsaber ? DOUBLE_FIRST_PERSON_DEPTH_OFFSET : FIRST_PERSON_DEPTH_OFFSET
        );
        poseStack.mulPose(Axis.ZP.rotationDegrees(FIRST_PERSON_TILT * handSide));
        if (doubleLightsaber) {
            applyDoubleFirstPersonAnimation(poseStack, handSide);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(FIRST_PERSON_YAW * handSide));
        poseStack.scale(FIRST_PERSON_SCALE, FIRST_PERSON_SCALE, FIRST_PERSON_SCALE);
    }

    private void applyDoubleFirstPersonAnimation(PoseStack poseStack, int handSide) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(DOUBLE_FIRST_PERSON_ROLL * handSide));
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        float partialTick = Minecraft.getInstance().getPartialTick();
        float walk = player.walkAnimation.speed(partialTick);
        float swing = getHandSwingProgress(player, handSide, partialTick);
        float swingArc = (swing > 0.5F ? 1.0F - swing : swing) * 2.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(DOUBLE_WALK_ROLL * walk * handSide));
        poseStack.translate(
                (DOUBLE_WALK_PUSH_X * walk + DOUBLE_SWING_PUSH_X * swingArc) * handSide,
                DOUBLE_SWING_PUSH_Y * swingArc,
                DOUBLE_WALK_PUSH_Z * walk
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(DOUBLE_SWING_TILT * swingArc));
        poseStack.mulPose(Axis.ZP.rotationDegrees(DOUBLE_SWING_SPIN * swing * handSide));
    }

    private float getHandSwingProgress(LocalPlayer player, int handSide, float partialTick) {
        HumanoidArm arm = handSide == 1 ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        HumanoidArm swingingArm = player.swingingArm == InteractionHand.OFF_HAND
                ? player.getMainArm().getOpposite()
                : player.getMainArm();
        return arm == swingingArm ? player.getAttackAnim(partialTick) : 0.0F;
    }

    private void applyThirdPersonTransform(
            PoseStack poseStack,
            int handSide,
            boolean doubleLightsaber,
            boolean spinningDefense
    ) {
        poseStack.translate(
                spinningDefense
                        ? SPINNING_DEFENSE_HORIZONTAL_OFFSET * handSide
                        : 0.0F,
                THIRD_PERSON_VERTICAL_OFFSET
                        + (spinningDefense
                                ? SPINNING_DEFENSE_VERTICAL_OFFSET
                                : 0.0F),
                THIRD_PERSON_DEPTH_OFFSET
                        + (spinningDefense
                                ? SPINNING_DEFENSE_FORWARD_OFFSET
                                : 0.0F)
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F * handSide));
        poseStack.mulPose(Axis.ZP.rotationDegrees(THIRD_PERSON_TILT * handSide));
        poseStack.mulPose(Axis.ZP.rotationDegrees(THIRD_PERSON_PITCH * handSide));
        if (spinningDefense) {
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    SPINNING_DEFENSE_FACE_ROTATION * handSide
            ));
        }
        if (doubleLightsaber) {
            poseStack.mulPose(Axis.XP.rotationDegrees(DOUBLE_THIRD_PERSON_ROT_X * handSide));
            poseStack.mulPose(Axis.YP.rotationDegrees(DOUBLE_THIRD_PERSON_ROT_Y * handSide));
            poseStack.mulPose(Axis.ZP.rotationDegrees(DOUBLE_THIRD_PERSON_ROT_Z * handSide));
            poseStack.translate(
                    DOUBLE_THIRD_PERSON_OFFSET_X * handSide,
                    DOUBLE_THIRD_PERSON_OFFSET_Y,
                    DOUBLE_THIRD_PERSON_OFFSET_Z
            );
        }
        poseStack.translate(0.0F, THIRD_PERSON_GRIP_OFFSET, 0.0F);
        poseStack.scale(THIRD_PERSON_SCALE, THIRD_PERSON_SCALE, THIRD_PERSON_SCALE);
    }

    private void applyDisplayTransform(
            ItemDisplayContext displayContext,
            boolean lightsaberPart,
            boolean doubleLightsaber,
            boolean spinning,
            boolean rotateSpinningGui,
            boolean spinningDefense,
            PoseStack poseStack
    ) {
        switch (displayContext) {
            case GUI -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(
                        rotateSpinningGui ? SPINNING_GUI_ROTATION : -45.0F
                ));
                if (!lightsaberPart) {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                }
                if (rotateSpinningGui) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(SPINNING_GUI_FACE_ROTATION));
                    poseStack.mulPose(Axis.YP.rotationDegrees(SPINNING_GUI_TURN_ROTATION));
                }
                poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
                poseStack.scale(GUI_SCALE, GUI_SCALE, GUI_SCALE);
                if (spinning && !lightsaberPart) {
                    poseStack.scale(SPINNING_GUI_SCALE, SPINNING_GUI_SCALE, SPINNING_GUI_SCALE);
                }
            }
            case FIRST_PERSON_LEFT_HAND -> applyFirstPersonTransform(poseStack, -1, doubleLightsaber);
            case FIRST_PERSON_RIGHT_HAND -> applyFirstPersonTransform(poseStack, 1, doubleLightsaber);
            case THIRD_PERSON_LEFT_HAND -> {
                applyThirdPersonTransform(
                        poseStack,
                        -1,
                        doubleLightsaber,
                        spinningDefense
                );
                if (spinning && !lightsaberPart) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(SPINNING_THIRD_PERSON_TURN_ROTATION));
                }
            }
            case THIRD_PERSON_RIGHT_HAND -> {
                applyThirdPersonTransform(
                        poseStack,
                        1,
                        doubleLightsaber,
                        spinningDefense
                );
                if (spinning && !lightsaberPart) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(SPINNING_THIRD_PERSON_TURN_ROTATION));
                }
            }
            case GROUND -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.scale(GROUND_SCALE, GROUND_SCALE, GROUND_SCALE);
            }
            case FIXED -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
                poseStack.scale(FIXED_SCALE, FIXED_SCALE, FIXED_SCALE);
            }
            default -> poseStack.scale(FIXED_SCALE, FIXED_SCALE, FIXED_SCALE);
        }
    }

    private boolean isSpinningStack(ItemStack stack, ItemLightsaberPart partItem) {
        if (partItem != null) {
            return ItemLightsaberPart.get(stack) == HiltManager.SPINNING;
        }
        if (stack.getItem() instanceof ItemDoubleLightsaber) {
            for (LightsaberData data : ItemDoubleLightsaber.get(stack)) {
                if (SpinningLightsaberObjRenderer.isSupported(data)) {
                    return true;
                }
            }
            return false;
        }
        return SpinningLightsaberObjRenderer.isSupported(LightsaberData.get(stack));
    }

    private boolean isThirdPerson(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
