package com.fiskmods.lightsabers.client.model;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.entity.EntitySithGhost;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class ModelSithGhost extends HumanoidModel<EntitySithGhost> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, "sith_ghost"),
            "main"
    );

    public ModelSithGhost(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-4.5F, -8.0F, -4.5F, 9.0F, 9.0F, 1.0F),
                PartPose.ZERO
        );
        head.addOrReplaceChild(
                "hood",
                CubeListBuilder.create()
                        .texOffs(0, 27)
                        .mirror()
                        .addBox(-5.5F, -9.4F, -3.6F, 11.0F, 11.0F, 9.0F),
                PartPose.rotation(0.0842994F, 0.0F, 0.0F)
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(22, 10)
                        .mirror()
                        .addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(48, 10)
                        .addBox(-4.0F, -2.0F, -1.9F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-4.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(48, 10)
                        .mirror()
                        .addBox(0.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(4.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.3F, 0.0F, -2.5F, 6.0F, 12.0F, 5.0F),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.05235988F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .mirror()
                        .addBox(-3.7F, 0.0F, -2.5F, 6.0F, 12.0F, 5.0F),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.05235988F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(
            EntitySithGhost entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        super.setupAnim(
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch
        );
        rightArm.setPos(-4.0F, 2.0F, 0.0F);
        leftArm.setPos(4.0F, 2.0F, 0.0F);
    }
}
