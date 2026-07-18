package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.TierSortingRegistry;
import org.jetbrains.annotations.Nullable;

public class BlockCrystal extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final double CRYSTAL_WIDTH = 6.0D;
    private static final double CRYSTAL_HEIGHT = 6.0D;
    private static final double CRYSTAL_MIN = (16.0D - CRYSTAL_WIDTH) / 2.0D;
    private static final double CRYSTAL_MAX = CRYSTAL_MIN + CRYSTAL_WIDTH;
    private static final VoxelShape UP_SHAPE = Block.box(
            CRYSTAL_MIN, 0, CRYSTAL_MIN,
            CRYSTAL_MAX, CRYSTAL_HEIGHT, CRYSTAL_MAX
    );
    private static final VoxelShape DOWN_SHAPE = Block.box(
            CRYSTAL_MIN, 16 - CRYSTAL_HEIGHT, CRYSTAL_MIN,
            CRYSTAL_MAX, 16, CRYSTAL_MAX
    );
    private static final VoxelShape EAST_SHAPE = Block.box(
            0, CRYSTAL_MIN, CRYSTAL_MIN,
            CRYSTAL_HEIGHT, CRYSTAL_MAX, CRYSTAL_MAX
    );
    private static final VoxelShape WEST_SHAPE = Block.box(
            16 - CRYSTAL_HEIGHT, CRYSTAL_MIN, CRYSTAL_MIN,
            16, CRYSTAL_MAX, CRYSTAL_MAX
    );
    private static final VoxelShape SOUTH_SHAPE = Block.box(
            CRYSTAL_MIN, CRYSTAL_MIN, 0,
            CRYSTAL_MAX, CRYSTAL_MAX, CRYSTAL_HEIGHT
    );
    private static final VoxelShape NORTH_SHAPE = Block.box(
            CRYSTAL_MIN, CRYSTAL_MIN, 16 - CRYSTAL_HEIGHT,
            CRYSTAL_MAX, CRYSTAL_MAX, 16
    );

    public BlockCrystal(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
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
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        if (level.getBlockEntity(pos) instanceof TileEntityCrystal crystal) {
            crystal.setColor(ItemCrystal.get(stack));
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof TileEntityCrystal crystal) {
            return ItemCrystal.createBlock(crystal.getColor());
        }
        return ItemCrystal.createBlock(CrystalColor.DEEP_BLUE);
    }

    @Override
    public boolean canHarvestBlock(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Player player
    ) {
        return hasRequiredPickaxe(player.getMainHandItem());
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(
            BlockState state,
            Player player,
            BlockGetter level,
            BlockPos pos
    ) {
        return hasRequiredPickaxe(player.getMainHandItem())
                ? super.getDestroyProgress(state, player, level, pos)
                : 0.0F;
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack tool
    ) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (blockEntity instanceof TileEntityCrystal crystal) {
            Block.popResource(level, pos, ItemCrystal.create(crystal.getColor()));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityCrystal(pos, state);
    }

    @SuppressWarnings("deprecation")
    private static boolean hasRequiredPickaxe(ItemStack stack) {
        if (!stack.is(ItemTags.PICKAXES)
                || !(stack.getItem() instanceof TieredItem tieredItem)) {
            return false;
        }

        Tier tier = tieredItem.getTier();
        if (!TierSortingRegistry.isTierSorted(tier)) {
            return tier.getLevel() >= Tiers.NETHERITE.getLevel();
        }
        return tier == Tiers.NETHERITE
                || TierSortingRegistry.getTiersLowerThan(tier).contains(Tiers.NETHERITE);
    }
}
