package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.tile.ModelSithStoneCoffin;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithStoneCoffin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class RenderSithStoneCoffin
        implements BlockEntityRenderer<TileEntitySithStoneCoffin> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/models/sith_stone_coffin.png"
    );

    private final ModelSithStoneCoffin model;

    public RenderSithStoneCoffin(BlockEntityRendererProvider.Context context) {
        model = new ModelSithStoneCoffin(
                context.bakeLayer(ModelSithStoneCoffin.LAYER)
        );
    }

    @Override
    public void render(
            TileEntitySithStoneCoffin coffin,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing = coffin.getBlockState().getValue(
                HorizontalDirectionalBlock.FACING
        );
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        model.render(
                poseStack,
                consumer,
                packedLight,
                packedOverlay,
                coffin.isBaseplateOnly()
        );
        poseStack.popPose();
    }
}
