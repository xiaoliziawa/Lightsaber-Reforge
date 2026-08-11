package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.common.block.BlockSithStoneCoffin;
import com.fiskmods.lightsabers.common.entity.EntitySithGhost;
import com.fiskmods.lightsabers.common.entity.ModEntities;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TileEntitySithStoneCoffin extends BlockEntity {
    private static final double ACTIVATION_RADIUS = 14.0D;
    private static final double ACTIVATION_VERTICAL_RANGE = 2.0D;
    private static final int PLAYER_CHECK_INTERVAL_TICKS = 20;
    private static final String COFFIN_X_TAG = "CoffinX";
    private static final String COFFIN_Y_TAG = "CoffinY";
    private static final String COFFIN_Z_TAG = "CoffinZ";
    private static final String EQUIPMENT_TAG = "Equipment";
    private static final String BASEPLATE_ONLY_TAG = "BaseplateOnly";
    private static final String TASK_FINISHED_TAG = "TaskFinished";

    private ItemStack equipment = ItemStack.EMPTY;
    private boolean baseplateOnly;
    private boolean taskFinished;
    @Nullable
    private BlockPos mainCoffinPos;

    public TileEntitySithStoneCoffin(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SITH_STONE_COFFIN.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            TileEntitySithStoneCoffin coffin
    ) {
        if (coffin.baseplateOnly || coffin.taskFinished || coffin.mainCoffinPos == null) {
            return;
        }
        if (Math.floorMod(
                level.getGameTime() + pos.hashCode(),
                PLAYER_CHECK_INTERVAL_TICKS
        ) != 0) {
            return;
        }
        if (!(level.getBlockEntity(coffin.mainCoffinPos) instanceof TileEntitySithCoffin)) {
            return;
        }

        AABB activationArea = new AABB(coffin.mainCoffinPos).inflate(
                ACTIVATION_RADIUS,
                ACTIVATION_VERTICAL_RANGE,
                ACTIVATION_RADIUS
        );
        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                activationArea,
                player -> !player.isSpectator() && player.isAlive()
        );
        if (!players.isEmpty()) {
            coffin.taskFinished = true;
            coffin.releaseGhost(players.get(0));
        }
    }

    public boolean releaseGhost(@Nullable LivingEntity target) {
        if (baseplateOnly || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        BlockState state = getBlockState();
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        EntitySithGhost ghost = new EntitySithGhost(ModEntities.SITH_GHOST.get(), serverLevel);
        ghost.snapTo(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 3.0D / 16.0D,
                worldPosition.getZ() + 0.5D,
                facing.toYRot(),
                0.0F
        );
        ghost.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(worldPosition),
                EntitySpawnReason.TRIGGERED,
                null
        );
        ghost.tickCount = -ghost.getRandom().nextInt(20);
        ghost.setRestingPlace(worldPosition);

        if (!equipment.isEmpty()) {
            ItemStack ghostEquipment = equipment.copy();
            ItemLightsaberBase.setActive(ghostEquipment, false);
            ghost.setItemSlot(EquipmentSlot.MAINHAND, ghostEquipment);
            equipment = ItemStack.EMPTY;
        }
        if (target != null) {
            ghost.setTarget(target);
        }

        baseplateOnly = true;
        taskFinished = true;
        breakUpperPart(serverLevel, state);
        setChangedAndSync();
        serverLevel.addFreshEntity(ghost);
        return true;
    }

    public void restoreFromGhost(ItemStack returnedEquipment) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        equipment = returnedEquipment.copy();
        baseplateOnly = false;
        taskFinished = true;
        BlockState state = getBlockState();
        BlockPos upperPos = worldPosition.above();
        if (serverLevel.getBlockState(upperPos).canBeReplaced()) {
            serverLevel.setBlock(
                    upperPos,
                    state.setValue(
                            BlockSithStoneCoffin.PART,
                            BlockSithStoneCoffin.Part.UPPER
                    ),
                    Block.UPDATE_ALL
            );
        } else {
            baseplateOnly = true;
        }
        setChangedAndSync();
    }

    private void breakUpperPart(ServerLevel level, BlockState state) {
        BlockPos upperPos = worldPosition.above();
        BlockState upperState = level.getBlockState(upperPos);
        if (upperState.is(state.getBlock())
                && upperState.getValue(BlockSithStoneCoffin.PART)
                == BlockSithStoneCoffin.Part.UPPER
                && upperState.getValue(HorizontalDirectionalBlock.FACING)
                == state.getValue(HorizontalDirectionalBlock.FACING)) {
            level.levelEvent(2001, upperPos, Block.getId(upperState));
            level.removeBlock(upperPos, false);
        }
        level.levelEvent(2001, worldPosition, Block.getId(state));
    }

    public void setMainCoffin(BlockPos coffinPos) {
        mainCoffinPos = coffinPos.immutable();
        setChanged();
    }

    public ItemStack getEquipment() {
        return equipment;
    }

    public boolean isBaseplateOnly() {
        return baseplateOnly;
    }

    public boolean isTaskFinished() {
        return taskFinished;
    }

    public void setTaskFinished(boolean taskFinished) {
        this.taskFinished = taskFinished;
        setChangedAndSync();
    }

    public void loadEquipmentFromItem(ItemStack stack) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        equipment = tag != null && level != null
                ? tag.read(
                        EQUIPMENT_TAG,
                        ItemStack.CODEC,
                        level.registryAccess().createSerializationContext(NbtOps.INSTANCE)
                ).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        setChanged();
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getInt(COFFIN_X_TAG).isPresent()
                && input.getInt(COFFIN_Y_TAG).isPresent()
                && input.getInt(COFFIN_Z_TAG).isPresent()) {
            mainCoffinPos = new BlockPos(
                    input.getIntOr(COFFIN_X_TAG, 0),
                    input.getIntOr(COFFIN_Y_TAG, 0),
                    input.getIntOr(COFFIN_Z_TAG, 0)
            );
        } else {
            mainCoffinPos = null;
        }
        equipment = input.read(EQUIPMENT_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        baseplateOnly = input.getBooleanOr(BASEPLATE_ONLY_TAG, false);
        taskFinished = input.getBooleanOr(TASK_FINISHED_TAG, false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (mainCoffinPos != null) {
            output.putInt(COFFIN_X_TAG, mainCoffinPos.getX());
            output.putInt(COFFIN_Y_TAG, mainCoffinPos.getY());
            output.putInt(COFFIN_Z_TAG, mainCoffinPos.getZ());
        }
        if (!equipment.isEmpty()) {
            output.store(EQUIPMENT_TAG, ItemStack.CODEC, equipment);
        }
        output.putBoolean(BASEPLATE_ONLY_TAG, baseplateOnly);
        output.putBoolean(TASK_FINISHED_TAG, taskFinished);
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
    public void onDataPacket(Connection connection, ValueInput input) {
        loadWithComponents(input);
    }
}
