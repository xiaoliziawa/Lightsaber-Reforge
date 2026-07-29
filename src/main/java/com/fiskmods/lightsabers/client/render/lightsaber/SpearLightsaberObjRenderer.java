package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
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

public final class SpearLightsaberObjRenderer {
    public static final int BLADE_LENGTH = 26;

    private static final ResourceLocation EMITTER_MODEL = model("emitter_head");
    private static final ResourceLocation SWITCH_MODEL = model("switch_ring");
    private static final ResourceLocation GRIP_MODEL = model("grip");
    private static final ResourceLocation POMMEL_MODEL = model("pommel_cap");

    private static final float PIXELS_PER_MODEL_UNIT = 9.0F;
    private static final float HILT_DISPLAY_SCALE = 1.3F;
    private static final float MODEL_SCALE = PIXELS_PER_MODEL_UNIT / 16.0F * HILT_DISPLAY_SCALE;
    private static final float MODEL_ORIENTATION_ROTATION = 180.0F;
    private static final float HILT_CENTER_Y = 0.3344F;
    private static final float BLADE_SOCKET_Y = 2.375F;
    private static final float BLADE_MODEL_SCALE = 1.15F;
    private static final float EMITTER_CENTER_Y = 2.0656F;
    private static final float SWITCH_CENTER_Y = 1.2876F;
    private static final float BODY_CENTER_Y = -0.3906F;
    private static final float POMMEL_CENTER_Y = -1.7687F;
    private static final float EMITTER_EXTENT = 1.2437F;
    private static final float SWITCH_EXTENT = 0.3125F;
    private static final float BODY_EXTENT = 3.1188F;
    private static final float POMMEL_EXTENT = 0.5F;
    private static final float EMITTER_PART_TARGET_BLOCKS = 1.25F;
    private static final float SWITCH_PART_TARGET_BLOCKS = 1.0F;
    private static final float BODY_PART_TARGET_BLOCKS = 2.4F;
    private static final float POMMEL_PART_TARGET_BLOCKS = 1.25F;
    private static final RenderType MODEL_RENDER_TYPE = RenderType.entityCutoutNoCull(
            TextureAtlas.LOCATION_BLOCKS
    );

    private SpearLightsaberObjRenderer() {
    }

    public static float getBladeModelLength() {
        return getSocketOffset()
                + LightsaberBladeRenderer.getBladeLength(BLADE_LENGTH) * BLADE_MODEL_SCALE;
    }

    private static float getSocketOffset() {
        return (BLADE_SOCKET_Y - HILT_CENTER_Y) * MODEL_SCALE;
    }

    public static float getPartScale(PartType type) {
        float extent = switch (type) {
            case EMITTER -> EMITTER_EXTENT;
            case SWITCH_SECTION -> SWITCH_EXTENT;
            case BODY -> BODY_EXTENT;
            case POMMEL -> POMMEL_EXTENT;
        };
        float targetBlocks = switch (type) {
            case EMITTER -> EMITTER_PART_TARGET_BLOCKS;
            case SWITCH_SECTION -> SWITCH_PART_TARGET_BLOCKS;
            case BODY -> BODY_PART_TARGET_BLOCKS;
            case POMMEL -> POMMEL_PART_TARGET_BLOCKS;
        };
        return targetBlocks / (extent * MODEL_SCALE);
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(EMITTER_MODEL);
        event.register(SWITCH_MODEL);
        event.register(GRIP_MODEL);
        event.register(POMMEL_MODEL);
    }

    public static boolean isSupported(LightsaberData data) {
        return data.isSpear();
    }

    public static void renderHilt(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.translate(0.0F, -HILT_CENTER_Y, 0.0F);
        renderModel(
                itemRenderer,
                modelManager.getModel(EMITTER_MODEL),
                stack,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderModel(
                itemRenderer,
                modelManager.getModel(SWITCH_MODEL),
                stack,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderModel(
                itemRenderer,
                modelManager.getModel(GRIP_MODEL),
                stack,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        renderModel(
                itemRenderer,
                modelManager.getModel(POMMEL_MODEL),
                stack,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    public static void renderBlade(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            boolean inWorld
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, -getSocketOffset(), 0.0F);
        poseStack.scale(BLADE_MODEL_SCALE, BLADE_MODEL_SCALE, BLADE_MODEL_SCALE);
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
            case EMITTER -> EMITTER_MODEL;
            case SWITCH_SECTION -> SWITCH_MODEL;
            case BODY -> GRIP_MODEL;
            case POMMEL -> POMMEL_MODEL;
        };
        float centerY = switch (type) {
            case EMITTER -> EMITTER_CENTER_Y;
            case SWITCH_SECTION -> SWITCH_CENTER_Y;
            case BODY -> BODY_CENTER_Y;
            case POMMEL -> POMMEL_CENTER_Y;
        };
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.translate(0.0F, -centerY, 0.0F);
        renderModel(
                itemRenderer,
                modelManager.getModel(model),
                ItemStack.EMPTY,
                poseStack,
                buffer,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private static void renderModel(
            ItemRenderer itemRenderer,
            BakedModel model,
            ItemStack stack,
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
                ItemRenderer.getFoilBuffer(
                        buffer,
                        MODEL_RENDER_TYPE,
                        true,
                        stack.hasFoil()
                )
        );
    }

    private static ResourceLocation model(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Lightsabers.MODID,
                "item/spear/" + name
        );
    }
}
