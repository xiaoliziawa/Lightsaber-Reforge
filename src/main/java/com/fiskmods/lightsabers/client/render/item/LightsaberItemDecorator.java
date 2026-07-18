package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import org.joml.Matrix4f;

public class LightsaberItemDecorator implements IItemDecorator {
    private static final float TRIANGLE_SIZE = 4.0F;
    private static final float INVERTING_SHRINK = 1.5F;
    private static final float DECORATION_Z = 200.0F;
    private static final float[] BLACK = {0.0F, 0.0F, 0.0F};

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, DECORATION_Z);
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.gui());
        float small = TRIANGLE_SIZE / INVERTING_SHRINK;
        float smallOffset = small / 8.0F;

        if (stack.getItem() instanceof ItemDoubleLightsaber) {
            LightsaberData[] sabers = ItemDoubleLightsaber.get(stack);
            drawUpperTriangle(consumer, matrix, sabers[0].getRGB(stack), TRIANGLE_SIZE, 0.0F);
            drawLowerTriangle(consumer, matrix, sabers[1].getRGB(stack), TRIANGLE_SIZE, 0.0F);
            if (sabers[0].hasFocusingCrystal(FocusingCrystal.INVERTING)) {
                drawUpperTriangle(consumer, matrix, BLACK, small, smallOffset);
            }
            if (sabers[1].hasFocusingCrystal(FocusingCrystal.INVERTING)) {
                drawLowerTriangle(consumer, matrix, BLACK, small, smallOffset);
            }
        } else {
            LightsaberData data = LightsaberData.get(stack);
            drawWedge(consumer, matrix, data.getRGB(stack), TRIANGLE_SIZE, 0.0F);
            if (data.hasFocusingCrystal(FocusingCrystal.INVERTING)) {
                drawWedge(consumer, matrix, BLACK, small, smallOffset);
            }
        }
        graphics.flush();
        graphics.pose().popPose();
        return true;
    }

    private static void drawWedge(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] rgb,
            float size,
            float offset
    ) {
        vertex(consumer, matrix, offset, offset, rgb);
        vertex(consumer, matrix, offset, offset + size, rgb);
        vertex(consumer, matrix, offset + size / 2.0F, offset + size / 2.0F, rgb);
        vertex(consumer, matrix, offset + size, offset, rgb);
    }

    private static void drawUpperTriangle(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] rgb,
            float size,
            float offset
    ) {
        vertex(consumer, matrix, offset, offset, rgb);
        vertex(consumer, matrix, offset + size / 2.0F, offset + size / 2.0F, rgb);
        vertex(consumer, matrix, offset + size, offset, rgb);
        vertex(consumer, matrix, offset + size, offset, rgb);
    }

    private static void drawLowerTriangle(
            VertexConsumer consumer,
            Matrix4f matrix,
            float[] rgb,
            float size,
            float offset
    ) {
        vertex(consumer, matrix, offset, offset, rgb);
        vertex(consumer, matrix, offset, offset + size, rgb);
        vertex(consumer, matrix, offset + size / 2.0F, offset + size / 2.0F, rgb);
        vertex(consumer, matrix, offset + size / 2.0F, offset + size / 2.0F, rgb);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x,
            float y,
            float[] rgb
    ) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(rgb[0], rgb[1], rgb[2], 1.0F)
                .endVertex();
    }
}
