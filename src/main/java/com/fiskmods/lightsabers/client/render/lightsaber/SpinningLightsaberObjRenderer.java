package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;

public final class SpinningLightsaberObjRenderer {
    public static final float MODEL_UNITS_PER_BLOCK = 8.0F;
    public static final int BLADE_LENGTH = 80;

    private static final ResourceLocation GRIP_MODEL = model("grip");
    private static final ResourceLocation OUTER_MODEL = model("outer");
    private static final ResourceLocation UPPER_EMITTER_MODEL = model("upper_emitter");
    private static final ResourceLocation LOWER_EMITTER_MODEL = model("lower_emitter");
    private static final ResourceLocation SWITCH_MODEL = model("switch");

    private static final float OBJ_SCALE = 1.0F / MODEL_UNITS_PER_BLOCK;
    private static final float DISPLAY_SCALE = 1.35F;
    private static final float BLADE_WIDTH_SCALE = 2.25F;
    private static final float MODEL_ORIENTATION_ROTATION = 180.0F;
    private static final float SWITCH_PART_OFFSET_X_CM = 0.355F;
    private static final float SWITCH_PART_OFFSET_Y_CM = 2.356F;
    private static final float UPPER_EMITTER_PART_OFFSET_X_CM = 0.300F;
    private static final float UPPER_EMITTER_PART_OFFSET_Y_CM = 8.230F;
    private static final float LOWER_EMITTER_PART_OFFSET_X_CM = 0.335F;
    private static final float LOWER_EMITTER_PART_OFFSET_Y_CM = -8.230F;
    private static final float UPPER_BLADE_BASE_X = -0.018F * OBJ_SCALE;
    private static final float UPPER_BLADE_BASE_Y = 10.178F * OBJ_SCALE;
    private static final float LOWER_BLADE_BASE_X = 0.018F * OBJ_SCALE;
    private static final float LOWER_BLADE_BASE_Y = -10.178F * OBJ_SCALE;
    private static final float NORMAL_POMMEL_OFFSET_Y = -0.96F;
    private static final float ROTATION_DEGREES_PER_TICK = 72.0F;
    private static final RenderType MODEL_RENDER_TYPE = RenderType.entityCutoutNoCull(
            TextureAtlas.LOCATION_BLOCKS
    );

    private SpinningLightsaberObjRenderer() {
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(GRIP_MODEL);
        event.register(OUTER_MODEL);
        event.register(UPPER_EMITTER_MODEL);
        event.register(LOWER_EMITTER_MODEL);
        event.register(SWITCH_MODEL);
    }

    public static boolean isSupported(LightsaberData data) {
        return data.hasSpinningCore();
    }

    public static boolean hasLowerEmitter(LightsaberData data) {
        return data.canSpinBlades();
    }

    public static void renderHilt(
            LightsaberData data,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!isSupported(data)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        float rotation = getRotation(minecraft, data);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        poseStack.pushPose();
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(itemRenderer, modelManager.getModel(GRIP_MODEL), poseStack, buffer, packedLight, packedOverlay);
        renderModel(itemRenderer, modelManager.getModel(SWITCH_MODEL), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(itemRenderer, modelManager.getModel(OUTER_MODEL), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(itemRenderer, modelManager.getModel(UPPER_EMITTER_MODEL), poseStack, buffer, packedLight, packedOverlay);
        if (hasLowerEmitter(data)) {
            renderModel(itemRenderer, modelManager.getModel(LOWER_EMITTER_MODEL), poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();

        if (!hasLowerEmitter(data)) {
            poseStack.pushPose();
            poseStack.translate(0.0F, NORMAL_POMMEL_OFFSET_Y, 0.0F);
            HiltModelRenderer.renderPart(
                    data.get(PartType.POMMEL),
                    PartType.POMMEL,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static void renderBlades(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            boolean inWorld
    ) {
        if (!isSupported(data)) {
            return;
        }

        float rotation = getRotation(Minecraft.getInstance(), data);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(UPPER_BLADE_BASE_X, UPPER_BLADE_BASE_Y, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(BLADE_WIDTH_SCALE, 1.0F, BLADE_WIDTH_SCALE);
        LightsaberBladeRenderer.render(
                data,
                stack,
                poseStack,
                buffer,
                inWorld,
                BLADE_LENGTH,
                false
        );
        poseStack.popPose();

        if (!hasLowerEmitter(data)) {
            poseStack.popPose();
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(LOWER_BLADE_BASE_X, LOWER_BLADE_BASE_Y, 0.0F);
        poseStack.scale(BLADE_WIDTH_SCALE, 1.0F, BLADE_WIDTH_SCALE);
        LightsaberBladeRenderer.render(
                data,
                stack,
                poseStack,
                buffer,
                inWorld,
                BLADE_LENGTH,
                false
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    public static void renderPart(
            PartType type,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ResourceLocation model = switch (type) {
            case EMITTER -> UPPER_EMITTER_MODEL;
            case SWITCH_SECTION -> SWITCH_MODEL;
            case BODY -> GRIP_MODEL;
            case POMMEL -> LOWER_EMITTER_MODEL;
        };
        float offsetX = switch (type) {
            case EMITTER -> UPPER_EMITTER_PART_OFFSET_X_CM;
            case SWITCH_SECTION -> SWITCH_PART_OFFSET_X_CM;
            case BODY -> 0.0F;
            case POMMEL -> LOWER_EMITTER_PART_OFFSET_X_CM;
        };
        float offsetY = switch (type) {
            case EMITTER -> UPPER_EMITTER_PART_OFFSET_Y_CM;
            case SWITCH_SECTION -> SWITCH_PART_OFFSET_Y_CM;
            case BODY -> 0.0F;
            case POMMEL -> LOWER_EMITTER_PART_OFFSET_Y_CM;
        };
        poseStack.pushPose();
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        poseStack.translate(-offsetX, -offsetY, 0.0F);
        renderModel(itemRenderer, modelManager.getModel(model), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderModel(
            ItemRenderer itemRenderer,
            BakedModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        itemRenderer.renderModelLists(
                model,
                ItemStack.EMPTY,
                packedLight,
                packedOverlay,
                poseStack,
                buffer.getBuffer(MODEL_RENDER_TYPE)
        );
    }

    private static float getRotation(Minecraft minecraft, LightsaberData data) {
        if (minecraft.level == null
                || minecraft.player == null
                || !minecraft.player.isUsingItem()) {
            return 0.0F;
        }
        ItemStack usingStack = minecraft.player.getUseItem();
        if (!ItemLightsaberBase.isActive(usingStack)
                || !ItemLightsaberBase.isSpinningLightsaber(usingStack)
                || LightsaberData.get(usingStack).hash != data.hash) {
            return 0.0F;
        }
        return (minecraft.level.getGameTime() + minecraft.getFrameTime())
                * ROTATION_DEGREES_PER_TICK % 360.0F;
    }

    private static ResourceLocation model(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Lightsabers.MODID,
                "item/spinning/" + name
        );
    }
}
