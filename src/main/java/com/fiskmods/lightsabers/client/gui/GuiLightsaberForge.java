package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.render.item.LightsaberItemRenderer;
import com.fiskmods.lightsabers.common.container.ContainerLightsaberForge;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemFocusingCrystal;
import com.fiskmods.lightsabers.common.item.ItemLightsaberPart;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class GuiLightsaberForge extends AbstractContainerScreen<ContainerLightsaberForge> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/lightsaber_forge.png"
    );
    private static final int INVALID_LENGTH_COLOR = 0xFFD74848;
    private static final int VALID_LENGTH_COLOR = 0xFFFFFFFF;

    public GuiLightsaberForge(
            ContainerLightsaberForge menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title, 176, 196);
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256
        );

        LightsaberData resultData = menu.craftMatrix.getResult();
        if (resultData != null) {
            renderResultPreview(guiGraphics);
        } else {
            renderGhostComponents(guiGraphics);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);
        LightsaberData resultData = menu.craftMatrix.getResult();
        if (resultData == null) {
            return;
        }

        Component status = resultData.isTooShort()
                ? Component.translatable("gui.lightsaber_forge.too_short")
                : Component.translatable(
                        "gui.lightsaber_forge.height",
                        String.format(Locale.ROOT, "%.1f", resultData.getHeightCm())
                );
        guiGraphics.text(
                font,
                status,
                45,
                64 - font.lineHeight,
                resultData.isTooShort() ? INVALID_LENGTH_COLOR : VALID_LENGTH_COLOR,
                false
        );
    }

    private static final float PREVIEW_SCALE = 1.5F;
    private static final float PREVIEW_ROTATION = 45.0F;
    private static final int PREVIEW_X = 75;
    private static final int PREVIEW_Y = 40;

    private void renderResultPreview(GuiGraphicsExtractor guiGraphics) {
        ItemStack resultStack = menu.craftResult.getItem(0);
        if (resultStack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos + PREVIEW_X, topPos + PREVIEW_Y);
        guiGraphics.pose().scale(PREVIEW_SCALE, PREVIEW_SCALE);
        guiGraphics.pose().rotate((float) Math.toRadians(PREVIEW_ROTATION));
        LightsaberItemRenderer.guiBladePreview = true;
        try {
            guiGraphics.item(resultStack, -8, -8);
        } finally {
            LightsaberItemRenderer.guiBladePreview = false;
        }
        guiGraphics.pose().popMatrix();
    }

    private void renderGhostComponents(GuiGraphicsExtractor guiGraphics) {
        Hilt hilt = getPreviewHilt();
        if (hilt == null) {
            return;
        }

        for (int slot = 0; slot < PartType.values().length; slot++) {
            if (menu.craftMatrix.getItem(slot).isEmpty()) {
                int[] pos = ContainerLightsaberForge.SLOTS[slot];
                guiGraphics.item(
                        ItemLightsaberPart.create(PartType.values()[slot], hilt),
                        leftPos + pos[0],
                        topPos + pos[1]
                );
            }
        }
        if (menu.craftMatrix.getItem(5).isEmpty()) {
            int[] pos = ContainerLightsaberForge.SLOTS[5];
            guiGraphics.item(
                    ItemCrystal.create(hilt.getColor()),
                    leftPos + pos[0],
                    topPos + pos[1]
            );
        }
        for (int slot = 6; slot <= 7; slot++) {
            if (menu.craftMatrix.getItem(slot).isEmpty()) {
                int[] pos = ContainerLightsaberForge.SLOTS[slot];
                guiGraphics.item(
                        ItemFocusingCrystal.create(FocusingCrystal.values()[0]),
                        leftPos + pos[0],
                        topPos + pos[1]
                );
            }
        }
    }

    private Hilt getPreviewHilt() {
        Hilt hilt = null;
        for (int slot = 0; slot < PartType.values().length; slot++) {
            ItemStack stack = menu.craftMatrix.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            Hilt stackHilt = ItemLightsaberPart.get(stack);
            if (hilt != null && hilt != stackHilt) {
                hilt = null;
                break;
            }
            hilt = stackHilt;
        }
        if (hilt != null || minecraft == null || minecraft.player == null) {
            return hilt;
        }

        int hiltCount = Hilt.REGISTRY.getKeys().size();
        return hiltCount == 0
                ? null
                : Hilt.REGISTRY.getObjectById((minecraft.player.tickCount / 20) % hiltCount);
    }
}
