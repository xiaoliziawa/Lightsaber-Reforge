package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.container.ContainerHolocron;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.network.ALNetworkManager;
import com.fiskmods.lightsabers.common.network.MessageUnlockPower;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class GuiForcePowers extends AbstractContainerScreen<ContainerHolocron> {
    private static final int PANEL_WIDTH = 256;
    private static final int PANEL_HEIGHT = 202;
    private static final int LIST_X = 15;
    private static final int LIST_Y = 25;
    private static final int LIST_WIDTH = 226;
    private static final int ROW_HEIGHT = 18;
    private static final int VISIBLE_ROWS = 7;

    private final PowerManager powerManager;

    private int scrollOffset;

    public GuiForcePowers(
            ContainerHolocron menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
        powerManager = new PowerManager(playerInventory.player);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(leftPos + 88, topPos + 174, 80, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderPowerTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0101018);
        guiGraphics.fill(
                leftPos + LIST_X - 2,
                topPos + LIST_Y - 2,
                leftPos + LIST_X + LIST_WIDTH + 2,
                topPos + LIST_Y + VISIBLE_ROWS * ROW_HEIGHT + 2,
                0xC0202028
        );

        List<Power> powers = Power.POWERS;
        int end = Math.min(scrollOffset + VISIBLE_ROWS, powers.size());
        for (int index = scrollOffset; index < end; index++) {
            Power power = powers.get(index);
            int row = index - scrollOffset;
            int x = leftPos + LIST_X;
            int y = topPos + LIST_Y + row * ROW_HEIGHT;
            boolean hovered = mouseX >= x
                    && mouseX < x + LIST_WIDTH
                    && mouseY >= y
                    && mouseY < y + ROW_HEIGHT - 1;
            guiGraphics.fill(
                    x,
                    y,
                    x + LIST_WIDTH,
                    y + ROW_HEIGHT - 1,
                    hovered ? 0xA0505060 : 0x70404048
            );
            guiGraphics.drawString(
                    font,
                    Component.literal(power.getLocalizedName()),
                    x + 5,
                    y + 5,
                    getPowerColor(power),
                    false
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 7, 0xFFFFFF);
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;
        guiGraphics.drawString(
                font,
                Component.translatable(
                        "gui.forcePowers.xp",
                        Mth.floor(ALData.FORCE_XP.get(player))
                ),
                15,
                154,
                ALData.FORCE_XP.get(player) > 0 ? 0x80FF80 : 0xD74848,
                false
        );
        guiGraphics.drawString(
                font,
                Component.translatable(
                        "gui.forcePowers.basePower",
                        ALData.BASE_POWER.get(player)
                ),
                15,
                164,
                ALData.BASE_POWER.get(player) > 0 ? 0x80FF80 : 0xD74848,
                false
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Power power = getPowerAt(mouseX, mouseY);
            if (power != null && canInvest(power)) {
                investXp(power);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxOffset = Math.max(Power.POWERS.size() - VISIBLE_ROWS, 0);
        scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxOffset);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getPowerColor(Power power) {
        if (powerManager.hasPowerUnlocked(power)) {
            return 0x90FF90;
        }
        return powerManager.canUnlockPower(power) ? 0xFFFFFF : 0x808080;
    }

    private boolean canInvest(Power power) {
        if (minecraft == null || minecraft.player == null
                || powerManager.hasPowerUnlocked(power)
                || !powerManager.canUnlockPower(power)) {
            return false;
        }

        Player player = minecraft.player;
        return (Mth.floor(ALData.FORCE_XP.get(player)) > 0
                || power.getActualXpCost(player) == 0)
                && (ALData.BASE_POWER.get(player) >= power.powerStats.baseRequirement
                || power.powerStats.baseRequirement == 0);
    }

    private void investXp(Power power) {
        Player player = minecraft.player;
        PowerManager.InvestResult result = PowerManager.investXp(player, power);
        if (result == PowerManager.InvestResult.NONE) {
            return;
        }

        ALNetworkManager.sendToServer(new MessageUnlockPower(power));
        if (result == PowerManager.InvestResult.UNLOCKED) {
            Lightsabers.proxy.playLocalSound(player, ALSounds.block_holocron_unlock, 1.0F, 1.0F);
        } else {
            RandomSource random = player.getRandom();
            Lightsabers.proxy.playLocalSound(
                    player,
                    ALSounds.block_holocron_invest,
                    1.0F,
                    1.1F + (random.nextFloat() - random.nextFloat()) * 0.2F
            );
        }
    }

    private Power getPowerAt(double mouseX, double mouseY) {
        int x = leftPos + LIST_X;
        int y = topPos + LIST_Y;
        if (mouseX < x || mouseX >= x + LIST_WIDTH
                || mouseY < y || mouseY >= y + VISIBLE_ROWS * ROW_HEIGHT) {
            return null;
        }

        int index = scrollOffset + (int) (mouseY - y) / ROW_HEIGHT;
        return index >= 0 && index < Power.POWERS.size() ? Power.POWERS.get(index) : null;
    }

    private void renderPowerTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Power power = getPowerAt(mouseX, mouseY);
        if (power == null || minecraft == null || minecraft.player == null) {
            return;
        }

        PowerData data = powerManager.getPowerData(power);
        Component status;
        if (powerManager.hasPowerUnlocked(power)) {
            status = Component.translatable("forcepower.unlocked");
        } else if (!powerManager.canUnlockPower(power) && power.parent != null) {
            status = Component.translatable(
                    "achievement.requires",
                    power.parent.getLocalizedName()
            );
        } else {
            int invested = data == null ? 0 : data.xpInvested;
            status = Component.translatable(
                    "forcepower.xpLeft",
                    Math.max(power.getActualXpCost(minecraft.player) - invested, 0)
            );
        }
        guiGraphics.renderTooltip(font, status, mouseX, mouseY);
    }
}
