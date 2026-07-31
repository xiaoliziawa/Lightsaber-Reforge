package com.fiskmods.lightsabers.common.integration.lootr;

import com.fiskmods.lightsabers.common.generator.ModChestGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import noobanidus.mods.lootr.common.api.registry.LootrRegistry;
import noobanidus.mods.lootr.common.block.entity.LootrInventoryBlockEntity;

import java.util.Random;

public final class LootrIntegration {
    private static final int CHEST_SIZE = 27;

    private LootrIntegration() {
    }

    public static boolean generateSithTombChest(
            LevelAccessor level,
            BlockPos pos,
            String category,
            Direction facing,
            Random random
    ) {
        Block lootrInventory = LootrRegistry.getInventoryBlock();
        BlockState existing = level.getBlockState(pos);
        if (existing.is(Blocks.CHEST) || existing.is(lootrInventory)) {
            return false;
        }

        level.setBlock(
                pos,
                lootrInventory.defaultBlockState().setValue(ChestBlock.FACING, facing),
                Block.UPDATE_CLIENTS
        );

        if (level.getBlockEntity(pos) instanceof LootrInventoryBlockEntity inventory) {
            inventory.setInfoReferenceInventory(createReferenceInventory(level, category, random));
            inventory.setChanged();
            return true;
        }
        return false;
    }

    private static NonNullList<ItemStack> createReferenceInventory(
            LevelAccessor level,
            String category,
            Random random
    ) {
        SimpleContainer container = new SimpleContainer(CHEST_SIZE);
        ModChestGen.fill(container, category, random, level.registryAccess());

        NonNullList<ItemStack> contents = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                contents.set(slot, stack.copy());
            }
        }
        return contents;
    }
}
