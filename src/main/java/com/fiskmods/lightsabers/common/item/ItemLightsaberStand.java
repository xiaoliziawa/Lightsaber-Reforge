package com.fiskmods.lightsabers.common.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ItemLightsaberStand extends BlockItem {
    public ItemLightsaberStand(Block block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }
}
