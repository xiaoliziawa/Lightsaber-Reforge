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
import noobanidus.mods.lootr.block.entities.LootrInventoryBlockEntity;
import noobanidus.mods.lootr.init.ModBlocks;

import java.util.Random;

public final class LootrIntegration {
    private static final int CHEST_SIZE = 27;

    public static boolean generateSithTombChest(
            LevelAccessor level,
            BlockPos pos,
            String category,
            Direction facing,
            Random random,
            int lootVariant
    ) {
        BlockState existing = level.getBlockState(pos);
        if (existing.is(Blocks.CHEST) || existing.is(ModBlocks.INVENTORY.get())) {
            return false;
        }

        level.setBlock(
                pos,
                ModBlocks.INVENTORY.get().defaultBlockState().setValue(ChestBlock.FACING, facing),
                Block.UPDATE_CLIENTS
        );

        if (level.getBlockEntity(pos) instanceof LootrInventoryBlockEntity inventory) {
            inventory.setCustomInventory(createCustomInventory(category, random, lootVariant));
            inventory.setChanged();
            return true;
        }
        return false;
    }

    private static NonNullList<ItemStack> createCustomInventory(
            String category,
            Random random,
            int lootVariant
    ) {
        SimpleContainer container = new SimpleContainer(CHEST_SIZE);
        ModChestGen.fill(container, category, random, lootVariant);

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
