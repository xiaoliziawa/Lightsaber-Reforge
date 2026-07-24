package com.fiskmods.lightsabers.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class LegacyModelRenderer {
    private static final float VANILLA_MODEL_SCALE = 16.0F;
    private static final boolean RECORD_EXPORT_DATA = Boolean.getBoolean(
            "lightsabers.recordModelExportData"
    );
    private static final Set<Direction> ALL_FACES = Collections.unmodifiableSet(
            EnumSet.allOf(Direction.class)
    );

    private final LegacyModelBase owner;
    private final int textureOffsetX;
    private final int textureOffsetY;
    private final List<ModelPart.Cube> cubes = new ArrayList<>();
    private final List<CubeDefinition> cubeDefinitions = RECORD_EXPORT_DATA
            ? new ArrayList<>()
            : Collections.emptyList();
    private final List<LegacyModelRenderer> children = new ArrayList<>();
    private final Quaternionf cachedRotation = new Quaternionf();

    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public boolean mirror;
    public boolean isHidden;
    public boolean showModel = true;

    private float cachedRotationX = Float.NaN;
    private float cachedRotationY = Float.NaN;
    private float cachedRotationZ = Float.NaN;

    public LegacyModelRenderer(LegacyModelBase owner, int textureOffsetX, int textureOffsetY) {
        this.owner = owner;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
    }

    public void setRotationPoint(float x, float y, float z) {
        rotationPointX = x;
        rotationPointY = y;
        rotationPointZ = z;
    }

    public void addBox(
            float x,
            float y,
            float z,
            int width,
            int height,
            int depth,
            float inflation
    ) {
        if (RECORD_EXPORT_DATA) {
            cubeDefinitions.add(new CubeDefinition(
                    textureOffsetX,
                    textureOffsetY,
                    x,
                    y,
                    z,
                    width,
                    height,
                    depth,
                    inflation,
                    mirror
            ));
        }
        cubes.add(new ModelPart.Cube(
                textureOffsetX,
                textureOffsetY,
                x,
                y,
                z,
                width,
                height,
                depth,
                inflation,
                inflation,
                inflation,
                mirror,
                owner.textureWidth,
                owner.textureHeight,
                ALL_FACES
        ));
    }

    public void addChild(LegacyModelRenderer child) {
        children.add(child);
    }

    public List<CubeDefinition> getCubeDefinitions() {
        return Collections.unmodifiableList(cubeDefinitions);
    }

    public List<LegacyModelRenderer> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void render(float scale) {
        if (isHidden || !showModel) {
            return;
        }

        LegacyRenderContext.State context = LegacyRenderContext.get();
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.translate(
                rotationPointX * scale,
                rotationPointY * scale,
                rotationPointZ * scale
        );
        if (rotateAngleX != 0.0F || rotateAngleY != 0.0F || rotateAngleZ != 0.0F) {
            poseStack.mulPose(getRotation());
        }

        if (!cubes.isEmpty()) {
            poseStack.pushPose();
            float modelScale = scale * VANILLA_MODEL_SCALE;
            poseStack.scale(modelScale, modelScale, modelScale);
            PoseStack.Pose pose = poseStack.last();
            for (ModelPart.Cube cube : cubes) {
                cube.compile(
                        pose,
                        context.consumer(),
                        context.packedLight(),
                        context.packedOverlay(),
                        FastColor.ARGB32.colorFromFloat(
                                context.alpha(),
                                context.red(),
                                context.green(),
                                context.blue()
                        )
                );
            }
            poseStack.popPose();
        }

        for (LegacyModelRenderer child : children) {
            child.render(scale);
        }
        poseStack.popPose();
    }

    private Quaternionf getRotation() {
        if (cachedRotationX != rotateAngleX
                || cachedRotationY != rotateAngleY
                || cachedRotationZ != rotateAngleZ) {
            cachedRotation.rotationZYX(rotateAngleZ, rotateAngleY, rotateAngleX);
            cachedRotationX = rotateAngleX;
            cachedRotationY = rotateAngleY;
            cachedRotationZ = rotateAngleZ;
        }
        return cachedRotation;
    }

    public record CubeDefinition(
            int textureOffsetX,
            int textureOffsetY,
            float x,
            float y,
            float z,
            int width,
            int height,
            int depth,
            float inflation,
            boolean mirror
    ) {
    }
}
