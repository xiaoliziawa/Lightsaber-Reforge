package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.container.ContainerLightsaberForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class BlockLightsaberForge extends Block {
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final VoxelShape SOUTH_BASE_SHAPE = Block.box(
            -16.0D, 0.0D, 0.0D,
            16.0D, 13.0D, 16.0D
    );
    private static final VoxelShape SOUTH_PANEL_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            32.0D, 13.0D, 16.0D
    );
    private static final VoxelShape NORTH_BASE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            32.0D, 13.0D, 16.0D
    );
    private static final VoxelShape NORTH_PANEL_SHAPE = Block.box(
            -16.0D, 0.0D, 0.0D,
            16.0D, 13.0D, 16.0D
    );
    private static final VoxelShape WEST_BASE_SHAPE = Block.box(
            0.0D, 0.0D, -16.0D,
            16.0D, 13.0D, 16.0D
    );
    private static final VoxelShape WEST_PANEL_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, 13.0D, 32.0D
    );
    private static final VoxelShape EAST_BASE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, 13.0D, 32.0D
    );
    private static final VoxelShape EAST_PANEL_SHAPE = Block.box(
            0.0D, 0.0D, -16.0D,
            16.0D, 13.0D, 16.0D
    );

    public BlockLightsaberForge(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH)
                .setValue(PART, Part.BASE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(PART, Part.BASE);
        BlockPos panelPos = getPanelPos(context.getClickedPos(), facing);
        BlockState panelState = context.getLevel().getBlockState(panelPos);
        Player player = context.getPlayer();

        if (!panelState.canBeReplaced()
                || !context.getLevel().getWorldBorder().isWithinBounds(panelPos)
                || !context.getLevel().isUnobstructed(
                        state.setValue(PART, Part.PANEL),
                        panelPos,
                        player == null
                                ? CollisionContext.empty()
                                : CollisionContext.of(player)
                )) {
            return null;
        }
        if (player != null && !player.mayUseItemAt(
                panelPos,
                context.getClickedFace(),
                context.getItemInHand()
        )) {
            return null;
        }
        return state;
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
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        BlockPos basePos = getBasePos(state, pos);
        NetworkHooks.openScreen(
                serverPlayer,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                new ContainerLightsaberForge(
                                        containerId,
                                        inventory,
                                        basePos
                                ),
                        Component.translatable("gui.lightsaber_forge")
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
        boolean panel = state.getValue(PART) == Part.PANEL;
        return switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> panel ? NORTH_PANEL_SHAPE : NORTH_BASE_SHAPE;
            case WEST -> panel ? WEST_PANEL_SHAPE : WEST_BASE_SHAPE;
            case EAST -> panel ? EAST_PANEL_SHAPE : EAST_BASE_SHAPE;
            default -> panel ? SOUTH_PANEL_SHAPE : SOUTH_BASE_SHAPE;
        };
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
            BlockPos counterpartPos = getCounterpartPos(state, pos);
            BlockState counterpartState = level.getBlockState(counterpartPos);
            if (counterpartState.is(this)
                    && counterpartState.getValue(PART) != state.getValue(PART)
                    && counterpartState.getValue(HorizontalDirectionalBlock.FACING)
                    == state.getValue(HorizontalDirectionalBlock.FACING)) {
                level.removeBlock(counterpartPos, isMoving);
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

    public static BlockPos getPanelPos(BlockPos basePos, Direction facing) {
        return basePos.relative(facing.getClockWise());
    }

    public static BlockPos getBasePos(BlockState state, BlockPos pos) {
        if (state.getValue(PART) == Part.BASE) {
            return pos;
        }
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        return pos.relative(facing.getCounterClockWise());
    }

    private static BlockPos getCounterpartPos(BlockState state, BlockPos pos) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        return state.getValue(PART) == Part.BASE
                ? getPanelPos(pos, facing)
                : pos.relative(facing.getCounterClockWise());
    }

    public enum Part implements StringRepresentable {
        BASE("base"),
        PANEL("panel");

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
