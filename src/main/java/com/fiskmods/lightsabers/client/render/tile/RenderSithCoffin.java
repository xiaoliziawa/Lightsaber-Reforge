package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.model.tile.ModelSithCoffin;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithCoffin;
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

public class RenderSithCoffin implements BlockEntityRenderer<TileEntitySithCoffin> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/models/sith_coffin.png"
    );

    private final ModelSithCoffin model;

    public RenderSithCoffin(BlockEntityRendererProvider.Context context) {
        model = new ModelSithCoffin(context.bakeLayer(ModelSithCoffin.LAYER));
    }

    @Override
    public void render(
            TileEntitySithCoffin coffin,
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
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot() + 180.0F));
        model.render(
                poseStack,
                consumer,
                packedLight,
                packedOverlay,
                coffin.getLidOpenProgress(partialTick)
        );
        poseStack.popPose();
    }
}
