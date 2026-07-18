package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.client.render.item.CrystalClientItemExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public final class ItemCrystalBlock extends BlockItem {
    public ItemCrystalBlock(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(CrystalClientItemExtensions.INSTANCE);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return ItemCrystal.rarityMap.get(ItemCrystal.get(stack));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(ItemCrystal.get(stack).getUnlocalizedName())
                .withStyle(ChatFormatting.GRAY));
    }
}
