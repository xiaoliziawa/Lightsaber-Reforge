package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.container.ContainerDisassemblyStation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiDisassemblyStation extends AbstractContainerScreen<ContainerDisassemblyStation> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/disassembly_station.png"
    );

    public GuiDisassemblyStation(
            ContainerDisassemblyStation menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        imageHeight = 168;
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
        RenderSystem.setShaderColor(1, 1, 1, 1);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.isBurning()) {
            int burnHeight = menu.getBurnProgress(13);
            guiGraphics.blit(
                    TEXTURE,
                    leftPos + 17,
                    topPos + 49 - burnHeight,
                    176,
                    12 - burnHeight,
                    14,
                    burnHeight + 2
            );

            int progressWidth = menu.getDisassemblyProgress(24);
            guiGraphics.blit(
                    TEXTURE,
                    leftPos + 39,
                    topPos + 36,
                    176,
                    14,
                    progressWidth + 1,
                    16
            );
        }
    }
}
