package com.fiskmods.lightsabers.common.force;

import java.util.List;

import com.fiskmods.lightsabers.common.data.ALData;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class PowerManager
{
    public enum InvestResult
    {
        NONE,
        INVESTED,
        UNLOCKED
    }

    private final Player thePlayer;

    public PowerManager(Player player)
    {
        thePlayer = player;
    }

    public int getHierarchy(Power power)
    {
        if (hasPowerUnlocked(power))
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (Power parent = power.parent; parent != null && !hasPowerUnlocked(parent); ++i)
            {
                parent = parent.parent;
            }

            return i;
        }
    }

    public boolean hasPowerUnlocked(Power power)
    {
        return hasPowerUnlocked(thePlayer, power);
    }

    public boolean canUnlockPower(Power power)
    {
        return canUnlockPower(thePlayer, power);
    }

    public PowerData getPowerData(Power power)
    {
        return getPowerData(thePlayer, power);
    }

    public static boolean unlockPower(Player player, Power power)
    {
        if (!hasPowerUnlocked(player, power))
        {
            PowerData data = getPowerData(player, power);

            if (data != null)
            {
                data.setUnlocked(player, true);
                data.xpInvested = power.getActualXpCost(player);

                ALData.BASE_POWER.incrWithoutNotify(player, (byte) (power.powerStats.baseBonus - power.powerStats.baseRequirement));

                if (power == Power.FORCE_SENSITIVITY)
                {
                    for (ForceSide side : ForceSide.values())
                    {
                        unlockPower(player, side.getPower());
                    }
                }

                unlockPower(player, power.parent);

                return true;
            }
        }

        return false;
    }

    public static InvestResult investXp(Player player, Power power)
    {
        PowerData data = getPowerData(player, power);

        if (data == null || data.isUnlocked() || !canUnlockPower(player, power))
        {
            return InvestResult.NONE;
        }

        PowerStats stats = power.powerStats;

        if (stats.baseRequirement > 0 && ALData.BASE_POWER.get(player) < stats.baseRequirement)
        {
            return InvestResult.NONE;
        }

        int cost = power.getActualXpCost(player);
        int amount = Math.min(cost - data.xpInvested, Mth.floor(ALData.FORCE_XP.get(player)));

        if (amount > 0)
        {
            data.xpInvested += amount;
            ALData.FORCE_XP.incrWithoutNotify(player, -(float) amount);
        }

        if (data.xpInvested >= cost)
        {
            data.setUnlocked(player, true);
            ALData.BASE_POWER.incrWithoutNotify(player, (byte) (stats.baseBonus - stats.baseRequirement));

            return InvestResult.UNLOCKED;
        }

        return amount > 0 ? InvestResult.INVESTED : InvestResult.NONE;
    }

    public static boolean removePower(Player player, Power power)
    {
        if (hasPowerUnlocked(player, power))
        {
            PowerData data = getPowerData(player, power);

            if (data != null)
            {
                data.setUnlocked(player, false);
                data.xpInvested = 0;
                
                ALData.BASE_POWER.incrWithoutNotify(player, (byte) (power.powerStats.baseRequirement - power.powerStats.baseBonus));

                for (Power child : power.children)
                {
                    removePower(player, child);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean hasPowerUnlocked(Player player, Power power)
    {
        PowerData data = getPowerData(player, power);
        return data != null && data.isUnlocked();
    }

    public static boolean canUnlockPower(Player player, Power power)
    {
        return power.parent == null || hasPowerUnlocked(player, power.parent);
    }

    public static PowerData getPowerData(Player player, Power power)
    {
        return ALData.POWERS.get(player).get(power);
    }

    public static Power getSelectedPower(Player player)
    {
        List<Power> selectedPowers = ALData.SELECTED_POWERS.get(player);

        if (!selectedPowers.isEmpty())
        {
            int index = ALData.SELECTED_POWER.get(player);

            if (index >= 0 && index < selectedPowers.size())
            {
                return selectedPowers.get(index);
            }
        }

        return null;
    }
}
