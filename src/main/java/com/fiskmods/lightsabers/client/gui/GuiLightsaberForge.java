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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class GuiLightsaberForge extends AbstractContainerScreen<ContainerLightsaberForge> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/lightsaber_forge.png"
    );

    public GuiLightsaberForge(
            ContainerLightsaberForge menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        imageHeight = 196;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        LightsaberData resultData = menu.craftMatrix.getResult();
        if (resultData != null) {
            renderResultPreview(guiGraphics);
        } else {
            renderGhostComponents(guiGraphics);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
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
        guiGraphics.drawString(
                font,
                status,
                45,
                64 - font.lineHeight,
                resultData.isTooShort() ? 0xD74848 : 0xFFFFFF,
                false
        );
    }

    private static final float PREVIEW_SCALE = 1.5F;
    private static final float PREVIEW_ROTATION = 45.0F;
    private static final int PREVIEW_X = 75;
    private static final int PREVIEW_Y = 40;

    private void renderResultPreview(GuiGraphics guiGraphics) {
        ItemStack resultStack = menu.craftResult.getItem(0);
        if (resultStack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos + PREVIEW_X, topPos + PREVIEW_Y, 100);
        guiGraphics.pose().scale(PREVIEW_SCALE, PREVIEW_SCALE, PREVIEW_SCALE);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(PREVIEW_ROTATION));
        LightsaberItemRenderer.guiBladePreview = true;
        guiGraphics.renderFakeItem(resultStack, -8, -8);
        LightsaberItemRenderer.guiBladePreview = false;
        guiGraphics.pose().popPose();
    }

    private void renderGhostComponents(GuiGraphics guiGraphics) {
        Hilt hilt = getPreviewHilt();
        if (hilt == null) {
            return;
        }

        for (int slot = 0; slot < PartType.values().length; slot++) {
            if (menu.craftMatrix.getItem(slot).isEmpty()) {
                int[] pos = ContainerLightsaberForge.SLOTS[slot];
                guiGraphics.renderFakeItem(
                        ItemLightsaberPart.create(PartType.values()[slot], hilt),
                        leftPos + pos[0],
                        topPos + pos[1]
                );
            }
        }
        if (menu.craftMatrix.getItem(5).isEmpty()) {
            int[] pos = ContainerLightsaberForge.SLOTS[5];
            guiGraphics.renderFakeItem(
                    ItemCrystal.create(hilt.getColor()),
                    leftPos + pos[0],
                    topPos + pos[1]
            );
        }
        for (int slot = 6; slot <= 7; slot++) {
            if (menu.craftMatrix.getItem(slot).isEmpty()) {
                int[] pos = ContainerLightsaberForge.SLOTS[slot];
                guiGraphics.renderFakeItem(
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
