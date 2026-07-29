package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class LightsaberClientItemExtensions implements IClientItemExtensions {
    private static final float SABER_MAIN_ARM_X = -1.0F;
    private static final float SABER_MAIN_ARM_Y = -0.5F;
    private static final float SABER_MAIN_ARM_Z = -0.2F;
    private static final float SABER_SUPPORT_ARM_X = -1.0F;
    private static final float SABER_SUPPORT_ARM_Y = 0.75F;
    private static final float SABER_ARM_INWARD_SHIFT = 0.5F;
    private static final float DOUBLE_ARM_X = -Mth.PI / 2.5F;
    private static final float DOUBLE_ARM_Y = -Mth.PI / 15.0F;

    public static final EnumProxy<HumanoidModel.ArmPose> SABER_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            true,
            (IArmPoseTransformer) LightsaberClientItemExtensions::applySaberPose
    );
    public static final EnumProxy<HumanoidModel.ArmPose> DOUBLE_SABER_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            true,
            (IArmPoseTransformer) LightsaberClientItemExtensions::applyDoubleSaberPose
    );

    public static final LightsaberClientItemExtensions INSTANCE =
            new LightsaberClientItemExtensions();

    private final LightsaberItemRenderer renderer = new LightsaberItemRenderer();

    private LightsaberClientItemExtensions() {
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(
            LivingEntity entity,
            InteractionHand hand,
            ItemStack stack
    ) {
        if (!ItemLightsaberBase.isActive(stack)
                || ItemLightsaberBase.isSpearLightsaber(stack)
                || entity.isCrouching()) {
            return null;
        }
        return stack.getItem() instanceof ItemDoubleLightsaber
                ? DOUBLE_SABER_POSE.getValue()
                : SABER_POSE.getValue();
    }

    private static void applySaberPose(
            HumanoidModel<?> model,
            LivingEntity entity,
            HumanoidArm arm
    ) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        ModelPart mainArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        ModelPart supportArm = arm == HumanoidArm.RIGHT ? model.leftArm : model.rightArm;

        mainArm.xRot = SABER_MAIN_ARM_X;
        mainArm.yRot = SABER_MAIN_ARM_Y * side;
        mainArm.zRot = SABER_MAIN_ARM_Z * side;
        mainArm.x += SABER_ARM_INWARD_SHIFT * side;
        supportArm.xRot = SABER_SUPPORT_ARM_X;
        supportArm.yRot = SABER_SUPPORT_ARM_Y * side;
        supportArm.zRot = 0.0F;
        supportArm.x -= SABER_ARM_INWARD_SHIFT * side;
    }

    private static void applyDoubleSaberPose(
            HumanoidModel<?> model,
            LivingEntity entity,
            HumanoidArm arm
    ) {
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        ModelPart mainArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        mainArm.xRot = DOUBLE_ARM_X;
        mainArm.yRot = DOUBLE_ARM_Y * side;
    }
}
