package com.fiskmods.lightsabers.client.model.tile;

import com.fiskmods.lightsabers.Lightsabers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class ModelCrystal {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Lightsabers.MODID, "lightsaber_crystal"),
            "main"
    );

    private final ModelPart root;

    public ModelCrystal(ModelPart root) {
        this.root = root;
    }

    public ModelPart root() {
        return root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "shape1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(
                        -1.0F,
                        24.3F,
                        0.0F,
                        -0.17453292F,
                        0.0F,
                        -0.17453292F
                )
        );
        root.addOrReplaceChild(
                "shape2",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(
                        -0.8F,
                        24.3F,
                        -0.9F,
                        0.34906584F,
                        0.33161256F,
                        -0.06981317F
                )
        );
        root.addOrReplaceChild(
                "shape3",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offsetAndRotation(
                        0.0F,
                        24.3F,
                        0.0F,
                        0.08726646F,
                        0.0F,
                        0.10471976F
                )
        );
        root.addOrReplaceChild(
                "shape4",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, -4.0F, -1.0F, 1.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(
                        0.0F,
                        24.3F,
                        1.0F,
                        -0.2443461F,
                        1.1693707F,
                        0.34906584F
                )
        );
        root.addOrReplaceChild(
                "shape5",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(
                        0.0F,
                        24.3F,
                        0.5F,
                        -0.5235988F,
                        -0.40142572F,
                        0.12217305F
                )
        );
        root.addOrReplaceChild(
                "shape6",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, -3.0F, -0.5F, 2.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(
                        0.5F,
                        24.0F,
                        -1.5F,
                        0.20943952F,
                        -0.82030475F,
                        0.12217305F
                )
        );
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        root.render(
                poseStack,
                consumer,
                packedLight,
                packedOverlay,
                ARGB.colorFromFloat(alpha, red, green, blue)
        );
    }
}
