package com.fiskmods.lightsabers.common.integration.epicfight;

import net.epicfight_dd.world.capabilities.item.DawnDayWeaponCapabilityPreset;
import net.minecraft.world.item.Item;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.function.Function;

final class EpicFightDawnDayIntegration {
    private EpicFightDawnDayIntegration() {
    }

    static Function<Item, CapabilityItem.Builder> polebladePreset() {
        return DawnDayWeaponCapabilityPreset.POLEBLADE;
    }
}
