package com.fiskmods.lightsabers.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class BlockStateItemHelper {
    private BlockStateItemHelper() {
    }

    public static void setProperty(ItemStack stack, String property, String value) {
        stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(property, value);
    }

    public static String getProperty(ItemStack stack, String property, String fallback) {
        CompoundTag tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return tag != null && tag.contains(property) ? tag.getString(property) : fallback;
    }
}
