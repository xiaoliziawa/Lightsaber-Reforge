package com.fiskmods.lightsabers.helper;

import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import com.fiskmods.lightsabers.common.force.PowerManager;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public final class ALPredicates {
    private ALPredicates() {
    }

    public static Predicate<Power> isUnlocked(Player player) {
        return power -> PowerManager.hasPowerUnlocked(player, power);
    }

    public static Predicate<PowerData> isRelevant(Player player) {
        return data -> data.isUnlocked()
                && data.power.isUsable()
                && ALHelper.getUnlockedChildren(player, data.power).isEmpty();
    }
}
