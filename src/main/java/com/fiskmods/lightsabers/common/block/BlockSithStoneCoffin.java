package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithStoneCoffin;
import com.mojang.serialization.MapCodec;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockSithStoneCoffin extends BaseEntityBlock {
    public static final MapCodec<BlockSithStoneCoffin> CODEC =
            simpleCodec(BlockSithStoneCoffin::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final String EQUIPMENT_TAG = "Equipment";
    private static final VoxelShape BASEPLATE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, 3.0D, 16.0D
    );
    private static final VoxelShape SOUTH_COFFIN_SHAPE = Block.box(
            1.5D, 3.0D, 1.5D,
            14.5D, 31.0D, 10.5D
    );
    private static final VoxelShape WEST_COFFIN_SHAPE = Block.box(
            5.5D, 3.0D, 1.5D,
            14.5D, 31.0D, 14.5D
    );
    private static final VoxelShape NORTH_COFFIN_SHAPE = Block.box(
            1.5D, 3.0D, 5.5D,
            14.5D, 31.0D, 14.5D
    );
    private static final VoxelShape EAST_COFFIN_SHAPE = Block.box(
            1.5D, 3.0D, 1.5D,
            10.5D, 31.0D, 14.5D
    );
    private static final VoxelShape SOUTH_FULL_SHAPE = Shapes.or(
            BASEPLATE_SHAPE,
            SOUTH_COFFIN_SHAPE
    );
    private static final VoxelShape WEST_FULL_SHAPE = Shapes.or(
            BASEPLATE_SHAPE,
            WEST_COFFIN_SHAPE
    );
    private static final VoxelShape NORTH_FULL_SHAPE = Shapes.or(
            BASEPLATE_SHAPE,
            NORTH_COFFIN_SHAPE
    );
    private static final VoxelShape EAST_FULL_SHAPE = Shapes.or(
            BASEPLATE_SHAPE,
            EAST_COFFIN_SHAPE
    );
    private static final VoxelShape SOUTH_UPPER_SHAPE = SOUTH_FULL_SHAPE.move(
            0.0D,
            -1.0D,
            0.0D
    );
    private static final VoxelShape WEST_UPPER_SHAPE = WEST_FULL_SHAPE.move(
            0.0D,
            -1.0D,
            0.0D
    );
    private static final VoxelShape NORTH_UPPER_SHAPE = NORTH_FULL_SHAPE.move(
            0.0D,
            -1.0D,
            0.0D
    );
    private static final VoxelShape EAST_UPPER_SHAPE = EAST_FULL_SHAPE.move(
            0.0D,
            -1.0D,
            0.0D
    );

    public BlockSithStoneCoffin(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH)
                .setValue(PART, Part.BASE));
    }

    @Override
    protected MapCodec<? extends BlockSithStoneCoffin> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1
                || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState()
                .setValue(
                        HorizontalDirectionalBlock.FACING,
                        context.getHorizontalDirection().getOpposite()
                )
                .setValue(PART, Part.BASE);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(PART) == Part.UPPER) {
            BlockState baseState = level.getBlockState(pos.below());
            return baseState.is(this)
                    && baseState.getValue(PART) == Part.BASE
                    && baseState.getValue(HorizontalDirectionalBlock.FACING)
                    == state.getValue(HorizontalDirectionalBlock.FACING);
        }
        BlockPos supportPos = pos.below();
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof TileEntitySithStoneCoffin coffin) {
            coffin.loadEquipmentFromItem(stack);
            coffin.setTaskFinished(true);
        }
    }

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean isMoving
    ) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            level.scheduleTick(getBasePos(state, pos), this, 1);
        }
    }

    @Override
    public void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        tryActivate(level, state, pos);
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean isMoving
    ) {
        if (!level.isClientSide) {
            tryActivate(level, state, pos);
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
    }

    private void tryActivate(Level level, BlockState state, BlockPos pos) {
        BlockPos basePos = getBasePos(state, pos);
        if ((level.hasNeighborSignal(basePos) || level.hasNeighborSignal(basePos.above()))
                && level.getBlockEntity(basePos) instanceof TileEntitySithStoneCoffin coffin) {
            coffin.releaseGhost(null);
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            BlockPos basePos = getBasePos(state, pos);
            if (level.getBlockEntity(basePos) instanceof TileEntitySithStoneCoffin coffin
                    && !coffin.isBaseplateOnly()
                    && coffin.isTaskFinished()) {
                ItemStack droppedStack = new ItemStack(this);
                ItemStack equipment = coffin.getEquipment();
                if (!equipment.isEmpty()) {
                    CompoundTag equipmentTag = new CompoundTag();
                    equipment.save(level.registryAccess(), equipmentTag);
                    ItemDataHelper.updateCustomData(
                            droppedStack,
                            tag -> tag.put(EQUIPMENT_TAG, equipmentTag)
                    );
                }

                BlockState baseState = level.getBlockState(basePos);
                level.levelEvent(player, 2001, basePos, Block.getId(baseState));
                popResource(level, basePos, droppedStack);
                level.removeBlock(basePos, false);
                return;
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        BlockPos basePos = getBasePos(state, pos);
        if (level.getBlockEntity(basePos) instanceof TileEntitySithStoneCoffin coffin
                && coffin.isBaseplateOnly()) {
            return state.getValue(PART) == Part.BASE ? BASEPLATE_SHAPE : Shapes.empty();
        }

        return getFullShape(state, state.getValue(PART) == Part.UPPER);
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(PART) == Part.BASE
                ? getShape(state, level, pos, context)
                : Shapes.empty();
    }

    private VoxelShape getFullShape(BlockState state, boolean upper) {
        return switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> upper ? NORTH_UPPER_SHAPE : NORTH_FULL_SHAPE;
            case WEST -> upper ? WEST_UPPER_SHAPE : WEST_FULL_SHAPE;
            case EAST -> upper ? EAST_UPPER_SHAPE : EAST_FULL_SHAPE;
            default -> upper ? SOUTH_UPPER_SHAPE : SOUTH_FULL_SHAPE;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean isMoving
    ) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            if (state.getValue(PART) == Part.BASE) {
                removeMatchingUpper(level, pos.above(), facing, isMoving);
            } else {
                BlockPos basePos = pos.below();
                BlockState baseState = level.getBlockState(basePos);
                if (isMatchingPart(baseState, facing, Part.BASE)
                        && (!(level.getBlockEntity(basePos)
                        instanceof TileEntitySithStoneCoffin coffin)
                        || !coffin.isBaseplateOnly())) {
                    level.removeBlock(basePos, isMoving);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(
                HorizontalDirectionalBlock.FACING,
                rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING))
        );
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(
                state,
                mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING))
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, PART);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.BASE
                ? new TileEntitySithStoneCoffin(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide || state.getValue(PART) != Part.BASE) {
            return null;
        }
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.SITH_STONE_COFFIN.get(),
                TileEntitySithStoneCoffin::serverTick
        );
    }

    public static BlockPos getBasePos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == Part.BASE ? pos : pos.below();
    }

    private void removeMatchingUpper(
            Level level,
            BlockPos pos,
            Direction facing,
            boolean isMoving
    ) {
        if (isMatchingPart(level.getBlockState(pos), facing, Part.UPPER)) {
            level.removeBlock(pos, isMoving);
        }
    }

    private boolean isMatchingPart(BlockState state, Direction facing, Part part) {
        return state.is(this)
                && state.getValue(HorizontalDirectionalBlock.FACING) == facing
                && state.getValue(PART) == part;
    }

    public enum Part implements StringRepresentable {
        BASE("base"),
        UPPER("upper");

        private final String serializedName;

        Part(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
