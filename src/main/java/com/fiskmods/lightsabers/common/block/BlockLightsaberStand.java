package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.tileentity.TileEntityLightsaberStand;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockLightsaberStand extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create(
            "facing",
            Direction.UP,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    );
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape FLOOR_X_SHAPE = Block.box(
            2.75D, 0.0D, 5.25D,
            13.25D, 3.0D, 10.75D
    );
    private static final VoxelShape FLOOR_Z_SHAPE = Block.box(
            5.25D, 0.0D, 2.75D,
            10.75D, 3.0D, 13.25D
    );
    private static final VoxelShape NORTH_SHAPE = Block.box(
            2.75D, 5.25D, 13.0D,
            13.25D, 10.75D, 16.0D
    );
    private static final VoxelShape SOUTH_SHAPE = Block.box(
            2.75D, 5.25D, 0.0D,
            13.25D, 10.75D, 3.0D
    );
    private static final VoxelShape WEST_SHAPE = Block.box(
            13.0D, 5.25D, 2.75D,
            16.0D, 10.75D, 13.25D
    );
    private static final VoxelShape EAST_SHAPE = Block.box(
            0.0D, 5.25D, 2.75D,
            3.0D, 10.75D, 13.25D
    );

    public BlockLightsaberStand(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (facing == Direction.DOWN) {
            return null;
        }

        Direction.Axis axis = facing == Direction.UP
                ? context.getHorizontalDirection().getClockWise().getAxis()
                : Direction.Axis.X;
        BlockState state = defaultBlockState()
                .setValue(FACING, facing)
                .setValue(AXIS, axis);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.DOWN) {
            return false;
        }

        BlockPos supportPos = pos.relative(facing.getOpposite());
        return Block.canSupportCenter(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (direction == state.getValue(FACING).getOpposite()
                && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            case UP -> state.getValue(AXIS) == Direction.Axis.X
                    ? FLOOR_X_SHAPE
                    : FLOOR_Z_SHAPE;
            default -> FLOOR_X_SHAPE;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        if (placer != null
                && level.getBlockEntity(pos) instanceof TileEntityLightsaberStand stand) {
            stand.setOwner(placer);
        }
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

        if (!(level.getBlockEntity(pos) instanceof TileEntityLightsaberStand stand)) {
            return InteractionResult.PASS;
        }

        if (!player.isCreative() && !stand.isOwner(player)) {
            player.displayClientMessage(
                    Component.translatable("message.lightsaberStand.notOwner")
                            .withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.CONSUME;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.isEmpty() && !(heldStack.getItem() instanceof ItemLightsaberBase)) {
            return InteractionResult.CONSUME;
        }

        ItemStack previousStack = stand.getDisplayStack();
        if (stand.setDisplayStack(heldStack)) {
            player.setItemInHand(hand, previousStack);
        }
        return InteractionResult.CONSUME;
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
            if (level.getBlockEntity(pos) instanceof TileEntityLightsaberStand stand) {
                ItemStack displayStack = stand.getDisplayStack();
                if (!displayStack.isEmpty()) {
                    Block.popResource(level, pos, displayStack.copy());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.UP) {
            Direction.Axis axis = state.getValue(AXIS);
            if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
                axis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            }
            return state.setValue(AXIS, axis);
        }
        return state.setValue(FACING, rotation.rotate(facing));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AXIS);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityLightsaberStand(pos, state);
    }
}
