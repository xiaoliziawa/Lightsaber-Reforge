package com.fiskmods.lightsabers.client.render;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.block.HolocronType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;

public final class HolocronObjRenderer {
    private static final ModelResourceLocation SITH_BOTTOM_MODEL = model("sith_bottom");
    private static final ModelResourceLocation SITH_SIDE_MODEL = model("sith_side");
    private static final ModelResourceLocation JEDI_FACE_MODEL = model("jedi_face");
    private static final ModelResourceLocation JEDI_FRAME_MODEL = model("jedi_frame");
    private static final ModelResourceLocation JEDI_CORNER_MODEL = model("jedi_corner");
    private static final ResourceLocation SITH_ITEM_TEXTURE = texture("sith_holocron");
    private static final ResourceLocation JEDI_ITEM_TEXTURE = texture("jedi_holocron");
    private static final RenderType MODEL_RENDER_TYPE = RenderType.entityTranslucent(
            InventoryMenu.BLOCK_ATLAS,
            false
    );
    private static final float SITH_SIDE_CENTER_Y = -1.0F / 12.0F;
    private static final float SITH_SIDE_CENTER_Z = -1.0F / 6.0F;
    private static final float JEDI_SIZE = 0.5F;
    private static final float JEDI_CORNER_BASE_OFFSET = 0.5775F;
    private static final float JEDI_CORNER_OPEN_OFFSET = 0.3F;
    private static final float JEDI_CORNER_TILT = 35.0F;
    private static final float JEDI_CORNER_ROTATION = 180.0F;
    private static final float HOVER_SPEED = 10.0F;
    private static final float HOVER_HEIGHT = 0.05F;

    private HolocronObjRenderer() {
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(SITH_BOTTOM_MODEL);
        event.register(SITH_SIDE_MODEL);
        event.register(JEDI_FACE_MODEL);
        event.register(JEDI_FRAME_MODEL);
        event.register(JEDI_CORNER_MODEL);
    }

    public static void renderModel(
            HolocronType type,
            float openProgress,
            float openTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        VertexConsumer consumer = buffer.getBuffer(MODEL_RENDER_TYPE);
        if (type == HolocronType.JEDI) {
            renderJedi(
                    openProgress,
                    openTicks,
                    poseStack,
                    itemRenderer,
                    modelManager,
                    consumer,
                    packedOverlay
            );
        } else {
            renderSith(
                    poseStack,
                    itemRenderer,
                    modelManager,
                    consumer,
                    packedOverlay
            );
        }
    }

    public static void renderItemIcon(
            HolocronType type,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedOverlay
    ) {
        ResourceLocation texture = type == HolocronType.JEDI
                ? JEDI_ITEM_TEXTURE
                : SITH_ITEM_TEXTURE;
        VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityTranslucentEmissive(texture, false)
        );
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, -0.5F, -0.5F, 0, 0, 1, packedOverlay);
        vertex(consumer, pose, 0.5F, -0.5F, 0, 1, 1, packedOverlay);
        vertex(consumer, pose, 0.5F, 0.5F, 0, 1, 0, packedOverlay);
        vertex(consumer, pose, -0.5F, 0.5F, 0, 0, 0, packedOverlay);
    }

    private static void renderSith(
            PoseStack poseStack,
            ItemRenderer itemRenderer,
            ModelManager modelManager,
            VertexConsumer consumer,
            int packedOverlay
    ) {
        BakedModel bottom = modelManager.getModel(SITH_BOTTOM_MODEL);
        BakedModel side = modelManager.getModel(SITH_SIDE_MODEL);
        renderPart(itemRenderer, bottom, poseStack, consumer, packedOverlay);

        for (int sideIndex = 0; sideIndex < 4; sideIndex++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(sideIndex * 90.0F));
            poseStack.translate(0, SITH_SIDE_CENTER_Y, SITH_SIDE_CENTER_Z);
            renderPart(itemRenderer, side, poseStack, consumer, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderJedi(
            float openProgress,
            float openTicks,
            PoseStack poseStack,
            ItemRenderer itemRenderer,
            ModelManager modelManager,
            VertexConsumer consumer,
            int packedOverlay
    ) {
        BakedModel face = modelManager.getModel(JEDI_FACE_MODEL);
        BakedModel frame = modelManager.getModel(JEDI_FRAME_MODEL);
        BakedModel corner = modelManager.getModel(JEDI_CORNER_MODEL);

        renderJediCore(itemRenderer, face, poseStack, consumer, packedOverlay);
        renderJediFrames(itemRenderer, frame, poseStack, consumer, packedOverlay);

        float hoverOffset = Mth.sin(openTicks / HOVER_SPEED) * HOVER_HEIGHT;
        float cornerOffset = JEDI_SIZE * (
                JEDI_CORNER_BASE_OFFSET
                        + openProgress * (JEDI_CORNER_OPEN_OFFSET + hoverOffset)
        );
        float cornerRotation = JEDI_CORNER_ROTATION * openProgress;
        for (int verticalSide = 0; verticalSide < 2; verticalSide++) {
            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F + cornerIndex * 90.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(JEDI_CORNER_TILT));
                poseStack.mulPose(Axis.ZP.rotationDegrees(cornerRotation));
                if (verticalSide == 1) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                }
                poseStack.translate(0, 0, cornerOffset);
                renderPart(itemRenderer, corner, poseStack, consumer, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    private static void renderJediCore(
            ItemRenderer itemRenderer,
            BakedModel face,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedOverlay
    ) {
        for (int faceIndex = 0; faceIndex < 6; faceIndex++) {
            poseStack.pushPose();
            if (faceIndex < 4) {
                poseStack.mulPose(Axis.YP.rotationDegrees(faceIndex * 90.0F));
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(
                        (faceIndex - 4) * 180.0F + 90.0F
                ));
            }
            renderPart(itemRenderer, face, poseStack, consumer, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderJediFrames(
            ItemRenderer itemRenderer,
            BakedModel frame,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedOverlay
    ) {
        for (int verticalSide = 0; verticalSide < 2; verticalSide++) {
            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(cornerIndex * 90.0F));
                if (verticalSide == 1) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                }
                renderPart(itemRenderer, frame, poseStack, consumer, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    private static void renderPart(
            ItemRenderer itemRenderer,
            BakedModel model,
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedOverlay
    ) {
        itemRenderer.renderModelLists(
                model,
                ItemStack.EMPTY,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                poseStack,
                consumer
        );
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int packedOverlay
    ) {
        consumer.addVertex(pose.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0, 0, 1);
    }

    private static ModelResourceLocation model(String name) {
        return ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                Lightsabers.MODID,
                "block/holocron/" + name
        ));
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Lightsabers.MODID,
                "textures/item/" + name + ".png"
        );
    }
}
