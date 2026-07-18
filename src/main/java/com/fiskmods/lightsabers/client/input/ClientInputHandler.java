package com.fiskmods.lightsabers.client.input;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.gui.GuiSelectPowers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerType;
import com.fiskmods.lightsabers.common.input.ForcePowerInput;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.network.ALNetworkManager;
import com.fiskmods.lightsabers.common.network.MessageToggleLightsaber;
import com.fiskmods.lightsabers.common.network.MessageUsePower;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.List;

public enum ClientInputHandler {
    INSTANCE;

    private static final int SELECT_HOLD_TICKS = 5;

    private int selectHeldTicks;
    private boolean selectorOpened;
    private boolean powerFailSoundPlayed;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            resetSelectorState();
            powerFailSoundPlayed = false;
            return;
        }

        if (minecraft.screen == null) {
            handleLightsaberToggle(player);
            handlePowerUse(player);
            handlePowerSelection(minecraft, player);
        } else if (!(minecraft.screen instanceof GuiSelectPowers)) {
            resetSelectorState();
        }

        if (!ALKeyMappings.SELECT_POWER.isDown()) {
            resetSelectorState();
        }
        if (!ALKeyMappings.ACTIVATE_POWER.isDown()) {
            powerFailSoundPlayed = false;
        }
    }

    private static void handleLightsaberToggle(Player player) {
        while (ALKeyMappings.ACTIVATE_LIGHTSABER.consumeClick()) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof ItemLightsaberBase) {
                boolean active = !ItemLightsaberBase.isActive(stack);
                ItemLightsaberBase.ignite(player, active);
                ALNetworkManager.sendToServer(new MessageToggleLightsaber(active));
            }
        }
    }

    private void handlePowerUse(Player player) {
        Power selected = PowerManager.getSelectedPower(player);
        boolean continuous = selected != null
                && selected.powerStats.powerType == PowerType.PER_SECOND;
        while (ALKeyMappings.ACTIVATE_POWER.consumeClick()) {
            if (continuous) {
                continue;
            }
            if (ForcePowerInput.tryUseSelectedPower(player, LogicalSide.CLIENT)) {
                ALNetworkManager.sendToServer(new MessageUsePower());
            } else if (!powerFailSoundPlayed) {
                Lightsabers.proxy.playLocalSound(
                        player,
                        ALSounds.player_force_fail,
                        1.0F,
                        1.0F
                );
                powerFailSoundPlayed = true;
            }
        }
    }

    private void handlePowerSelection(Minecraft minecraft, Player player) {
        while (ALKeyMappings.SELECT_POWER.consumeClick()) {
            cycleSelectedPower(player, 1);
        }

        if (!ALKeyMappings.SELECT_POWER.isDown() || selectorOpened) {
            return;
        }
        selectHeldTicks++;
        if (selectHeldTicks >= SELECT_HOLD_TICKS
                && PowerManager.hasPowerUnlocked(player, Power.FORCE_SENSITIVITY)) {
            cycleSelectedPower(player, -1);
            selectorOpened = true;
            minecraft.setScreen(new GuiSelectPowers());
        }
    }

    private static void cycleSelectedPower(Player player, int direction) {
        List<Power> selectedPowers = ALData.SELECTED_POWERS.get(player);
        if (selectedPowers.isEmpty()) {
            return;
        }
        int selected = Math.floorMod(
                ALData.SELECTED_POWER.get(player) + direction,
                selectedPowers.size()
        );
        ALData.SELECTED_POWER.set(player, (byte) selected);
    }

    private void resetSelectorState() {
        selectHeldTicks = 0;
        selectorOpened = false;
    }
}
