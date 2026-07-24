package com.fiskmods.lightsabers.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;

import java.util.HashMap;
import java.util.Map;

public final class BlockStateItemHelper {
    private BlockStateItemHelper() {
    }

    public static void setProperty(ItemStack stack, String property, String value) {
        BlockItemStateProperties current = stack.getOrDefault(
                DataComponents.BLOCK_STATE,
                BlockItemStateProperties.EMPTY
        );
        Map<String, String> properties = new HashMap<>(current.properties());
        properties.put(property, value);
        stack.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(Map.copyOf(properties)));
    }

    public static String getProperty(ItemStack stack, String property, String fallback) {
        return stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
                .properties()
                .getOrDefault(property, fallback);
    }
}
