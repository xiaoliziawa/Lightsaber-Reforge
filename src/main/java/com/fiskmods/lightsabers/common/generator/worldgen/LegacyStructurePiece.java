package com.fiskmods.lightsabers.common.generator.worldgen;

import com.fiskmods.lightsabers.common.generator.structure.EnumStructure;
import com.fiskmods.lightsabers.common.generator.structure.Structure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Random;

public final class LegacyStructurePiece extends StructurePiece {
    private static final String STRUCTURE_TAG = "Structure";
    private static final String ORIGIN_X_TAG = "OriginX";
    private static final String ORIGIN_Y_TAG = "OriginY";
    private static final String ORIGIN_Z_TAG = "OriginZ";
    private static final String SEED_TAG = "Seed";

    private final EnumStructure structure;
    private final BlockPos origin;
    private final long seed;

    public LegacyStructurePiece(EnumStructure structure, BlockPos origin, long seed) {
        super(ModWorldgen.LEGACY_PIECE.get(), 0, createBoundingBox(structure, origin));
        this.structure = structure;
        this.origin = origin;
        this.seed = seed;
    }

    public LegacyStructurePiece(CompoundTag tag) {
        super(ModWorldgen.LEGACY_PIECE.get(), tag);
        structure = EnumStructure.valueOf(tag.getString(STRUCTURE_TAG));
        origin = new BlockPos(
                tag.getInt(ORIGIN_X_TAG),
                tag.getInt(ORIGIN_Y_TAG),
                tag.getInt(ORIGIN_Z_TAG)
        );
        seed = tag.getLong(SEED_TAG);
    }

    @Override
    protected void addAdditionalSaveData(
            StructurePieceSerializationContext context,
            CompoundTag tag
    ) {
        tag.putString(STRUCTURE_TAG, structure.name());
        tag.putInt(ORIGIN_X_TAG, origin.getX());
        tag.putInt(ORIGIN_Y_TAG, origin.getY());
        tag.putInt(ORIGIN_Z_TAG, origin.getZ());
        tag.putLong(SEED_TAG, seed);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBounds,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        Random structureRandom = new Random(seed);
        Structure legacyStructure = structure.construct(
                level,
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                structureRandom
        );
        legacyStructure.setGenerationBounds(chunkBounds);
        legacyStructure.spawnStructure(structureRandom);
    }

    private static BoundingBox createBoundingBox(EnumStructure structure, BlockPos origin) {
        return switch (structure) {
            case CRYSTAL_CAVE -> new BoundingBox(
                    origin.getX() - 8,
                    origin.getY() - 512,
                    origin.getZ() - 8,
                    origin.getX() + 7,
                    origin.getY() + 64,
                    origin.getZ() + 7
            );
            case JEDI_TEMPLE -> new BoundingBox(
                    origin.getX() - 32,
                    origin.getY() - 48,
                    origin.getZ() - 1,
                    origin.getX() + 32,
                    origin.getY() + 112,
                    origin.getZ() + 72
            );
            case SITH_TOMB -> new BoundingBox(
                    origin.getX() - 32,
                    origin.getY() - 160,
                    origin.getZ(),
                    origin.getX() + 12,
                    origin.getY() + 48,
                    origin.getZ() + 160
            );
        };
    }
}
