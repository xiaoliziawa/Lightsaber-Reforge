package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.container.ContainerHolocron;
import com.fiskmods.lightsabers.common.tileentity.ModBlockEntities;
import com.fiskmods.lightsabers.common.tileentity.TileEntityHolocron;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class BlockHolocron extends BaseEntityBlock {
    private static final int SHAPE_STEPS = 100;
    private static final VoxelShape[] SHAPES = createShapes();

    private final HolocronType type;

    public BlockHolocron(BlockBehaviour.Properties properties, HolocronType type) {
        super(properties);
        this.type = type;
    }

    public HolocronType getType() {
        return type;
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
        if (!(player instanceof ServerPlayer serverPlayer)
                || !(level.getBlockEntity(pos) instanceof TileEntityHolocron holocron)) {
            return InteractionResult.CONSUME;
        }

        NetworkHooks.openScreen(
                serverPlayer,
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                new ContainerHolocron(containerId, inventory, holocron),
                        Component.translatable("gui.forcePowers")
                ),
                buffer -> buffer.writeBlockPos(pos)
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
        float openProgress = level.getBlockEntity(pos) instanceof TileEntityHolocron holocron
                ? holocron.getOpenProgress(0)
                : 0;
        int shapeIndex = Mth.clamp(Math.round(openProgress * SHAPE_STEPS), 0, SHAPE_STEPS);
        return SHAPES[shapeIndex];
    }

    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[SHAPE_STEPS + 1];
        for (int index = 0; index <= SHAPE_STEPS; index++) {
            float openProgress = (float) index / SHAPE_STEPS;
            shapes[index] = createShape(openProgress);
        }
        return shapes;
    }

    private static VoxelShape createShape(float openProgress) {
        float size = 0.25F;
        float offset = size * openProgress;
        return Block.box(
                size * 16,
                offset * 16,
                size * 16,
                (1 - size) * 16,
                (1 - size * 2 + offset) * 16,
                (1 - size) * 16
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityHolocron(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        return createTickerHelper(
                blockEntityType,
                ModBlockEntities.HOLOCRON.get(),
                TileEntityHolocron::tick
        );
    }
}
