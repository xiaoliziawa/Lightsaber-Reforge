package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.item.ItemForcestoneSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

public class BlockModSlab extends SlabBlock {
    public static final EnumProperty<Variant> VARIANT =
            EnumProperty.create("variant", Variant.class);

    public BlockModSlab(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(VARIANT, Variant.LIGHT));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Variant heldVariant = ItemForcestoneSlab.getVariant(context.getItemInHand());
        BlockState existingState = context.getLevel().getBlockState(context.getClickedPos());
        if (existingState.is(this) && existingState.getValue(VARIANT) != heldVariant) {
            return null;
        }

        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(VARIANT, heldVariant);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(VARIANT) == ItemForcestoneSlab.getVariant(context.getItemInHand())
                && super.canBeReplaced(state, context);
    }

    @Override
    public ItemStack getCloneItemStack(
            LevelReader level,
            BlockPos pos,
            BlockState state,
            boolean includeData,
            Player player
    ) {
        return ItemForcestoneSlab.create(this, state.getValue(VARIANT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    public enum Variant implements StringRepresentable {
        LIGHT,
        DARK;

        private final String serializedName = name().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public static Variant byName(String name) {
            return DARK.serializedName.equals(name) ? DARK : LIGHT;
        }
    }
}
