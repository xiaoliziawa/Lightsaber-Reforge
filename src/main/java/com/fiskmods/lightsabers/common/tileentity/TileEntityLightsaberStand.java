package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TileEntityLightsaberStand extends BlockEntity {
    private static final String DISPLAY_STACK_TAG = "DisplayStack";
    private static final String OWNER_TAG = "Owner";
    private static final String UUID_MOST_TAG = "UUIDMost";
    private static final String UUID_LEAST_TAG = "UUIDLeast";

    private ItemStack displayStack = ItemStack.EMPTY;
    private UUID owner;

    public TileEntityLightsaberStand(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTSABER_STAND.get(), pos, state);
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
        return stack.isEmpty() || stack.getItem() instanceof ItemLightsaberBase;
    }

    public ItemStack getDisplayStack() {
        return displayStack;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(LivingEntity entity) {
        UUID entityId = entity.getUUID();
        if (entityId.equals(owner)) {
            return;
        }

        owner = entityId;
        setChangedAndSync();
    }

    public boolean isOwner(LivingEntity entity) {
        return owner != null && owner.equals(entity.getUUID());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        displayStack = tag.contains(DISPLAY_STACK_TAG, Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(registries, tag.getCompound(DISPLAY_STACK_TAG))
                : ItemStack.EMPTY;
        owner = readOwner(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!displayStack.isEmpty()) {
            tag.put(DISPLAY_STACK_TAG, displayStack.save(registries, new CompoundTag()));
        }
        if (owner != null) {
            CompoundTag ownerTag = new CompoundTag();
            ownerTag.putLong(UUID_MOST_TAG, owner.getMostSignificantBits());
            ownerTag.putLong(UUID_LEAST_TAG, owner.getLeastSignificantBits());
            tag.put(OWNER_TAG, ownerTag);
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

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static UUID readOwner(CompoundTag tag) {
        if (!tag.contains(OWNER_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag ownerTag = tag.getCompound(OWNER_TAG);
        if (!ownerTag.contains(UUID_MOST_TAG, Tag.TAG_LONG)
                || !ownerTag.contains(UUID_LEAST_TAG, Tag.TAG_LONG)) {
            return null;
        }
        return new UUID(ownerTag.getLong(UUID_MOST_TAG), ownerTag.getLong(UUID_LEAST_TAG));
    }
}
