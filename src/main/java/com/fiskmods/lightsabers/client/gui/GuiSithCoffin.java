package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.common.container.ContainerSithCoffin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiSithCoffin extends AbstractContainerScreen<ContainerSithCoffin> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace(
            "textures/gui/container/generic_54.png"
    );

    public GuiSithCoffin(
            ContainerSithCoffin menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title, 176, 114 + ContainerSithCoffin.VISIBLE_ROWS * 18);
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
        int containerHeight = ContainerSithCoffin.VISIBLE_ROWS * 18 + 17;
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                containerHeight,
                256,
                256
        );
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos,
                topPos + containerHeight,
                0,
                126,
                imageWidth,
                96,
                256,
                256
        );
    }
}
