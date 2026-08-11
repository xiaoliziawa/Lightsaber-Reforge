package com.fiskmods.lightsabers.common.generator.worldgen;

import java.util.Random;

import com.fiskmods.lightsabers.common.block.BlockCrystal;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.tileentity.TileEntityCrystal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class CrystalCaveFeature extends Feature<CrystalCaveConfiguration> {
    private static final int CHUNK_SIZE = 16;
    private static final int SAFE_CHUNK_RADIUS = 1;
    private static final int SURFACE_CLEARANCE = 10;
    private static final int CRYSTAL_CHUNK_RADIUS = 1;
    private static final int ENTRANCE_MIN_VERTICAL_STEPS = 10;
    private static final int ENTRANCE_VERTICAL_STEP_VARIATION = 10;
    private static final int ENTRANCE_HORIZONTAL_SHIFT_CHANCE = 3;
    private static final int ENTRANCE_VERTICAL_STEP_CHANCE = 9;
    private static final int ENTRANCE_HORIZONTAL_SHIFT = 2;
    private static final int MAX_ENTRANCE_PATH_FACTOR = 16;
    private static final int ENTRANCE_CENTER_RANGE = 3;
    private static final int ENTRANCE_CENTER_OFFSET = 2;
    private static final double ENTRANCE_DIAMETER_DIVISOR = 8.0D;
    private static final int CHAMBER_HORIZONTAL_RADIUS = 9;
    private static final int CHAMBER_VERTICAL_RADIUS = 6;
    private static final int CHAMBER_SURFACE_CLEARANCE = 24;
    private static final int CHAMBER_MIN_BOTTOM_CLEARANCE = 16;
    private static final int CHAMBER_CRYSTAL_ATTEMPTS = 160;
    private static final Direction[] CRYSTAL_FACINGS = {
            Direction.EAST,
            Direction.WEST,
            Direction.SOUTH,
            Direction.NORTH,
            Direction.UP,
            Direction.DOWN
    };

    public CrystalCaveFeature() {
        super(CrystalCaveConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalCaveConfiguration> context) {
        CrystalCaveConfiguration config = context.config();
        if (context.chunkGenerator() instanceof FlatLevelSource) {
            return false;
        }

        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(
                context.origin().getX() >> 4,
                context.origin().getZ() >> 4
        );
        if (!hasCrystalCaveStart(level, chunk)) {
            return false;
        }

        BlockPos biomePos = new BlockPos(
                chunk.getMiddleBlockX(),
                context.chunkGenerator().getSeaLevel(),
                chunk.getMiddleBlockZ()
        );
        if (level.getBiome(biomePos).is(BiomeTags.IS_OCEAN)) {
            return false;
        }

        Random random = new Random(context.random().nextLong());
        BlockPos cavePos = findCavePosition(level, chunk, random, config.minimumAirBlocks());
        if (cavePos == null) {
            cavePos = createFallbackCavePosition(level, chunk, random, config.crystalMaxY());
        }

        GenerationBounds bounds = GenerationBounds.around(chunk, level);
        carveChamber(level, cavePos, bounds);
        carveEntrance(level, cavePos, random, config.entranceLength(), bounds);
        generateCrystals(level, chunk, random, config, bounds);
        generateChamberCrystals(level, cavePos, random, bounds);
        return true;
    }

    private static boolean hasCrystalCaveStart(WorldGenLevel level, ChunkPos chunk) {
        Registry<Structure> structures = level.registryAccess().lookupOrThrow(
                Registries.STRUCTURE
        );
        Structure crystalCave = structures.getValue(ModWorldgen.CRYSTAL_CAVE);
        if (crystalCave == null) {
            return false;
        }

        StructureStart start = level.getChunk(
                chunk.x(),
                chunk.z(),
                ChunkStatus.STRUCTURE_STARTS
        ).getStartForStructure(crystalCave);
        return start != null && start.isValid();
    }

    private static BlockPos findCavePosition(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random,
            int minimumAirBlocks
    ) {
        int airBlocks = countCaveAir(level, chunk);
        if (airBlocks <= minimumAirBlocks) {
            return null;
        }

        int targetIndex = random.nextInt(airBlocks);
        int currentIndex = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            int x = chunk.getMinBlockX() + localX;
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                int z = chunk.getMinBlockZ() + localZ;
                int maxY = getCaveScanMaxY(level, x, z);
                for (int y = level.getMinY(); y < maxY; y++) {
                    pos.set(x, y, z);
                    if (level.isEmptyBlock(pos) && currentIndex++ == targetIndex) {
                        return pos.immutable();
                    }
                }
            }
        }

        return null;
    }

    private static BlockPos createFallbackCavePosition(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random,
            int crystalMaxY
    ) {
        int x = chunk.getMinBlockX() + random.nextInt(CHUNK_SIZE);
        int z = chunk.getMinBlockZ() + random.nextInt(CHUNK_SIZE);
        int minY = level.getMinY() + CHAMBER_MIN_BOTTOM_CLEARANCE;
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int maxY = Math.min(
                crystalMaxY - CHAMBER_VERTICAL_RADIUS,
                surfaceY - CHAMBER_SURFACE_CLEARANCE
        );
        int y = maxY > minY ? minY + random.nextInt(maxY - minY + 1) : minY;
        return new BlockPos(x, y, z);
    }

    private static int countCaveAir(WorldGenLevel level, ChunkPos chunk) {
        int airBlocks = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < CHUNK_SIZE; localX++) {
            int x = chunk.getMinBlockX() + localX;
            for (int localZ = 0; localZ < CHUNK_SIZE; localZ++) {
                int z = chunk.getMinBlockZ() + localZ;
                int maxY = getCaveScanMaxY(level, x, z);
                for (int y = level.getMinY(); y < maxY; y++) {
                    pos.set(x, y, z);
                    if (level.isEmptyBlock(pos)) {
                        airBlocks++;
                    }
                }
            }
        }

        return airBlocks;
    }

    private static int getCaveScanMaxY(WorldGenLevel level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        return Math.min(surfaceY - SURFACE_CLEARANCE, level.getMaxY() + 1);
    }

    private static void carveChamber(
            WorldGenLevel level,
            BlockPos center,
            GenerationBounds bounds
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int offsetX = -CHAMBER_HORIZONTAL_RADIUS;
             offsetX <= CHAMBER_HORIZONTAL_RADIUS;
             offsetX++) {
            double normalizedX = offsetX / (double) CHAMBER_HORIZONTAL_RADIUS;
            for (int offsetY = -CHAMBER_VERTICAL_RADIUS;
                 offsetY <= CHAMBER_VERTICAL_RADIUS;
                 offsetY++) {
                double normalizedY = offsetY / (double) CHAMBER_VERTICAL_RADIUS;
                for (int offsetZ = -CHAMBER_HORIZONTAL_RADIUS;
                     offsetZ <= CHAMBER_HORIZONTAL_RADIUS;
                     offsetZ++) {
                    double normalizedZ = offsetZ / (double) CHAMBER_HORIZONTAL_RADIUS;
                    if (normalizedX * normalizedX
                            + normalizedY * normalizedY
                            + normalizedZ * normalizedZ > 1.0D) {
                        continue;
                    }

                    pos.setWithOffset(center, offsetX, offsetY, offsetZ);
                    if (!bounds.contains(pos)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(pos);
                    if (!state.hasBlockEntity() && !state.is(Blocks.BEDROCK)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static void carveEntrance(
            WorldGenLevel level,
            BlockPos cavePos,
            Random random,
            int entranceLength,
            GenerationBounds bounds
    ) {
        int x = cavePos.getX();
        int y = cavePos.getY();
        int z = cavePos.getZ();
        int straightSteps = ENTRANCE_MIN_VERTICAL_STEPS
                + random.nextInt(ENTRANCE_VERTICAL_STEP_VARIATION);
        int horizontalMargin = entranceLength / 8 + 2;
        int maxSteps = level.getHeight() * MAX_ENTRANCE_PATH_FACTOR;

        for (int step = 0; step < maxSteps; step++) {
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            if (y >= surfaceY) {
                return;
            }

            if (step < straightSteps) {
                y++;
            } else {
                if (random.nextInt(ENTRANCE_HORIZONTAL_SHIFT_CHANCE) == 0) {
                    int nextX = x + (random.nextInt(3) - 1) * ENTRANCE_HORIZONTAL_SHIFT;
                    if (bounds.containsHorizontal(nextX, z, horizontalMargin)) {
                        x = nextX;
                    }
                }
                if (random.nextInt(ENTRANCE_VERTICAL_STEP_CHANCE) == 0) {
                    y++;
                }
                if (random.nextInt(ENTRANCE_HORIZONTAL_SHIFT_CHANCE) == 0) {
                    int nextZ = z + (random.nextInt(3) - 1) * ENTRANCE_HORIZONTAL_SHIFT;
                    if (bounds.containsHorizontal(x, nextZ, horizontalMargin)) {
                        z = nextZ;
                    }
                }
            }

            carveEntranceBlob(level, random, x, y, z, entranceLength, bounds);
        }
    }

    private static void carveEntranceBlob(
            WorldGenLevel level,
            Random random,
            int x,
            int y,
            int z,
            int entranceLength,
            GenerationBounds bounds
    ) {
        double centerX = x + random.nextInt(ENTRANCE_CENTER_RANGE) - ENTRANCE_CENTER_OFFSET;
        double centerY = y + random.nextInt(ENTRANCE_CENTER_RANGE) - ENTRANCE_CENTER_OFFSET;
        double centerZ = z + random.nextInt(ENTRANCE_CENTER_RANGE) - ENTRANCE_CENTER_OFFSET;
        double horizontalDiameter = random.nextDouble()
                * entranceLength / ENTRANCE_DIAMETER_DIVISOR + 1.0D;
        double verticalDiameter = horizontalDiameter;
        int minX = Mth.floor(centerX - horizontalDiameter / 2.0D);
        int minY = Mth.floor(centerY - verticalDiameter / 2.0D);
        int minZ = Mth.floor(centerZ - horizontalDiameter / 2.0D);
        int maxX = Mth.floor(centerX + horizontalDiameter / 2.0D);
        int maxY = Mth.floor(centerY + verticalDiameter / 2.0D);
        int maxZ = Mth.floor(centerZ + horizontalDiameter / 2.0D);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int blockX = minX; blockX <= maxX; blockX++) {
            double normalizedX = (blockX + 0.5D - centerX) / (horizontalDiameter / 2.0D);
            if (normalizedX * normalizedX >= 1.0D) {
                continue;
            }

            for (int blockY = minY; blockY <= maxY; blockY++) {
                double normalizedY = (blockY + 0.5D - centerY) / (verticalDiameter / 2.0D);
                if (normalizedX * normalizedX + normalizedY * normalizedY >= 1.0D) {
                    continue;
                }

                for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                    double normalizedZ = (blockZ + 0.5D - centerZ)
                            / (horizontalDiameter / 2.0D);
                    if (normalizedX * normalizedX
                            + normalizedY * normalizedY
                            + normalizedZ * normalizedZ >= 1.0D) {
                        continue;
                    }

                    pos.set(blockX, blockY, blockZ);
                    carveBlock(level, pos, bounds);
                }
            }
        }
    }

    private static void carveBlock(
            WorldGenLevel level,
            BlockPos.MutableBlockPos pos,
            GenerationBounds bounds
    ) {
        if (!bounds.contains(pos) || !hasNonAirNeighbor(level, pos, bounds)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (state.hasBlockEntity() || state.is(Blocks.BEDROCK)) {
            return;
        }

        if (!state.isAir()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        for (Direction direction : Direction.values()) {
            normalizeWall(level, pos.relative(direction), bounds);
        }
    }

    private static boolean hasNonAirNeighbor(
            WorldGenLevel level,
            BlockPos pos,
            GenerationBounds bounds
    ) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (bounds.contains(neighborPos) && !level.isEmptyBlock(neighborPos)) {
                return true;
            }
        }
        return false;
    }

    private static void normalizeWall(
            WorldGenLevel level,
            BlockPos pos,
            GenerationBounds bounds
    ) {
        if (!bounds.contains(pos)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (!state.hasBlockEntity()
                && !state.is(Blocks.BEDROCK)
                && state.isFaceSturdy(level, pos, Direction.UP)) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static void generateCrystals(
            WorldGenLevel level,
            ChunkPos centerChunk,
            Random random,
            CrystalCaveConfiguration config,
            GenerationBounds bounds
    ) {
        int minY = level.getMinY();
        int maxY = Math.min(config.crystalMaxY(), level.getMaxY() + 1);
        if (maxY <= minY) {
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int chunkOffsetX = -CRYSTAL_CHUNK_RADIUS;
             chunkOffsetX <= CRYSTAL_CHUNK_RADIUS;
             chunkOffsetX++) {
            int chunkMinX = centerChunk.getMinBlockX() + chunkOffsetX * CHUNK_SIZE;
            for (int chunkOffsetZ = -CRYSTAL_CHUNK_RADIUS;
                 chunkOffsetZ <= CRYSTAL_CHUNK_RADIUS;
                 chunkOffsetZ++) {
                int chunkMinZ = centerChunk.getMinBlockZ() + chunkOffsetZ * CHUNK_SIZE;
                for (int attempt = 0; attempt < config.crystalAttempts(); attempt++) {
                    pos.set(
                            chunkMinX + random.nextInt(CHUNK_SIZE),
                            minY + random.nextInt(maxY - minY),
                            chunkMinZ + random.nextInt(CHUNK_SIZE)
                    );
                    placeCrystal(level, pos, random, bounds);
                }
            }
        }
    }

    private static void generateChamberCrystals(
            WorldGenLevel level,
            BlockPos center,
            Random random,
            GenerationBounds bounds
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < CHAMBER_CRYSTAL_ATTEMPTS; attempt++) {
            pos.set(
                    center.getX() + random.nextInt(CHAMBER_HORIZONTAL_RADIUS * 2 + 1)
                            - CHAMBER_HORIZONTAL_RADIUS,
                    center.getY() + random.nextInt(CHAMBER_VERTICAL_RADIUS * 2 + 1)
                            - CHAMBER_VERTICAL_RADIUS,
                    center.getZ() + random.nextInt(CHAMBER_HORIZONTAL_RADIUS * 2 + 1)
                            - CHAMBER_HORIZONTAL_RADIUS
            );
            placeCrystal(level, pos, random, bounds);
        }
    }

    private static void placeCrystal(
            WorldGenLevel level,
            BlockPos.MutableBlockPos pos,
            Random random,
            GenerationBounds bounds
    ) {
        if (!bounds.contains(pos) || !level.isEmptyBlock(pos)) {
            return;
        }

        Direction facing = findCrystalFacing(level, pos, bounds);
        if (facing == null) {
            return;
        }

        BlockState crystalState = ModBlocks.LIGHTSABER_CRYSTAL.get()
                .defaultBlockState()
                .setValue(BlockCrystal.FACING, facing);
        BlockPos crystalPos = pos.immutable();
        if (level.setBlock(crystalPos, crystalState, Block.UPDATE_CLIENTS)
                && level.getBlockEntity(crystalPos) instanceof TileEntityCrystal crystal) {
            crystal.setColor(ItemCrystal.getRandomGen(random));
        }
    }

    private static Direction findCrystalFacing(
            WorldGenLevel level,
            BlockPos pos,
            GenerationBounds bounds
    ) {
        for (Direction facing : CRYSTAL_FACINGS) {
            BlockPos supportPos = pos.relative(facing.getOpposite());
            if (bounds.contains(supportPos)
                    && Block.canSupportCenter(level, supportPos, facing)) {
                return facing;
            }
        }
        return null;
    }

    private record GenerationBounds(
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        private static GenerationBounds around(ChunkPos chunk, WorldGenLevel level) {
            return new GenerationBounds(
                    chunk.getMinBlockX() - CHUNK_SIZE * SAFE_CHUNK_RADIUS,
                    level.getMinY(),
                    chunk.getMinBlockZ() - CHUNK_SIZE * SAFE_CHUNK_RADIUS,
                    chunk.getMaxBlockX() + CHUNK_SIZE * SAFE_CHUNK_RADIUS,
                    level.getMaxY(),
                    chunk.getMaxBlockZ() + CHUNK_SIZE * SAFE_CHUNK_RADIUS
            );
        }

        private boolean contains(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX
                    && pos.getY() >= minY && pos.getY() <= maxY
                    && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }

        private boolean containsHorizontal(int x, int z, int margin) {
            return x >= minX + margin && x <= maxX - margin
                    && z >= minZ + margin && z <= maxZ - margin;
        }
    }
}
