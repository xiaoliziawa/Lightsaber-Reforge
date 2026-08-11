package com.fiskmods.lightsabers.common.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;

public class TileEntityHolocron extends BlockEntity {
    private static final String PLAYERS_USING_TAG = "PlayersUsing";

    private int playersUsing;
    private float openTimer;
    private float previousOpenTimer;
    private int openTicks;
    private int previousOpenTicks;

    public TileEntityHolocron(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLOCRON.get(), pos, state);
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            TileEntityHolocron holocron
    ) {
        holocron.previousOpenTimer = holocron.openTimer;
        holocron.previousOpenTicks = holocron.openTicks;

        if (holocron.playersUsing <= 0) {
            holocron.openTimer *= 0.85F;
        } else if (holocron.openTimer < 1) {
            holocron.openTimer += 0.05F;
            holocron.openTimer *= 1.05F;
        }
        if (holocron.openTimer < 1.0E-6F) {
            holocron.openTimer = 0;
        }
        if (holocron.openTimer == 0) {
            holocron.openTicks = 0;
        } else if (holocron.openTimer >= 1) {
            holocron.openTicks++;
        }

        holocron.openTimer = Mth.clamp(holocron.openTimer, 0, 1);
        holocron.playersUsing = Math.max(holocron.playersUsing, 0);
    }

    public void startUsing() {
        playersUsing++;
        syncUsageCount();
    }

    public void stopUsing() {
        playersUsing = Math.max(playersUsing - 1, 0);
        syncUsageCount();
    }

    public float getOpenProgress(float partialTick) {
        return Mth.lerp(partialTick, previousOpenTimer, openTimer);
    }

    public float getOpenTicks(float partialTick) {
        return Mth.lerp(partialTick, previousOpenTicks, openTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getInt(PLAYERS_USING_TAG).ifPresent(count -> playersUsing = Math.max(count, 0));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(PLAYERS_USING_TAG, playersUsing);
        return tag;
    }

    private void syncUsageCount() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
