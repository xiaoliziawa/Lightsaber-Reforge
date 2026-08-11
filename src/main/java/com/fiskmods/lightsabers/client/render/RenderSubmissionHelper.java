package com.fiskmods.lightsabers.client.render;

import com.fiskmods.lightsabers.client.integration.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;

public final class RenderSubmissionHelper {
    private static final int[] NO_TINTS = new int[0];
    private static final Map<QuadCollection, List<BakedQuad>> IRIS_COMPATIBLE_QUADS =
            new WeakHashMap<>();
    private static final Function<Identifier, RenderType> IRIS_ITEM_CUTOUT =
            Util.memoize(texture -> createIrisItemRenderType(texture, false));
    private static final Function<Identifier, RenderType> IRIS_ITEM_TRANSLUCENT =
            Util.memoize(texture -> createIrisItemRenderType(texture, true));

    private RenderSubmissionHelper() {
    }

    public static void submitGeometry(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType renderType,
            GeometryRenderer renderer
    ) {
        submitGeometry(collector, poseStack, renderType, false, renderer);
    }

    public static void submitShaderEffectGeometry(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType renderType,
            GeometryRenderer renderer
    ) {
        submitGeometry(collector, poseStack, renderType, true, renderer);
    }

    private static void submitGeometry(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType renderType,
            boolean skipIrisShadowPass,
            GeometryRenderer renderer
    ) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            if (skipIrisShadowPass && IrisCompat.isRenderingShadowPass()) {
                return;
            }
            PoseStack renderPose = new PoseStack();
            renderPose.last().set(pose);
            renderer.render(renderPose, consumer);
        });
    }

    public static void submitQuads(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            QuadCollection model,
            int packedLight,
            int packedOverlay,
            boolean hasFoil,
            int outlineColor
    ) {
        if (model == null || model.getAll().isEmpty()) {
            return;
        }
        List<BakedQuad> quads = IrisCompat.isShaderPackInUse()
                ? IRIS_COMPATIBLE_QUADS.computeIfAbsent(
                        model,
                        RenderSubmissionHelper::createIrisCompatibleQuads
                )
                : model.getAll();
        collector.submitItem(
                poseStack,
                ItemDisplayContext.NONE,
                packedLight,
                packedOverlay,
                outlineColor,
                NO_TINTS,
                quads,
                hasFoil
                        ? ItemStackRenderState.FoilType.STANDARD
                        : ItemStackRenderState.FoilType.NONE
        );
    }

    private static List<BakedQuad> createIrisCompatibleQuads(QuadCollection model) {
        List<BakedQuad> source = model.getAll();
        List<BakedQuad> quads = new ArrayList<>(source.size());
        for (BakedQuad quad : source) {
            BakedQuad.MaterialInfo material = quad.materialInfo();
            Identifier atlas = material.sprite().atlasLocation();
            RenderType renderType = material.itemRenderType().hasBlending()
                    ? IRIS_ITEM_TRANSLUCENT.apply(atlas)
                    : IRIS_ITEM_CUTOUT.apply(atlas);
            BakedQuad.MaterialInfo compatibleMaterial = new BakedQuad.MaterialInfo(
                    material.sprite(),
                    material.layer(),
                    renderType,
                    material.tintIndex(),
                    material.shade(),
                    material.lightEmission(),
                    material.ambientOcclusion()
            );
            quads.add(new BakedQuad(
                    quad.position0(),
                    quad.position1(),
                    quad.position2(),
                    quad.position3(),
                    quad.packedUV0(),
                    quad.packedUV1(),
                    quad.packedUV2(),
                    quad.packedUV3(),
                    quad.direction(),
                    compatibleMaterial,
                    quad.bakedNormals(),
                    quad.bakedColors()
            ));
        }
        return List.copyOf(quads);
    }

    private static RenderType createIrisItemRenderType(
            Identifier texture,
            boolean translucent
    ) {
        RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(
                        translucent
                                ? RenderPipelines.ITEM_TRANSLUCENT
                                : RenderPipelines.ITEM_CUTOUT
                )
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
        if (translucent) {
            setup.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET).sortOnUpload();
        }
        return RenderType.create(
                translucent
                        ? "lightsabers_iris_item_translucent"
                        : "lightsabers_iris_item_cutout",
                setup.createRenderSetup()
        );
    }

    @FunctionalInterface
    public interface GeometryRenderer {
        void render(PoseStack poseStack, VertexConsumer consumer);
    }
}
