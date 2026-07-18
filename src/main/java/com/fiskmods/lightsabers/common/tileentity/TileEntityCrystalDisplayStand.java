package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemCrystalBlock;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
    public void load(CompoundTag tag) {
        super.load(tag);
        boolean displayPresent = tag.contains(DISPLAY_PRESENT_TAG, Tag.TAG_BYTE)
                ? tag.getBoolean(DISPLAY_PRESENT_TAG)
                : tag.contains(DISPLAY_STACK_TAG, Tag.TAG_COMPOUND);
        displayStack = displayPresent
                && tag.contains(DISPLAY_STACK_TAG, Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound(DISPLAY_STACK_TAG))
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(DISPLAY_PRESENT_TAG, !displayStack.isEmpty());
        if (!displayStack.isEmpty()) {
            tag.put(DISPLAY_STACK_TAG, displayStack.save(new CompoundTag()));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
