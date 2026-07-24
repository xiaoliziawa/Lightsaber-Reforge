package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

import java.util.Locale;
import java.util.Random;

public class ItemFocusingCrystal extends Item implements ILightsaberComponent {
    private static final String CRYSTAL_ID_TAG = "FocusingCrystalId";

    public ItemFocusingCrystal() {
        super(new Item.Properties());
    }

    @Override
    public long getFingerprint(ItemStack stack, int slot) {
        return get(stack).getCode() << 32;
    }

    @Override
    public boolean isCompatibleSlot(ItemStack stack, int slot) {
        return slot == 6 || slot == 7;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack)
                + "."
                + get(stack).name().toLowerCase(Locale.ROOT);
    }

    public static FocusingCrystal get(ItemStack stack) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        int id = tag != null && tag.contains(CRYSTAL_ID_TAG)
                ? tag.getInt(CRYSTAL_ID_TAG)
                : stack.getDamageValue();
        return get(id);
    }

    public static FocusingCrystal get(int id) {
        return FocusingCrystal.values()[Math.abs(id) % FocusingCrystal.values().length];
    }

    public static FocusingCrystal getRandom(Random random) {
        return FocusingCrystal.values()[random.nextInt(FocusingCrystal.values().length)];
    }

    public static FocusingCrystal getRandom() {
        return getRandom(new Random());
    }

    public static ItemStack create(FocusingCrystal crystal) {
        ItemStack stack = new ItemStack(ModItems.FOCUSING_CRYSTAL.get());
        ItemDataHelper.updateCustomData(
                stack,
                tag -> tag.putInt(CRYSTAL_ID_TAG, crystal.ordinal())
        );
        return stack;
    }
}
