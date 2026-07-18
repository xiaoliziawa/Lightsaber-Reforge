package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.container.ContainerDisassemblyStation;
import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import com.fiskmods.lightsabers.common.tileentity.TileEntityDisassemblyStation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class BlockDisassemblyStation extends BaseEntityBlock {
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final VoxelShape BASE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, 14.0D, 16.0D
    );
    private static final VoxelShape SIDE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, 17.0D, 16.0D
    );
    private static final double TOP_WIDTH = 12.4D;
    private static final VoxelShape SOUTH_TOP_BASE_SHAPE = Block.box(
            0.0D, -2.0D, 16.0D - TOP_WIDTH,
            16.0D, 13.0D, 16.0D
    );
    private static final VoxelShape SOUTH_TOP_SIDE_SHAPE = Block.box(
            0.0D, 1.0D, 16.0D - TOP_WIDTH,
            16.0D, 13.0D, 16.0D
    );
    private static final VoxelShape WEST_TOP_BASE_SHAPE = Block.box(
            0.0D, -2.0D, 0.0D,
            TOP_WIDTH, 13.0D, 16.0D
    );
    private static final VoxelShape WEST_TOP_SIDE_SHAPE = Block.box(
            0.0D, 1.0D, 0.0D,
            TOP_WIDTH, 13.0D, 16.0D
    );
    private static final VoxelShape NORTH_TOP_BASE_SHAPE = Block.box(
            0.0D, -2.0D, 0.0D,
            16.0D, 13.0D, TOP_WIDTH
    );
    private static final VoxelShape NORTH_TOP_SIDE_SHAPE = Block.box(
            0.0D, 1.0D, 0.0D,
            16.0D, 13.0D, TOP_WIDTH
    );
    private static final VoxelShape EAST_TOP_BASE_SHAPE = Block.box(
            16.0D - TOP_WIDTH, -2.0D, 0.0D,
            16.0D, 13.0D, 16.0D
    );
    private static final VoxelShape EAST_TOP_SIDE_SHAPE = Block.box(
            16.0D - TOP_WIDTH, 1.0D, 0.0D,
            16.0D, 13.0D, 16.0D
    );

    public BlockDisassemblyStation(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH)
                .setValue(PART, Part.BASE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockState baseState = defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(PART, Part.BASE);
        BlockPos basePos = context.getClickedPos();
        BlockPos sidePos = getSidePos(basePos, facing);

        return canPlacePart(context, sidePos, baseState.setValue(PART, Part.SIDE))
                && canPlacePart(
                        context,
                        basePos.above(),
                        baseState.setValue(PART, Part.TOP_BASE)
                )
                && canPlacePart(
                        context,
                        sidePos.above(),
                        baseState.setValue(PART, Part.TOP_SIDE)
                )
                ? baseState
                : null;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        BlockPos basePos = getBasePos(state, pos);
        if (!(level.getBlockEntity(basePos) instanceof TileEntityDisassemblyStation station)) {
            return InteractionResult.CONSUME;
        }

        NetworkHooks.openScreen(
                serverPlayer,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                new ContainerDisassemblyStation(
                                        containerId,
                                        inventory,
                                        station
                                ),
                        Component.translatable("gui.disassembly_station")
                ),
                buffer -> buffer.writeBlockPos(basePos)
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        Part part = state.getValue(PART);
        if (part == Part.BASE) {
            return BASE_SHAPE;
        }
        if (part == Part.SIDE) {
            return SIDE_SHAPE;
        }

        boolean sideTop = part == Part.TOP_SIDE;
        return switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> sideTop ? NORTH_TOP_SIDE_SHAPE : NORTH_TOP_BASE_SHAPE;
            case WEST -> sideTop ? WEST_TOP_SIDE_SHAPE : WEST_TOP_BASE_SHAPE;
            case EAST -> sideTop ? EAST_TOP_SIDE_SHAPE : EAST_TOP_BASE_SHAPE;
            default -> sideTop ? SOUTH_TOP_SIDE_SHAPE : SOUTH_TOP_BASE_SHAPE;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
            if (state.getValue(PART) == Part.BASE) {
                if (level instanceof ServerLevel
                        && level.getBlockEntity(pos) instanceof TileEntityDisassemblyStation station) {
                    Containers.dropContents(level, pos, station);
                    level.updateNeighbourForOutputSignal(pos, this);
                }
                removeAdditionalParts(level, pos, state, isMoving);
            } else {
                BlockPos basePos = getBasePos(state, pos);
                BlockState baseState = level.getBlockState(basePos);
                if (baseState.is(this) && baseState.getValue(PART) == Part.BASE) {
                    level.removeBlock(basePos, isMoving);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockPos basePos = getBasePos(state, pos);
        return AbstractContainerMenu.getRedstoneSignalFromContainer(
                level.getBlockEntity(basePos) instanceof TileEntityDisassemblyStation station
                        ? station
                        : null
        );
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
                ? new TileEntityDisassemblyStation(pos, state)
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
                ModBlockEntities.DISASSEMBLY_STATION.get(),
                TileEntityDisassemblyStation::serverTick
        );
    }

    public static BlockPos getSidePos(BlockPos basePos, Direction facing) {
        return basePos.relative(facing.getClockWise());
    }

    public static BlockPos getBasePos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        return switch (state.getValue(PART)) {
            case BASE -> pos;
            case SIDE -> pos.relative(facing.getCounterClockWise());
            case TOP_BASE -> pos.below();
            case TOP_SIDE -> pos.below().relative(facing.getCounterClockWise());
        };
    }

    private static boolean canPlacePart(
            BlockPlaceContext context,
            BlockPos pos,
            BlockState state
    ) {
        Player player = context.getPlayer();
        return context.getLevel().getBlockState(pos).canBeReplaced()
                && context.getLevel().getWorldBorder().isWithinBounds(pos)
                && context.getLevel().isUnobstructed(
                        state,
                        pos,
                        player == null
                                ? CollisionContext.empty()
                                : CollisionContext.of(player)
                )
                && (player == null || player.mayUseItemAt(
                        pos,
                        context.getClickedFace(),
                        context.getItemInHand()
                ));
    }

    private void removeAdditionalParts(
            Level level,
            BlockPos basePos,
            BlockState baseState,
            boolean isMoving
    ) {
        Direction facing = baseState.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos sidePos = getSidePos(basePos, facing);
        removePart(level, sidePos, facing, Part.SIDE, isMoving);
        removePart(level, basePos.above(), facing, Part.TOP_BASE, isMoving);
        removePart(level, sidePos.above(), facing, Part.TOP_SIDE, isMoving);
    }

    private void removePart(
            Level level,
            BlockPos pos,
            Direction facing,
            Part part,
            boolean isMoving
    ) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)
                && state.getValue(HorizontalDirectionalBlock.FACING) == facing
                && state.getValue(PART) == part) {
            level.removeBlock(pos, isMoving);
        }
    }

    public enum Part implements StringRepresentable {
        BASE("base"),
        SIDE("side"),
        TOP_BASE("top_base"),
        TOP_SIDE("top_side");

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
