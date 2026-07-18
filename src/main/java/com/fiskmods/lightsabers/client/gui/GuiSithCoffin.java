package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.common.container.ContainerSithCoffin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiSithCoffin extends AbstractContainerScreen<ContainerSithCoffin> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/gui/container/generic_54.png"
    );

    public GuiSithCoffin(
            ContainerSithCoffin menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        imageHeight = 114 + ContainerSithCoffin.VISIBLE_ROWS * 18;
        inventoryLabelY = imageHeight - 94;
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
        int containerHeight = ContainerSithCoffin.VISIBLE_ROWS * 18 + 17;
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                containerHeight
        );
        guiGraphics.blit(
                TEXTURE,
                leftPos,
                topPos + containerHeight,
                0,
                126,
                imageWidth,
                96
        );
    }
}
