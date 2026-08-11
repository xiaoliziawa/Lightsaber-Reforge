package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.container.ContainerSithCoffin;
import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithCoffin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockSithCoffin extends BaseEntityBlock {
    public static final MapCodec<BlockSithCoffin> CODEC = simpleCodec(BlockSithCoffin::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final double HEIGHT = 15.0D;
    private static final VoxelShape SOUTH_BASE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, HEIGHT, 32.0D
    );
    private static final VoxelShape SOUTH_FRONT_SHAPE = Block.box(
            0.0D, 0.0D, -16.0D,
            16.0D, HEIGHT, 16.0D
    );
    private static final VoxelShape WEST_BASE_SHAPE = Block.box(
            -16.0D, 0.0D, 0.0D,
            16.0D, HEIGHT, 16.0D
    );
    private static final VoxelShape WEST_FRONT_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            32.0D, HEIGHT, 16.0D
    );
    private static final VoxelShape NORTH_BASE_SHAPE = Block.box(
            0.0D, 0.0D, -16.0D,
            16.0D, HEIGHT, 16.0D
    );
    private static final VoxelShape NORTH_FRONT_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            16.0D, HEIGHT, 32.0D
    );
    private static final VoxelShape EAST_BASE_SHAPE = Block.box(
            0.0D, 0.0D, 0.0D,
            32.0D, HEIGHT, 16.0D
    );
    private static final VoxelShape EAST_FRONT_SHAPE = Block.box(
            -16.0D, 0.0D, 0.0D,
            16.0D, HEIGHT, 16.0D
    );

    public BlockSithCoffin(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH)
                .setValue(PART, Part.BASE));
    }

    @Override
    protected MapCodec<? extends BlockSithCoffin> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos basePos = context.getClickedPos();
        BlockPos frontPos = getFrontPos(basePos, facing);
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState state = defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(PART, Part.BASE);

        if (!level.getBlockState(frontPos).canBeReplaced(context)
                || !level.getWorldBorder().isWithinBounds(frontPos)
                || !level.getBlockState(basePos.below()).isFaceSturdy(
                        level,
                        basePos.below(),
                        Direction.UP
                )
                || !level.getBlockState(frontPos.below()).isFaceSturdy(
                        level,
                        frontPos.below(),
                        Direction.UP
                )
                || player != null && !player.mayUseItemAt(
                        frontPos,
                        context.getClickedFace(),
                        context.getItemInHand()
                )
                || !level.isUnobstructed(
                        state.setValue(PART, Part.FRONT),
                        frontPos,
                        player == null
                                ? CollisionContext.empty()
                                : CollisionContext.of(player)
                )) {
            return null;
        }
        return state;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos basePos = getBasePos(state, pos);
        if (!(level.getBlockEntity(basePos) instanceof TileEntitySithCoffin coffin)) {
            return InteractionResult.CONSUME;
        }

        if (!coffin.hasBeenOpened()
                || coffin.getLidOpenTimer() == 0
                || player.isShiftKeyDown()) {
            coffin.toggleLid();
            return InteractionResult.CONSUME;
        }

        if (coffin.getLidOpenTimer() == TileEntitySithCoffin.LID_OPEN_MAX
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) ->
                                    new ContainerSithCoffin(
                                            containerId,
                                            inventory,
                                            coffin
                                    ),
                            Component.translatable("gui.sith_coffin")
                    ),
                    buffer -> buffer.writeBlockPos(basePos)
            );
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        boolean front = state.getValue(PART) == Part.FRONT;
        return switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case NORTH -> front ? NORTH_FRONT_SHAPE : NORTH_BASE_SHAPE;
            case WEST -> front ? WEST_FRONT_SHAPE : WEST_BASE_SHAPE;
            case EAST -> front ? EAST_FRONT_SHAPE : EAST_BASE_SHAPE;
            default -> front ? SOUTH_FRONT_SHAPE : SOUTH_BASE_SHAPE;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockState playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        if (!level.isClientSide() && !player.isCreative()) {
            popResource(level, pos, new ItemStack(this));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            boolean isMoving
    ) {
        if (state.getValue(PART) == Part.BASE) {
            level.updateNeighbourForOutputSignal(pos, this);
            removeMatchingPart(
                    level,
                    getFrontPos(pos, state.getValue(HorizontalDirectionalBlock.FACING)),
                    state.getValue(HorizontalDirectionalBlock.FACING),
                    Part.FRONT,
                    isMoving
            );
        } else {
            BlockPos basePos = getBasePos(state, pos);
            BlockState baseState = level.getBlockState(basePos);
            if (isMatchingPart(
                    baseState,
                    state.getValue(HorizontalDirectionalBlock.FACING),
                    Part.BASE
            )) {
                level.removeBlock(basePos, isMoving);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(
            BlockState state,
            Level level,
            BlockPos pos,
            Direction direction
    ) {
        BlockPos basePos = getBasePos(state, pos);
        return AbstractContainerMenu.getRedstoneSignalFromContainer(
                level.getBlockEntity(basePos) instanceof TileEntitySithCoffin coffin
                        ? coffin
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
                ? new TileEntitySithCoffin(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        if (state.getValue(PART) != Part.BASE) {
            return null;
        }
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.SITH_COFFIN.get(),
                level.isClientSide()
                        ? TileEntitySithCoffin::clientTick
                        : TileEntitySithCoffin::serverTick
        );
    }

    public static BlockPos getFrontPos(BlockPos basePos, Direction facing) {
        return basePos.relative(facing);
    }

    public static BlockPos getBasePos(BlockState state, BlockPos pos) {
        return state.getValue(PART) == Part.BASE
                ? pos
                : pos.relative(state.getValue(HorizontalDirectionalBlock.FACING).getOpposite());
    }

    private void removeMatchingPart(
            Level level,
            BlockPos pos,
            Direction facing,
            Part part,
            boolean isMoving
    ) {
        if (isMatchingPart(level.getBlockState(pos), facing, part)) {
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
        FRONT("front");

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
