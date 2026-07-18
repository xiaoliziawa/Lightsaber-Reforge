package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.BlockLightsaberForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ItemLightsaberForge extends BlockItem {
    public ItemLightsaberForge(BlockLightsaberForge block) {
        super(block, new Item.Properties());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos basePos = context.getClickedPos();
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos panelPos = BlockLightsaberForge.getPanelPos(basePos, facing);

        if (!level.setBlock(basePos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return false;
        }
        if (level.setBlock(
                panelPos,
                state.setValue(BlockLightsaberForge.PART, BlockLightsaberForge.Part.PANEL),
                Block.UPDATE_ALL
        )) {
            return true;
        }

        level.removeBlock(basePos, false);
        return false;
    }
}
