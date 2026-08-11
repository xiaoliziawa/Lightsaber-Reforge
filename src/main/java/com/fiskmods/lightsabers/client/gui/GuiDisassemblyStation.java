package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.container.ContainerDisassemblyStation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiDisassemblyStation extends AbstractContainerScreen<ContainerDisassemblyStation> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/disassembly_station.png"
    );

    public GuiDisassemblyStation(
            ContainerDisassemblyStation menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title, 176, 168);
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

        if (menu.isBurning()) {
            int burnHeight = menu.getBurnProgress(13);
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + 17,
                    topPos + 49 - burnHeight,
                    176,
                    12 - burnHeight,
                    14,
                    burnHeight + 2,
                    256,
                    256
            );

            int progressWidth = menu.getDisassemblyProgress(24);
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + 39,
                    topPos + 36,
                    176,
                    14,
                    progressWidth + 1,
                    16,
                    256,
                    256
            );
        }
    }
}
