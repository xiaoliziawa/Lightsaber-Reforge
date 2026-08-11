package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemCrystalBlock;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class TileEntityCrystalDisplayStand extends BlockEntity {
    private static final String DISPLAY_STACK_TAG = "DisplayStack";
    private static final String DISPLAY_PRESENT_TAG = "DisplayPresent";

    private ItemStack displayStack = ItemStack.EMPTY;

    public TileEntityCrystalDisplayStand(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_DISPLAY_STAND.get(), pos, state);
    }

    public boolean setDisplayStack(ItemStack stack) {
        if (!isItemValid(stack) || ItemStack.matches(displayStack, stack)) {
            return false;
        }

        displayStack = stack.copy();
        setChangedAndSync();
        return true;
    }

    public boolean isItemValid(ItemStack stack) {
        return stack.isEmpty()
                || stack.getItem() instanceof ItemCrystal
                || stack.getItem() instanceof ItemCrystalBlock
                || stack.getItem() instanceof ItemLightsaberBase;
    }

    public ItemStack getDisplayStack() {
        return displayStack;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        Optional<ItemStack> storedStack = input.read(DISPLAY_STACK_TAG, ItemStack.CODEC);
        boolean displayPresent = input.getBooleanOr(DISPLAY_PRESENT_TAG, storedStack.isPresent());
        displayStack = displayPresent ? storedStack.orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(DISPLAY_PRESENT_TAG, !displayStack.isEmpty());
        if (!displayStack.isEmpty()) {
            output.store(DISPLAY_STACK_TAG, ItemStack.CODEC, displayStack);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !displayStack.isEmpty()) {
            Block.popResource(level, pos, displayStack.copy());
        }
        super.preRemoveSideEffects(pos, state);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
