package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileEntitySithCoffin extends BlockEntity implements Container {
    public static final int LID_OPEN_MAX = 60;

    private static final int INVENTORY_SIZE = 28;
    private static final double PARTICLE_RADIUS = 0.5D;
    private static final double PARTICLE_STEP = 0.025D;
    private static final String HAS_BEEN_OPENED_TAG = "HasBeenOpened";
    private static final String IS_LID_OPEN_TAG = "IsLidOpen";
    private static final String LID_OPEN_TIMER_TAG = "LidOpenTimer";

    private final NonNullList<ItemStack> items = NonNullList.withSize(
            INVENTORY_SIZE,
            ItemStack.EMPTY
    );

    private boolean hasBeenOpened;
    private boolean lidOpen;
    private int lidOpenTimer;
    private int previousLidOpenTimer;

    public TileEntitySithCoffin(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SITH_COFFIN.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            TileEntitySithCoffin coffin
    ) {
        boolean completedFirstOpen = coffin.tickLid();
        if (completedFirstOpen) {
            coffin.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(
            Level level,
            BlockPos pos,
            BlockState state,
            TileEntitySithCoffin coffin
    ) {
        coffin.tickLid();
        if (!coffin.hasBeenOpened && coffin.lidOpenTimer < LID_OPEN_MAX) {
            coffin.spawnOpeningParticles(level, pos, state);
        }
    }

    private boolean tickLid() {
        previousLidOpenTimer = lidOpenTimer;
        if (lidOpen) {
            if (lidOpenTimer < LID_OPEN_MAX) {
                lidOpenTimer++;
            }
        } else if (lidOpenTimer > 0) {
            lidOpenTimer--;
        }

        if (!hasBeenOpened && lidOpenTimer == LID_OPEN_MAX) {
            hasBeenOpened = true;
            return true;
        }
        return false;
    }

    private void spawnOpeningParticles(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        RandomSource random = level.random;
        double frontOffsetX = facing.getStepX() * 0.5D;
        double frontOffsetZ = facing.getStepZ() * 0.5D;
        double maxHeight = lidOpenTimer / 5.0D;

        for (double y = 0.0D; y <= maxHeight; y += PARTICLE_STEP) {
            double curve = Math.cos(y * 2.0D);
            double angle = y + lidOpenTimer / 2.0D;
            double x = PARTICLE_RADIUS * Math.cos(angle) * curve;
            double z = PARTICLE_RADIUS * Math.sin(angle) * curve;
            double motionX = (random.nextFloat() - 0.5F) * 0.5F * curve;
            double motionY = (random.nextFloat() - 0.5F) * 0.1F;
            double motionZ = (random.nextFloat() - 0.5F) * 0.5F * curve;
            level.addParticle(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5D + x + frontOffsetX,
                    pos.getY() + 0.8D + y,
                    pos.getZ() + 0.5D + z + frontOffsetZ,
                    motionX,
                    motionY,
                    motionZ
            );
        }
    }

    public boolean toggleLid() {
        if (lidOpenTimer == 0) {
            lidOpen = true;
            playLidSound(ModSounds.SITH_COFFIN_OPEN.get());
        } else if (lidOpenTimer == LID_OPEN_MAX) {
            lidOpen = false;
            playLidSound(ModSounds.SITH_COFFIN_CLOSE.get());
        } else {
            return false;
        }

        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private void playLidSound(SoundEvent sound) {
        if (level != null) {
            level.playSound(
                    null,
                    worldPosition,
                    sound,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
    }

    public boolean hasBeenOpened() {
        return hasBeenOpened;
    }

    public int getLidOpenTimer() {
        return lidOpenTimer;
    }

    public float getLidOpenProgress(float partialTick) {
        return (previousLidOpenTimer
                + (lidOpenTimer - previousLidOpenTimer) * partialTick) / LID_OPEN_MAX;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        hasBeenOpened = tag.getBoolean(HAS_BEEN_OPENED_TAG);
        lidOpen = tag.getBoolean(IS_LID_OPEN_TAG);
        lidOpenTimer = tag.getInt(LID_OPEN_TIMER_TAG);
        previousLidOpenTimer = lidOpenTimer;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putBoolean(HAS_BEEN_OPENED_TAG, hasBeenOpened);
        tag.putBoolean(IS_LID_OPEN_TAG, lidOpen);
        tag.putInt(LID_OPEN_TIMER_TAG, lidOpenTimer);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries
    ) {
        CompoundTag tag = packet.getTag();
        if (!tag.isEmpty()) {
            loadWithComponents(tag, registries);
        }
    }
}
