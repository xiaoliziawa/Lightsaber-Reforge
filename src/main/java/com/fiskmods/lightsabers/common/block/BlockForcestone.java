package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.common.item.ItemForcestone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

public class BlockForcestone extends RotatedPillarBlock {
    public static final EnumProperty<Variant> VARIANT =
            EnumProperty.create("variant", Variant.class);

    public BlockForcestone(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(VARIANT, Variant.DEFAULT)
                .setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemForcestone.create(this, state.getValue(VARIANT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, AXIS);
    }

    public BlockState getStateFromLegacyMetadata(int metadata) {
        return switch (metadata) {
            case 1 -> defaultBlockState().setValue(VARIANT, Variant.INSCRIBED);
            case 2 -> defaultBlockState().setValue(VARIANT, Variant.PILLAR);
            case 3 -> defaultBlockState()
                    .setValue(VARIANT, Variant.PILLAR)
                    .setValue(AXIS, Direction.Axis.X);
            case 4 -> defaultBlockState()
                    .setValue(VARIANT, Variant.PILLAR)
                    .setValue(AXIS, Direction.Axis.Z);
            case 5 -> defaultBlockState().setValue(VARIANT, Variant.CRACKED);
            case 6 -> defaultBlockState().setValue(VARIANT, Variant.MOSSY);
            default -> defaultBlockState();
        };
    }

    public int getLegacyMetadata(BlockState state) {
        Variant variant = state.getValue(VARIANT);
        if (variant != Variant.PILLAR) {
            return variant.legacyMetadata;
        }
        return switch (state.getValue(AXIS)) {
            case X -> 3;
            case Z -> 4;
            default -> 2;
        };
    }

    public enum Variant implements StringRepresentable {
        DEFAULT(0),
        INSCRIBED(1),
        PILLAR(2),
        CRACKED(5),
        MOSSY(6);

        private final int legacyMetadata;
        private final String serializedName;

        Variant(int legacyMetadata) {
            this.legacyMetadata = legacyMetadata;
            serializedName = name().toLowerCase(Locale.ROOT);
        }

        public int getLegacyMetadata() {
            return legacyMetadata;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public static Variant byName(String name) {
            for (Variant variant : values()) {
                if (variant.serializedName.equals(name)) {
                    return variant;
                }
            }
            return DEFAULT;
        }
    }
}
