package com.fiskmods.lightsabers.common.block;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class BlockModStairs extends StairBlock {
    public BlockModStairs(
            Supplier<BlockState> baseState,
            BlockBehaviour.Properties properties
    ) {
        super(baseState, properties);
    }
}
