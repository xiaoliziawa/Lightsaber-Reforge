package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.BlockForcestone;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemForcestone extends BlockItem {
    private static final String VARIANT_PROPERTY = BlockForcestone.VARIANT.getName();
    private static final float MODEL_VARIANT_DIVISOR =
            BlockForcestone.Variant.values().length - 1;

    public ItemForcestone(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId() + "." + getVariant(stack).getSerializedName();
    }

    public static ItemStack create(Block block, BlockForcestone.Variant variant) {
        ItemStack stack = new ItemStack(block);
        BlockStateItemHelper.setProperty(
                stack,
                VARIANT_PROPERTY,
                variant.getSerializedName()
        );
        return stack;
    }

    public static BlockForcestone.Variant getVariant(ItemStack stack) {
        return BlockForcestone.Variant.byName(BlockStateItemHelper.getProperty(
                stack,
                VARIANT_PROPERTY,
                BlockForcestone.Variant.DEFAULT.getSerializedName()
        ));
    }

    public static float getModelPropertyValue(ItemStack stack) {
        return getVariant(stack).ordinal() / MODEL_VARIANT_DIVISOR;
    }
}
