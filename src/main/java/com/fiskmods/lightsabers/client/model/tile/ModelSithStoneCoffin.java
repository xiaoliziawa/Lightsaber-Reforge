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
import net.minecraft.resources.ResourceLocation;

public class ModelSithStoneCoffin {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(
                    Lightsabers.MODID,
                    "sith_stone_coffin"
            ),
            "main"
    );

    private final ModelPart base;
    private final ModelPart coffin;

    public ModelSithStoneCoffin(ModelPart root) {
        base = root.getChild("base");
        coffin = base.getChild("coffin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -3.0F, -8.0F, 16.0F, 3.0F, 16.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );
        base.addOrReplaceChild(
                "coffin",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-6.5F, -4.5F, 0.0F, 13.0F, 9.0F, 28.0F),
                PartPose.offsetAndRotation(
                        0.0F,
                        -3.0F,
                        2.0F,
                        (float) Math.PI / 2.0F,
                        0.0F,
                        0.0F
                )
        );
        return LayerDefinition.create(mesh, 128, 128);
    }

    public void render(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            boolean baseplateOnly
    ) {
        coffin.visible = !baseplateOnly;
        base.render(poseStack, consumer, packedLight, packedOverlay);
    }
}
