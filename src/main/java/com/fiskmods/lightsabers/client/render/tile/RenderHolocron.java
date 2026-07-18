package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.tileentity.TileEntityHolocron;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderHolocron implements BlockEntityRenderer<TileEntityHolocron> {
    private static final float BASE_SCALE = 0.75F;

    private final ItemRenderer itemRenderer;
    private final ItemStack displayStack;

    public RenderHolocron(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
        displayStack = new ItemStack(ModBlocks.HOLOCRON_ITEM.get());
    }

    @Override
    public void render(
            TileEntityHolocron holocron,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        float openProgress = holocron.getOpenProgress(partialTick);
        float openTicks = holocron.getOpenTicks(partialTick);
        float hoverOffset = Mth.sin(openTicks / 10.0F) / 20.0F;
        float scale = BASE_SCALE;

        poseStack.pushPose();
        poseStack.translate(
                0.5F,
                0.25F + (0.5F + hoverOffset) * openProgress,
                0.5F
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F * openProgress));
        poseStack.scale(scale, scale, scale);
        itemRenderer.renderStatic(
                displayStack,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                poseStack,
                buffer,
                holocron.getLevel(),
                (int) holocron.getBlockPos().asLong()
        );
        poseStack.popPose();
    }
}
