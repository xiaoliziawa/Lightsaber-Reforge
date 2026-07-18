package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.container.ContainerCrystalPouch;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiCrystalPouch extends AbstractContainerScreen<ContainerCrystalPouch> {
    private static final int EXTRA_CRYSTAL_ROW_HEIGHT = 18;
    private static final int CRYSTAL_SECTION_HEIGHT = 54;
    private static final int ORIGINAL_IMAGE_HEIGHT = 150;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/crystal_pouch.png"
    );

    public GuiCrystalPouch(
            ContainerCrystalPouch menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        imageHeight = ORIGINAL_IMAGE_HEIGHT + EXTRA_CRYSTAL_ROW_HEIGHT;
        inventoryLabelY = imageHeight - 93;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
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
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                CRYSTAL_SECTION_HEIGHT
        );
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos + CRYSTAL_SECTION_HEIGHT,
                0,
                CRYSTAL_SECTION_HEIGHT - EXTRA_CRYSTAL_ROW_HEIGHT,
                imageWidth,
                EXTRA_CRYSTAL_ROW_HEIGHT
        );
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos + CRYSTAL_SECTION_HEIGHT + EXTRA_CRYSTAL_ROW_HEIGHT,
                0,
                CRYSTAL_SECTION_HEIGHT,
                imageWidth,
                ORIGINAL_IMAGE_HEIGHT - CRYSTAL_SECTION_HEIGHT
        );

        for (int slot = 0; slot < CrystalColor.values().length; slot++) {
            if (menu.inventory.getItem(slot).isEmpty()) {
                guiGraphics.renderFakeItem(
                        ItemCrystal.create(CrystalColor.values()[slot]),
                        leftPos + 8 + slot % 9 * 18,
                        topPos + 18 + slot / 9 * 18
                );
            }
        }
    }
}
