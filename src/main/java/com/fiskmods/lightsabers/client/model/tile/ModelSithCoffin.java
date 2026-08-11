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

public class ModelSithCoffin {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Lightsabers.MODID, "sith_coffin"),
            "main"
    );

    private final ModelPart base;
    private final ModelPart lid;

    public ModelSithCoffin(ModelPart root) {
        base = root.getChild("base");
        lid = root.getChild("lid");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(160, 0)
                        .addBox(-8.0F, -3.0F, -8.0F, 16.0F, 3.0F, 32.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        PartDefinition base2 = base.addOrReplaceChild(
                "base2",
                CubeListBuilder.create()
                        .texOffs(110, 0)
                        .addBox(-6.0F, -2.0F, -14.0F, 12.0F, 2.0F, 28.0F),
                PartPose.offset(0.0F, -3.0F, 8.0F)
        );
        base2.addOrReplaceChild(
                "base3",
                CubeListBuilder.create()
                        .texOffs(0, 90)
                        .addBox(-5.5F, -6.0F, -12.0F, 11.0F, 6.0F, 24.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F)
        );
        PartDefinition base4 = base2.addOrReplaceChild(
                "base4",
                CubeListBuilder.create()
                        .texOffs(183, 36)
                        .addBox(-11.0F, 4.19615F, -14.0F, 8.0F, 1.0F, 28.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, 0.0F, 1.0471976F)
        );
        base4.addOrReplaceChild(
                "base5",
                CubeListBuilder.create()
                        .texOffs(172, 0)
                        .addBox(2.19615F, 1.0F, 0.0F, 3.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -13.98F, 0.0F, 0.0F, 1.5707964F)
        );
        base4.addOrReplaceChild(
                "base6",
                CubeListBuilder.create()
                        .texOffs(172, 0)
                        .addBox(3.0F, 1.25F, -1.0F, 3.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, 13.98F, 0.0F, 0.0F, 1.5707964F)
        );
        PartDefinition base7 = base2.addOrReplaceChild(
                "base7",
                CubeListBuilder.create()
                        .texOffs(183, 36)
                        .mirror()
                        .addBox(0.0F, -1.0F, -14.0F, 8.0F, 1.0F, 28.0F),
                PartPose.offsetAndRotation(6.0F, -2.0F, 0.0F, 0.0F, 0.0F, -1.0471976F)
        );
        base7.addOrReplaceChild(
                "base8",
                CubeListBuilder.create()
                        .texOffs(172, 0)
                        .mirror()
                        .addBox(0.0F, -2.0F, 0.0F, 3.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -1.0F, -13.98F, 0.0F, 0.0F, -1.5707964F)
        );
        base7.addOrReplaceChild(
                "base9",
                CubeListBuilder.create()
                        .texOffs(172, 0)
                        .mirror()
                        .addBox(0.0F, -2.0F, -1.0F, 3.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(3.0F, -1.0F, 13.98F, 0.0F, 0.0F, -1.5707964F)
        );
        PartDefinition base10 = base2.addOrReplaceChild(
                "base10",
                CubeListBuilder.create()
                        .texOffs(107, 0)
                        .addBox(-6.0F, -6.0F, -0.5F, 12.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, -13.5F)
        );
        PartDefinition base11 = base10.addOrReplaceChild(
                "base11",
                CubeListBuilder.create()
                        .texOffs(58, 0)
                        .addBox(-10.0F, -2.0F, -1.0F, 20.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -5.5F, 0.0F)
        );
        base11.addOrReplaceChild(
                "base12",
                CubeListBuilder.create()
                        .texOffs(0, 30)
                        .mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 25.0F),
                PartPose.offset(9.0F, -1.0F, 1.0F)
        );
        base11.addOrReplaceChild(
                "base13",
                CubeListBuilder.create()
                        .texOffs(0, 30)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 25.0F),
                PartPose.offset(-9.0F, -1.0F, 1.0F)
        );
        base11.addOrReplaceChild(
                "base14",
                CubeListBuilder.create()
                        .texOffs(58, 0)
                        .addBox(-10.0F, -2.0F, -1.0F, 20.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 27.0F, 0.0F, (float) Math.PI, 0.0F)
        );
        base2.addOrReplaceChild(
                "base15",
                CubeListBuilder.create()
                        .texOffs(107, 0)
                        .addBox(-6.0F, -6.0F, -0.5F, 12.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -2.0F, 13.5F, 0.0F, (float) Math.PI, 0.0F)
        );

        PartDefinition lid = root.addOrReplaceChild(
                "lid",
                CubeListBuilder.create()
                        .texOffs(0, 60)
                        .addBox(1.5F, -2.3F, -13.0F, 15.0F, 1.0F, 26.0F),
                PartPose.offset(-9.0F, 11.5F, 8.0F)
        );
        lid.addOrReplaceChild(
                "lid2",
                CubeListBuilder.create()
                        .texOffs(64, 28)
                        .mirror()
                        .addBox(-1.0F, 0.0F, -13.0F, 1.0F, 3.0F, 26.0F),
                PartPose.offsetAndRotation(16.5F, -2.3F, 0.0F, 0.0F, 0.0F, -0.6981317F)
        );
        lid.addOrReplaceChild(
                "lid3",
                CubeListBuilder.create()
                        .texOffs(64, 28)
                        .addBox(0.0F, 0.0F, -13.0F, 1.0F, 3.0F, 26.0F),
                PartPose.offsetAndRotation(1.5F, -2.3F, 0.0F, 0.0F, 0.0F, 0.6981317F)
        );
        lid.addOrReplaceChild(
                "lid4",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -0.5F, 16.0F, 2.0F, 1.0F),
                PartPose.offset(9.0F, -1.3F, -12.5F)
        );
        lid.addOrReplaceChild(
                "lid5",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, 0.0F, -0.5F, 16.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(9.0F, -1.3F, 12.5F, 0.0F, (float) Math.PI, 0.0F)
        );
        return LayerDefinition.create(mesh, 256, 128);
    }

    public void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            float openProgress
    ) {
        float closingPhase = openProgress > 0.5F
                ? (1.0F - openProgress) * 2.0F
                : 1.0F;
        float movement = 1.0F - closingPhase;
        lid.x = -9.0F + 16.0F * openProgress;
        lid.y = 11.5F - 3.0F * movement;
        lid.z = 8.0F;
        lid.zRot = movement;
        lid.render(poseStack, consumer, packedLight, packedOverlay);
        base.render(poseStack, consumer, packedLight, packedOverlay);
    }
}
