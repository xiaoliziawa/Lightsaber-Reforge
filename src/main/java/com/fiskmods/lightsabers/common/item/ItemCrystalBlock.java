package com.fiskmods.lightsabers.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class ItemCrystalBlock extends BlockItem {
    public ItemCrystalBlock(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(ItemCrystal.get(stack).getUnlocalizedName())
                .withStyle(ChatFormatting.GRAY));
    }
}
