package com.fiskmods.lightsabers.common.integration.epicfight;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition.ItemStackCondition;

import java.util.List;

public final class SpinningLightsaberCondition extends ItemStackCondition {
    @Override
    public SpinningLightsaberCondition read(CompoundTag tag) {
        return this;
    }

    @Override
    public CompoundTag serializePredicate() {
        return new CompoundTag();
    }

    @Override
    public boolean predicate(ItemStack stack) {
        return ItemLightsaberBase.isSpinningLightsaber(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<ParameterEditor> getAcceptingParameters(Screen screen) {
        return List.of();
    }
}
