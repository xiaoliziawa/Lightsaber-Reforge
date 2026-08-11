package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public class LightsaberItemDecorator implements IItemDecorator {
    private static final float TRIANGLE_SIZE = 4.0F;
    private static final float INVERTING_SHRINK = 1.5F;
    private static final float[] BLACK = {0.0F, 0.0F, 0.0F};

    @Override
    public boolean render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        float small = TRIANGLE_SIZE / INVERTING_SHRINK;
        float smallOffset = small / 8.0F;

        if (stack.getItem() instanceof ItemDoubleLightsaber) {
            LightsaberData[] sabers = ItemDoubleLightsaber.get(stack);
            submitWedge(
                    graphics,
                    pose,
                    WedgeShape.UPPER,
                    sabers[0].getRGB(stack),
                    TRIANGLE_SIZE,
                    0.0F
            );
            submitWedge(
                    graphics,
                    pose,
                    WedgeShape.LOWER,
                    sabers[1].getRGB(stack),
                    TRIANGLE_SIZE,
                    0.0F
            );
            if (sabers[0].hasFocusingCrystal(FocusingCrystal.INVERTING)) {
                submitWedge(
                        graphics,
                        pose,
                        WedgeShape.UPPER,
                        BLACK,
                        small,
                        smallOffset
                );
            }
            if (sabers[1].hasFocusingCrystal(FocusingCrystal.INVERTING)) {
                submitWedge(
                        graphics,
                        pose,
                        WedgeShape.LOWER,
                        BLACK,
                        small,
                        smallOffset
                );
            }
        } else {
            LightsaberData data = LightsaberData.get(stack);
            submitWedge(
                    graphics,
                    pose,
                    WedgeShape.FULL,
                    data.getRGB(stack),
                    TRIANGLE_SIZE,
                    0.0F
            );
            if (data.hasFocusingCrystal(FocusingCrystal.INVERTING)) {
                submitWedge(
                        graphics,
                        pose,
                        WedgeShape.FULL,
                        BLACK,
                        small,
                        smallOffset
                );
            }
        }
        graphics.pose().popMatrix();
        return false;
    }

    private static void submitWedge(
            GuiGraphicsExtractor graphics,
            Matrix3x2f pose,
            WedgeShape shape,
            float[] rgb,
            float size,
            float offset
    ) {
        graphics.submitGuiElementRenderState(new WedgeRenderState(
                pose,
                shape,
                size,
                offset,
                ARGB.colorFromFloat(1.0F, rgb[0], rgb[1], rgb[2]),
                graphics.peekScissorStack()
        ));
    }

    private enum WedgeShape {
        FULL,
        UPPER,
        LOWER
    }

    private record WedgeRenderState(
            Matrix3x2f pose,
            WedgeShape shape,
            float size,
            float offset,
            int color,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        private WedgeRenderState(
                Matrix3x2f pose,
                WedgeShape shape,
                float size,
                float offset,
                int color,
                @Nullable ScreenRectangle scissorArea
        ) {
            this(
                    pose,
                    shape,
                    size,
                    offset,
                    color,
                    scissorArea,
                    getBounds(pose, size, offset, scissorArea)
            );
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            float middle = offset + size / 2.0F;
            switch (shape) {
                case FULL -> {
                    vertex(consumer, offset, offset);
                    vertex(consumer, offset, offset + size);
                    vertex(consumer, middle, middle);
                    vertex(consumer, offset + size, offset);
                }
                case UPPER -> {
                    vertex(consumer, offset, offset);
                    vertex(consumer, middle, middle);
                    vertex(consumer, offset + size, offset);
                    vertex(consumer, offset + size, offset);
                }
                case LOWER -> {
                    vertex(consumer, offset, offset);
                    vertex(consumer, offset, offset + size);
                    vertex(consumer, middle, middle);
                    vertex(consumer, middle, middle);
                }
            }
        }

        private void vertex(VertexConsumer consumer, float x, float y) {
            consumer.addVertexWith2DPose(pose, x, y).setColor(color);
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        private static @Nullable ScreenRectangle getBounds(
                Matrix3x2f pose,
                float size,
                float offset,
                @Nullable ScreenRectangle scissorArea
        ) {
            int min = (int) Math.floor(offset);
            int max = (int) Math.ceil(offset + size);
            ScreenRectangle bounds = new ScreenRectangle(
                    min,
                    min,
                    max - min,
                    max - min
            ).transformMaxBounds(pose);
            return scissorArea == null ? bounds : scissorArea.intersection(bounds);
        }
    }
}
