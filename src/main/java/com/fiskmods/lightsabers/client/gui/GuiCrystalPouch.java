package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.container.ContainerCrystalPouch;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiCrystalPouch extends AbstractContainerScreen<ContainerCrystalPouch> {
    private static final int EXTRA_CRYSTAL_ROW_HEIGHT = 18;
    private static final int CRYSTAL_SECTION_HEIGHT = 54;
    private static final int ORIGINAL_IMAGE_HEIGHT = 150;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/crystal_pouch.png"
    );

    public GuiCrystalPouch(
            ContainerCrystalPouch menu,
            Inventory playerInventory,
            Component title
    ) {
        super(
                menu,
                playerInventory,
                title,
                176,
                ORIGINAL_IMAGE_HEIGHT + EXTRA_CRYSTAL_ROW_HEIGHT
        );
        inventoryLabelY = imageHeight - 93;
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
                CRYSTAL_SECTION_HEIGHT,
                256,
                256
        );
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos + CRYSTAL_SECTION_HEIGHT,
                0,
                CRYSTAL_SECTION_HEIGHT - EXTRA_CRYSTAL_ROW_HEIGHT,
                imageWidth,
                EXTRA_CRYSTAL_ROW_HEIGHT,
                256,
                256
        );
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos + CRYSTAL_SECTION_HEIGHT + EXTRA_CRYSTAL_ROW_HEIGHT,
                0,
                CRYSTAL_SECTION_HEIGHT,
                imageWidth,
                ORIGINAL_IMAGE_HEIGHT - CRYSTAL_SECTION_HEIGHT,
                256,
                256
        );

        for (int slot = 0; slot < CrystalColor.values().length; slot++) {
            if (menu.inventory.getItem(slot).isEmpty()) {
                guiGraphics.item(
                        ItemCrystal.create(CrystalColor.values()[slot]),
                        leftPos + 8 + slot % 9 * 18,
                        topPos + 18 + slot / 9 * 18
                );
            }
        }
    }
}
