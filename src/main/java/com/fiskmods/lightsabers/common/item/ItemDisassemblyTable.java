package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.BlockDisassemblyStation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemDisassemblyTable extends BlockItem {
    public ItemDisassemblyTable(BlockDisassemblyStation block, Item.Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos basePos = context.getClickedPos();
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos sidePos = BlockDisassemblyStation.getSidePos(basePos, facing);
        BlockPos[] positions = {
                basePos,
                sidePos,
                basePos.above(),
                sidePos.above()
        };
        BlockDisassemblyStation.Part[] parts = {
                BlockDisassemblyStation.Part.BASE,
                BlockDisassemblyStation.Part.SIDE,
                BlockDisassemblyStation.Part.TOP_BASE,
                BlockDisassemblyStation.Part.TOP_SIDE
        };

        for (int index = 0; index < positions.length; index++) {
            int flags = index == 0 ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL;
            if (!level.setBlock(
                    positions[index],
                    state.setValue(BlockDisassemblyStation.PART, parts[index]),
                    flags
            )) {
                rollback(level, positions);
                return false;
            }
        }
        return true;
    }

    private void rollback(Level level, BlockPos[] positions) {
        for (BlockPos pos : positions) {
            if (level.getBlockState(pos).is(getBlock())) {
                level.removeBlock(pos, false);
            }
        }
    }
}
