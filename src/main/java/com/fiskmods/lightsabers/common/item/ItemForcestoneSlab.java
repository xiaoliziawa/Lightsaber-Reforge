package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.BlockModSlab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemForcestoneSlab extends BlockItem {
    private static final String VARIANT_PROPERTY = BlockModSlab.VARIANT.getName();

    public ItemForcestoneSlab(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId() + "." + getVariant(stack).getSerializedName();
    }

    public static ItemStack create(Block block, BlockModSlab.Variant variant) {
        ItemStack stack = new ItemStack(block);
        BlockStateItemHelper.setProperty(
                stack,
                VARIANT_PROPERTY,
                variant.getSerializedName()
        );
        return stack;
    }

    public static BlockModSlab.Variant getVariant(ItemStack stack) {
        return BlockModSlab.Variant.byName(BlockStateItemHelper.getProperty(
                stack,
                VARIANT_PROPERTY,
                BlockModSlab.Variant.LIGHT.getSerializedName()
        ));
    }

    public static float getModelPropertyValue(ItemStack stack) {
        return getVariant(stack).ordinal();
    }
}
