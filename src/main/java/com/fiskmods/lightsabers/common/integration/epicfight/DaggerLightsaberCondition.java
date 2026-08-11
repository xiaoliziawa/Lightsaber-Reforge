package com.fiskmods.lightsabers.common.integration.epicfight;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.data.conditions.Condition.ItemStackCondition;

import java.util.List;

public final class DaggerLightsaberCondition extends ItemStackCondition {
    @Override
    public DaggerLightsaberCondition read(CompoundTag tag) {
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        return new CompoundTag();
    }

    @Override
    public boolean predicate(ItemStack stack) {
        return ItemLightsaberBase.isDaggerLightsaber(stack);
    }

    @Override
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return List.of();
    }
}
