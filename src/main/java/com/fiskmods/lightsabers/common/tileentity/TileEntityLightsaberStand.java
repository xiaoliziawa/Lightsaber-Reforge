package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        displayStack = input.read(DISPLAY_STACK_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        owner = readOwner(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!displayStack.isEmpty()) {
            output.store(DISPLAY_STACK_TAG, ItemStack.CODEC, displayStack);
        }
        if (owner != null) {
            ValueOutput ownerOutput = output.child(OWNER_TAG);
            ownerOutput.putLong(UUID_MOST_TAG, owner.getMostSignificantBits());
            ownerOutput.putLong(UUID_LEAST_TAG, owner.getLeastSignificantBits());
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
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static UUID readOwner(ValueInput input) {
        return input.child(OWNER_TAG).flatMap(ownerInput -> {
            if (ownerInput.getLong(UUID_MOST_TAG).isEmpty()
                    || ownerInput.getLong(UUID_LEAST_TAG).isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new UUID(
                    ownerInput.getLongOr(UUID_MOST_TAG, 0L),
                    ownerInput.getLongOr(UUID_LEAST_TAG, 0L)
            ));
        }).orElse(null);
    }
}
