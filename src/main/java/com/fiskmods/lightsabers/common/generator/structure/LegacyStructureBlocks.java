package com.fiskmods.lightsabers.common.generator.structure;

import com.fiskmods.lightsabers.common.block.BlockForcestone;
import com.fiskmods.lightsabers.common.block.BlockModSlab;
import com.fiskmods.lightsabers.common.block.BlockSithCoffin;
import com.fiskmods.lightsabers.common.block.BlockSithStoneCoffin;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class LegacyStructureBlocks {
    public static final Block acacia_stairs = Blocks.ACACIA_STAIRS;
    public static final Block air = Blocks.AIR;
    public static final Block grass = Blocks.GRASS_BLOCK;
    public static final Block leaves = Blocks.OAK_LEAVES;
    public static final Block log = Blocks.OAK_LOG;
    public static final Block planks = Blocks.ACACIA_PLANKS;
    public static final Block redstone_block = Blocks.REDSTONE_BLOCK;
    public static final Block redstone_torch = Blocks.REDSTONE_TORCH;
    public static final Block redstone_wire = Blocks.REDSTONE_WIRE;
    public static final Block stained_glass = Blocks.STAINED_GLASS.white();
    public static final Block stained_glass_pane = Blocks.STAINED_GLASS_PANE.white();
    public static final Block stained_hardened_clay = Blocks.DYED_TERRACOTTA.white();
    public static final Block sticky_piston = Blocks.STICKY_PISTON;
    public static final Block stone = Blocks.STONE;
    public static final Block stone_slab = Blocks.STONE_SLAB;
    public static final Block water = Blocks.WATER;
    public static final Block wooden_pressure_plate = Blocks.OAK_PRESSURE_PLATE;
    public static final Block wooden_slab = Blocks.ACACIA_SLAB;

    private LegacyStructureBlocks() {
    }

    public static BlockState fromLegacy(Block block, int metadata) {
        if (isRemovedRedstoneComponent(block)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (block instanceof BlockForcestone forcestone) {
            return forcestone.getStateFromLegacyMetadata(metadata);
        }
        if (block instanceof BlockSithStoneCoffin) {
            return block.defaultBlockState()
                    .setValue(
                            HorizontalDirectionalBlock.FACING,
                            legacyCoffinDirection(metadata & 3)
                    )
                    .setValue(
                            BlockSithStoneCoffin.PART,
                            metadata >= 4
                                    ? BlockSithStoneCoffin.Part.UPPER
                                    : BlockSithStoneCoffin.Part.BASE
                    );
        }
        if (block instanceof BlockSithCoffin) {
            return block.defaultBlockState()
                    .setValue(
                            HorizontalDirectionalBlock.FACING,
                            legacyCoffinDirection(metadata & 3)
                    )
                    .setValue(
                            BlockSithCoffin.PART,
                            (metadata & 8) == 0
                                    ? BlockSithCoffin.Part.BASE
                                    : BlockSithCoffin.Part.FRONT
                    );
        }

        BlockState state = coloredState(block, metadata);
        if (state != null) {
            return state;
        }
        if (block instanceof StairBlock) {
            return block.defaultBlockState()
                    .setValue(StairBlock.FACING, horizontalDirection(metadata & 3))
                    .setValue(StairBlock.HALF, (metadata & 4) == 0 ? Half.BOTTOM : Half.TOP);
        }
        if (block instanceof BlockModSlab slab) {
            return slab.defaultBlockState()
                    .setValue(BlockModSlab.VARIANT, (metadata & 7) == 1
                            ? BlockModSlab.Variant.DARK
                            : BlockModSlab.Variant.LIGHT)
                    .setValue(SlabBlock.TYPE, (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP);
        }
        if (block instanceof SlabBlock) {
            return block.defaultBlockState().setValue(
                    SlabBlock.TYPE,
                    (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP
            );
        }
        if (block == sticky_piston) {
            return block.defaultBlockState().setValue(PistonBaseBlock.FACING, direction(metadata & 7));
        }
        if (block == redstone_torch && metadata > 0 && metadata < 5) {
            return Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(
                    WallTorchBlock.FACING,
                    switch (metadata) {
                        case 1 -> Direction.EAST;
                        case 2 -> Direction.WEST;
                        case 3 -> Direction.SOUTH;
                        default -> Direction.NORTH;
                    }
            );
        }
        if (block instanceof RotatedPillarBlock && (metadata & 12) != 0) {
            Direction.Axis axis = (metadata & 12) == 4 ? Direction.Axis.X : Direction.Axis.Z;
            return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }
        return block.defaultBlockState();
    }

    private static BlockState coloredState(Block block, int metadata) {
        if (block == stained_glass) {
            return stainedGlass(metadata).defaultBlockState();
        }
        if (block == stained_glass_pane) {
            return stainedGlassPane(metadata).defaultBlockState();
        }
        if (block == stained_hardened_clay) {
            return terracotta(metadata).defaultBlockState();
        }
        return null;
    }

    private static Block stainedGlass(int metadata) {
        return switch (metadata & 15) {
            case 3 -> Blocks.STAINED_GLASS.lightBlue();
            case 14 -> Blocks.STAINED_GLASS.red();
            default -> Blocks.STAINED_GLASS.white();
        };
    }

    private static Block stainedGlassPane(int metadata) {
        return switch (metadata & 15) {
            case 3 -> Blocks.STAINED_GLASS_PANE.lightBlue();
            case 14 -> Blocks.STAINED_GLASS_PANE.red();
            default -> Blocks.STAINED_GLASS_PANE.white();
        };
    }

    private static Block terracotta(int metadata) {
        return switch (metadata & 15) {
            case 3 -> Blocks.DYED_TERRACOTTA.lightBlue();
            case 14 -> Blocks.DYED_TERRACOTTA.red();
            default -> Blocks.DYED_TERRACOTTA.white();
        };
    }

    private static Direction horizontalDirection(int metadata) {
        return switch (metadata) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
    }

    private static Direction direction(int metadata) {
        return switch (metadata) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            default -> Direction.EAST;
        };
    }

    private static Direction legacyCoffinDirection(int metadata) {
        return switch (metadata) {
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.EAST;
            default -> Direction.SOUTH;
        };
    }

    private static boolean isRemovedRedstoneComponent(Block block) {
        return block == redstone_block
                || block == redstone_torch
                || block == redstone_wire
                || block == sticky_piston
                || block == wooden_pressure_plate;
    }
}
