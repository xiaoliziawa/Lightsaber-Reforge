package com.fiskmods.lightsabers.client.render;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.block.HolocronType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public final class HolocronObjRenderer {
    private static final StandaloneModelKey<BlockStateModelPart> SITH_BOTTOM_MODEL =
            modelKey("sith_bottom");
    private static final StandaloneModelKey<BlockStateModelPart> SITH_SIDE_MODEL =
            modelKey("sith_side");
    private static final StandaloneModelKey<BlockStateModelPart> JEDI_FACE_MODEL =
            modelKey("jedi_face");
    private static final StandaloneModelKey<BlockStateModelPart> JEDI_FRAME_MODEL =
            modelKey("jedi_frame");
    private static final StandaloneModelKey<BlockStateModelPart> JEDI_CORNER_MODEL =
            modelKey("jedi_corner");
    private static final Identifier SITH_ITEM_TEXTURE = texture("sith_holocron");
    private static final Identifier JEDI_ITEM_TEXTURE = texture("jedi_holocron");
    private static final RenderType MODEL_RENDER_TYPE = RenderTypes.entityTranslucent(
            TextureAtlas.LOCATION_BLOCKS,
            false
    );
    private static final int[] NO_TINTS = new int[0];
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

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        register(event, SITH_BOTTOM_MODEL, "sith_bottom");
        register(event, SITH_SIDE_MODEL, "sith_side");
        register(event, JEDI_FACE_MODEL, "jedi_face");
        register(event, JEDI_FRAME_MODEL, "jedi_frame");
        register(event, JEDI_CORNER_MODEL, "jedi_corner");
    }

    public static void renderModel(
            HolocronType type,
            float openProgress,
            float openTicks,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedOverlay
    ) {
        if (type == HolocronType.JEDI) {
            renderJedi(openProgress, openTicks, poseStack, collector, packedOverlay);
        } else {
            renderSith(poseStack, collector, packedOverlay);
        }
    }

    public static void renderItemIcon(
            HolocronType type,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedOverlay
    ) {
        Identifier texture = type == HolocronType.JEDI
                ? JEDI_ITEM_TEXTURE
                : SITH_ITEM_TEXTURE;
        RenderSubmissionHelper.submitGeometry(
                collector,
                poseStack,
                RenderTypes.entityTranslucentEmissive(texture, false),
                (renderPose, consumer) -> renderItemIconQuad(
                        renderPose,
                        consumer,
                        packedOverlay
                )
        );
    }

    private static void renderSith(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedOverlay
    ) {
        renderPart(SITH_BOTTOM_MODEL, poseStack, collector, packedOverlay);

        for (int sideIndex = 0; sideIndex < 4; sideIndex++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(sideIndex * 90.0F));
            poseStack.translate(0.0F, SITH_SIDE_CENTER_Y, SITH_SIDE_CENTER_Z);
            renderPart(SITH_SIDE_MODEL, poseStack, collector, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderJedi(
            float openProgress,
            float openTicks,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedOverlay
    ) {
        renderJediCore(poseStack, collector, packedOverlay);
        renderJediFrames(poseStack, collector, packedOverlay);

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
                poseStack.translate(0.0F, 0.0F, cornerOffset);
                renderPart(JEDI_CORNER_MODEL, poseStack, collector, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    private static void renderJediCore(
            PoseStack poseStack,
            SubmitNodeCollector collector,
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
            renderPart(JEDI_FACE_MODEL, poseStack, collector, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderJediFrames(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedOverlay
    ) {
        for (int verticalSide = 0; verticalSide < 2; verticalSide++) {
            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(cornerIndex * 90.0F));
                if (verticalSide == 1) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                }
                renderPart(JEDI_FRAME_MODEL, poseStack, collector, packedOverlay);
                poseStack.popPose();
            }
        }
    }

    private static void renderPart(
            StandaloneModelKey<BlockStateModelPart> key,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedOverlay
    ) {
        BlockStateModelPart model = Minecraft.getInstance()
                .getModelManager()
                .getStandaloneModel(key);
        if (model == null) {
            return;
        }
        collector.submitBlockModel(
                poseStack,
                MODEL_RENDER_TYPE,
                List.of(model),
                NO_TINTS,
                LightCoordsUtil.FULL_BRIGHT,
                packedOverlay,
                0
        );
    }

    private static void renderItemIconQuad(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedOverlay
    ) {
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F, packedOverlay);
        vertex(consumer, pose, 0.5F, -0.5F, 0.0F, 1.0F, 1.0F, packedOverlay);
        vertex(consumer, pose, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F, packedOverlay);
        vertex(consumer, pose, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F, packedOverlay);
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
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static StandaloneModelKey<BlockStateModelPart> modelKey(String name) {
        Identifier id = modelId(name);
        return new StandaloneModelKey<>(id::toString);
    }

    private static void register(
            ModelEvent.RegisterStandalone event,
            StandaloneModelKey<BlockStateModelPart> key,
            String name
    ) {
        event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(modelId(name)));
    }

    private static Identifier modelId(String name) {
        return Identifier.fromNamespaceAndPath(
                Lightsabers.MODID,
                "block/holocron/" + name
        );
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(
                Lightsabers.MODID,
                "textures/item/" + name + ".png"
        );
    }
}
