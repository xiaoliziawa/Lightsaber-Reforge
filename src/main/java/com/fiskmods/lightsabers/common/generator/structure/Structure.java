package com.fiskmods.lightsabers.common.generator.structure;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.generator.ModChestGen;
import com.fiskmods.lightsabers.common.integration.lootr.LootrIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Structure {
    private static final long CONTAINER_SEED_SALT = 0x6A09E667F3BCC909L;
    private static final long MIX_MULTIPLIER_1 = 0xBF58476D1CE4E5B9L;
    private static final long MIX_MULTIPLIER_2 = 0x94D049BB133111EBL;

    protected final LevelAccessor worldObj;
    protected int xCoord;
    protected int yCoord;
    protected int zCoord;
    protected boolean mirrorX;
    protected boolean mirrorZ;
    protected int maxY;
    protected boolean simulate;
    protected final List<StructurePoint> coverage = new ArrayList<>();
    private final Map<StructurePoint, StructurePoint> coverageByColumn = new HashMap<>();
    @Nullable
    private BoundingBox generationBounds;
    private long generationSeed;

    protected Structure(LevelAccessor level, int x, int y, int z) {
        worldObj = level;
        xCoord = x;
        yCoord = y;
        zCoord = z;
    }

    public abstract void spawnStructure(Random random);

    public final void setGenerationBounds(BoundingBox generationBounds) {
        this.generationBounds = generationBounds;
    }

    public final void setGenerationSeed(long generationSeed) {
        this.generationSeed = generationSeed;
    }

    private Random createContainerRandom(BlockPos pos, String category) {
        long seed = mixSeed(generationSeed ^ CONTAINER_SEED_SALT, pos.asLong());
        seed = mixSeed(seed, category.hashCode());
        return new Random(seed);
    }

    private static long mixSeed(long seed, long value) {
        long mixed = seed + value;
        mixed = (mixed ^ (mixed >>> 30)) * MIX_MULTIPLIER_1;
        mixed = (mixed ^ (mixed >>> 27)) * MIX_MULTIPLIER_2;
        return mixed ^ (mixed >>> 31);
    }

    private void fillStructureContainer(BlockPos pos, String category) {
        if (worldObj.getBlockEntity(pos) instanceof Container container) {
            ModChestGen.fill(
                    container,
                    category,
                    createContainerRandom(pos, category),
                    worldObj.registryAccess()
            );
            container.setChanged();
        }
    }

    protected final boolean isWithinGenerationBounds(BlockPos pos) {
        return generationBounds == null || generationBounds.isInside(pos);
    }

    private boolean isWithinGenerationBounds(int x, int y, int z) {
        return generationBounds == null || generationBounds.isInside(x, y, z);
    }

    protected final boolean isWithinHorizontalGenerationBounds(int x, int z) {
        return generationBounds == null
                || x >= generationBounds.minX() && x <= generationBounds.maxX()
                && z >= generationBounds.minZ() && z <= generationBounds.maxZ();
    }

    public void setBlock(Block block, int metadata, int x, int y, int z) {
        if (mirrorX && x > 0) {
            setBlock(
                    xCoord - x,
                    yCoord + y,
                    zCoord + z,
                    block,
                    StructureHelper.mirrorMetadata(block, metadata)
            );
        }
        if (mirrorZ && z > 0) {
            setBlock(
                    xCoord + x,
                    yCoord + y,
                    zCoord - z,
                    block,
                    StructureHelper.mirrorMetadata(block, metadata)
            );
        }
        setBlock(xCoord + x, yCoord + y, zCoord + z, block, metadata);
    }

    private void setBlock(int x, int y, int z, Block block, int metadata) {
        if (simulate) {
            recordCoverage(x, y, z);
            return;
        }
        if (!isWithinGenerationBounds(x, y, z)) {
            return;
        }

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = LegacyStructureBlocks.fromLegacy(block, metadata);
        BlockState existing = worldObj.getBlockState(pos);
        if (existing.getDestroySpeed(worldObj, pos) < 0.0F
                || (existing == state && !(block instanceof IronBarsBlock))) {
            return;
        }
        placeBlock(x, y, z, state, Block.UPDATE_CLIENTS);
    }

    private void recordCoverage(int x, int y, int z) {
        maxY = Math.max(maxY, y);
        StructurePoint point = new StructurePoint(x, y, z);
        StructurePoint existing = coverageByColumn.putIfAbsent(point, point);
        if (existing != null) {
            existing.posY = Math.min(existing.posY, y);
        } else {
            coverage.add(point);
        }
    }

    public void placeBlock(int x, int y, int z, Block block, int metadata, int flags) {
        placeBlock(x, y, z, LegacyStructureBlocks.fromLegacy(block, metadata), flags);
    }

    public void placeBlock(int x, int y, int z, BlockState state, int flags) {
        if (!isWithinGenerationBounds(x, y, z)) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        BlockState connectedState = updateConnections(pos, state);
        worldObj.setBlock(pos, connectedState, flags);
        updateAdjacentConnections(pos, connectedState);
    }

    private BlockState updateConnections(BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof IronBarsBlock)) {
            return state;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            state = state.updateShape(
                    worldObj,
                    worldObj,
                    pos,
                    direction,
                    neighborPos,
                    worldObj.getBlockState(neighborPos),
                    worldObj.getRandom()
            );
        }
        return state;
    }

    private void updateAdjacentConnections(BlockPos pos, BlockState state) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = worldObj.getBlockState(neighborPos);
            if (!(neighborState.getBlock() instanceof IronBarsBlock)) {
                continue;
            }
            BlockState connectedState = neighborState.updateShape(
                    worldObj,
                    worldObj,
                    neighborPos,
                    direction.getOpposite(),
                    pos,
                    state,
                    worldObj.getRandom()
            );
            if (connectedState != neighborState) {
                worldObj.setBlock(neighborPos, connectedState, Block.UPDATE_CLIENTS);
            }
        }
    }

    protected boolean generateStructureChestContents(
            Random random,
            int x,
            int y,
            int z,
            String category
    ) {
        return generateStructureChestContents(
                random,
                x,
                y,
                z,
                category,
                Direction.SOUTH
        );
    }

    protected boolean generateStructureChestContents(
            Random random,
            int x,
            int y,
            int z,
            String category,
            Direction facing
    ) {
        int worldX = xCoord + x;
        int worldY = yCoord + y;
        int worldZ = zCoord + z;
        if (!isWithinGenerationBounds(worldX, worldY, worldZ)) {
            return false;
        }
        BlockPos pos = new BlockPos(worldX, worldY, worldZ);
        if (Lightsabers.isLootrLoaded && ModChestGen.isSithTombCategory(category)) {
            return LootrIntegration.generateSithTombChest(
                    worldObj,
                    pos,
                    category,
                    facing,
                    createContainerRandom(pos, category)
            );
        }
        if (worldObj.getBlockState(pos).is(Blocks.CHEST)) {
            return false;
        }
        worldObj.setBlock(
                pos,
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing),
                Block.UPDATE_CLIENTS
        );
        fillStructureContainer(pos, category);
        return true;
    }

    protected boolean fillStructureInventory(
            Block block,
            Random random,
            int x,
            int y,
            int z,
            String category
    ) {
        int worldX = xCoord + x;
        int worldY = yCoord + y;
        int worldZ = zCoord + z;
        if (!isWithinGenerationBounds(worldX, worldY, worldZ)) {
            return false;
        }
        BlockPos pos = new BlockPos(worldX, worldY, worldZ);
        if (!worldObj.getBlockState(pos).is(block)) {
            return false;
        }
        fillStructureContainer(pos, category);
        return true;
    }
}
