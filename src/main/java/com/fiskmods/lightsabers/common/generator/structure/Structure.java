package com.fiskmods.lightsabers.common.generator.structure;

import com.fiskmods.lightsabers.common.generator.ModChestGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Structure {
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
        if (existing == state || existing.getDestroySpeed(worldObj, pos) < 0.0F) {
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
        worldObj.setBlock(pos, state, flags);
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
        if (worldObj.getBlockState(pos).is(Blocks.CHEST)) {
            return false;
        }
        worldObj.setBlock(
                pos,
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing),
                Block.UPDATE_CLIENTS
        );
        if (worldObj.getBlockEntity(pos) instanceof Container container) {
            ModChestGen.fill(container, category, random);
            container.setChanged();
        }
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
        if (worldObj.getBlockEntity(pos) instanceof Container container) {
            ModChestGen.fill(container, category, random);
            container.setChanged();
        }
        return true;
    }
}
