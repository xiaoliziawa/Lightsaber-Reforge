package com.fiskmods.lightsabers.client.render.tile;

import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystalDisplayStand;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class RenderCrystalDisplayStand
        implements BlockEntityRenderer<TileEntityCrystalDisplayStand> {
    private static final float DISPLAY_Y = 1.12F;
    private static final float DISPLAY_RAISE = 10.0F / 16.0F;
    private static final float DISPLAY_SCALE = 0.65F;
    private static final float BOB_AMPLITUDE = 0.035F;
    private static final float BOB_SPEED = 0.08F;

    private final ItemRenderer itemRenderer;

    public RenderCrystalDisplayStand(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            TileEntityCrystalDisplayStand stand,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack displayStack = stand.getDisplayStack();
        if (displayStack.isEmpty()) {
            return;
        }

        float time = stand.getLevel() == null
                ? partialTick
                : stand.getLevel().getGameTime() + partialTick;
        float phase = (float) (stand.getBlockPos().getX() * 0.37D
                + stand.getBlockPos().getZ() * 0.23D);
        float bob = Mth.sin(time * BOB_SPEED + phase) * BOB_AMPLITUDE;

        poseStack.pushPose();
        poseStack.translate(0.5F, DISPLAY_Y + DISPLAY_RAISE + bob, 0.5F);
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        if (displayStack.getItem() instanceof ItemLightsaberBase) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        }
        itemRenderer.renderStatic(
                displayStack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                Minecraft.getInstance().level,
                (int) stand.getBlockPos().asLong()
        );
        poseStack.popPose();
    }
}
