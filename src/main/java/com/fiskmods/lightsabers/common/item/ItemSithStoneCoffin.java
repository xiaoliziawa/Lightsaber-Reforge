package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.BlockSithStoneCoffin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSithStoneCoffin extends BlockItem {
    public ItemSithStoneCoffin(BlockSithStoneCoffin block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos basePos = context.getClickedPos();
        BlockPos upperPos = basePos.above();

        if (!level.setBlock(basePos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return false;
        }
        if (!level.setBlock(
                upperPos,
                state.setValue(BlockSithStoneCoffin.PART, BlockSithStoneCoffin.Part.UPPER),
                Block.UPDATE_ALL
        )) {
            if (level.getBlockState(basePos).is(getBlock())) {
                level.removeBlock(basePos, false);
            }
            return false;
        }
        return true;
    }
}
