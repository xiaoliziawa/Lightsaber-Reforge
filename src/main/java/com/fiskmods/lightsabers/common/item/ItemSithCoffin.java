package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.BlockSithCoffin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSithCoffin extends BlockItem {
    public ItemSithCoffin(BlockSithCoffin block) {
        super(block, new Item.Properties());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos basePos = context.getClickedPos();
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos frontPos = BlockSithCoffin.getFrontPos(basePos, facing);

        if (!level.setBlock(basePos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return false;
        }
        if (!level.setBlock(
                frontPos,
                state.setValue(BlockSithCoffin.PART, BlockSithCoffin.Part.FRONT),
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
