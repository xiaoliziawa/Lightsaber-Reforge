package com.fiskmods.lightsabers.common.input;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

public final class ForcePowerInput {
    private ForcePowerInput() {
    }

    public static boolean tryUseSelectedPower(Player player, LogicalSide side) {
        Power power = PowerManager.getSelectedPower(player);
        boolean valid = power != null
                && PowerManager.hasPowerUnlocked(player, power)
                && power.powerStats.powerType == PowerType.PER_USE
                && ALData.USE_POWER_COOLDOWN.get(player) == 0
                && ALData.POWERS.get(player).getForceMax() > 0
                && ALDataInterp.FORCE_POWER.get(player) >= power.getUseCost(player);

        if (!valid || !power.powerEffect.execute(player, side)) {
            return false;
        }

        if (side == LogicalSide.CLIENT) {
            String sound = power.powerEffect.getCastSound(power.getSide());
            if (sound != null && !sound.isEmpty()) {
                Lightsabers.proxy.playLocalSound(
                        player,
                        sound,
                        power.powerEffect.getCastSoundVolume(power.getSide()),
                        power.powerEffect.getCastSoundPitch(power.getSide())
                );
            }
        } else {
            ALDataInterp.FORCE_POWER.incr(player, -power.getUseCost(player));
            ALData.USE_POWER_COOLDOWN.set(player, ALConstants.FORCE_POWER_COOLDOWN);
        }
        return true;
    }
}
