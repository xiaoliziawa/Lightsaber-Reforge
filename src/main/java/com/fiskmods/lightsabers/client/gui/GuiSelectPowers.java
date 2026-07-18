package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class GuiSelectPowers extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/container/force_power_selector.png"
    );
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/icons.png"
    );
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 166;
    private static final int SLOT_SIZE = 18;
    private static final int POWER_COLUMNS = 4;
    private static final int POWER_ROWS = 4;
    private static final int SELECTED_SLOTS = 3;

    private final List<PowerSlot> slots = new ArrayList<>();
    private final Power[] selectedPowers = new Power[SELECTED_SLOTS];

    private int leftPos;
    private int topPos;
    private Power grabbedPower;

    public GuiSelectPowers() {
        super(Component.translatable("gui.selectPowers"));
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        slots.clear();
        Arrays.fill(selectedPowers, null);
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        List<Power> selected = ALData.SELECTED_POWERS.get(minecraft.player);
        for (int index = 0; index < Math.min(selected.size(), SELECTED_SLOTS); index++) {
            selectedPowers[index] = selected.get(index);
        }

        List<PowerData> relevantPowers = new ArrayList<>();
        PowerManager manager = new PowerManager(minecraft.player);
        for (PowerData data : ALData.POWERS.get(minecraft.player)) {
            if (data.isUnlocked()
                    && data.power.isUsable()
                    && hasNoUnlockedChildren(manager, data.power)) {
                relevantPowers.add(data);
            }
        }
        Collections.sort(relevantPowers);

        for (int index = 0; index < POWER_COLUMNS * POWER_ROWS; index++) {
            Power power = index < relevantPowers.size()
                    ? relevantPowers.get(index).power
                    : null;
            int column = index % POWER_COLUMNS;
            int row = index / POWER_COLUMNS;
            slots.add(new PowerSlot(
                    power,
                    leftPos + 8 + column * SLOT_SIZE,
                    topPos + 8 + row * SLOT_SIZE
            ));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.blit(
                BACKGROUND,
                leftPos,
                topPos,
                0,
                0,
                IMAGE_WIDTH,
                IMAGE_HEIGHT
        );

        Power hoveredPower = null;
        for (PowerSlot slot : slots) {
            drawPower(guiGraphics, slot.power, slot.x, slot.y);
            if (isInside(mouseX, mouseY, slot.x, slot.y)) {
                drawHighlight(guiGraphics, slot.x, slot.y);
                hoveredPower = slot.power;
            }
        }

        for (int index = 0; index < selectedPowers.length; index++) {
            int x = selectedSlotX(index);
            int y = selectedSlotY();
            drawPower(guiGraphics, selectedPowers[index], x, y);
            if (isInside(mouseX, mouseY, x, y)) {
                drawHighlight(guiGraphics, x, y);
                hoveredPower = selectedPowers[index];
            }
        }

        if (grabbedPower != null) {
            drawPower(guiGraphics, grabbedPower, mouseX - 8, mouseY - 8);
        }
        if (hoveredPower != null) {
            renderPowerTooltip(guiGraphics, hoveredPower, mouseX, mouseY);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        PowerSlot slot = getPowerSlot(mouseX, mouseY);
        if (slot != null && slot.power != null) {
            if (hasShiftDown()) {
                assignFirstEmpty(slot.power);
            } else {
                grabbedPower = slot.power;
            }
            return true;
        }

        int selectedSlot = getSelectedSlot(mouseX, mouseY);
        if (selectedSlot >= 0 && selectedPowers[selectedSlot] != null) {
            if (hasShiftDown()) {
                selectedPowers[selectedSlot] = null;
            } else {
                grabbedPower = selectedPowers[selectedSlot];
                selectedPowers[selectedSlot] = null;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && grabbedPower != null) {
            int selectedSlot = getSelectedSlot(mouseX, mouseY);
            if (selectedSlot >= 0) {
                selectedPowers[selectedSlot] = grabbedPower;
            }
            grabbedPower = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null
                && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null) {
            ALData.SELECTED_POWERS.set(
                    minecraft.player,
                    new ArrayList<>(Arrays.asList(selectedPowers))
            );
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawPower(GuiGraphics guiGraphics, Power power, int x, int y) {
        if (power == null || !power.hasIcon()) {
            return;
        }
        guiGraphics.blit(
                ICONS,
                x,
                y,
                power.getIconX() * 16,
                power.getIconY() * 16,
                16,
                16
        );
    }

    private static void drawHighlight(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
    }

    private void renderPowerTooltip(
            GuiGraphics guiGraphics,
            Power power,
            int mouseX,
            int mouseY
    ) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(power.getLocalizedName()));
        if (power.powerStats.useCost > 0) {
            tooltip.add(Component.translatable(
                    power.powerStats.powerType == PowerType.PER_USE
                            ? "forcepower.perUse"
                            : "forcepower.perSecond",
                    power.powerStats.useCost
            ));
        }
        if (power.powerEffect != null) {
            for (String description : power.powerEffect.getDesc()) {
                tooltip.add(Component.literal(description));
            }
        }
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private static boolean hasNoUnlockedChildren(PowerManager manager, Power power) {
        for (Power child : power.children) {
            if (manager.hasPowerUnlocked(child)) {
                return false;
            }
        }
        return true;
    }

    private void assignFirstEmpty(Power power) {
        for (int index = 0; index < selectedPowers.length; index++) {
            if (selectedPowers[index] == null) {
                selectedPowers[index] = power;
                return;
            }
        }
    }

    private PowerSlot getPowerSlot(double mouseX, double mouseY) {
        for (PowerSlot slot : slots) {
            if (isInside(mouseX, mouseY, slot.x, slot.y)) {
                return slot;
            }
        }
        return null;
    }

    private int getSelectedSlot(double mouseX, double mouseY) {
        for (int index = 0; index < selectedPowers.length; index++) {
            if (isInside(mouseX, mouseY, selectedSlotX(index), selectedSlotY())) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x - 1
                && mouseX < x + 17
                && mouseY >= y - 1
                && mouseY < y + 17;
    }

    private int selectedSlotX(int index) {
        return leftPos + 62 + index * SLOT_SIZE;
    }

    private int selectedSlotY() {
        return topPos + 142;
    }

    private record PowerSlot(Power power, int x, int y) {
    }
}
