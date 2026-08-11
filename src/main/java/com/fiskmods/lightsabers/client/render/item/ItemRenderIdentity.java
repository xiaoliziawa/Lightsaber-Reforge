package com.fiskmods.lightsabers.client.render.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record ItemRenderIdentity(
        Item item,
        DataComponentMap components,
        ItemDisplayContext displayContext
) {
    public static ItemRenderIdentity of(
            ItemStack stack,
            ItemDisplayContext displayContext
    ) {
        return new ItemRenderIdentity(
                stack.getItem(),
                stack.immutableComponents(),
                displayContext
        );
    }
}
