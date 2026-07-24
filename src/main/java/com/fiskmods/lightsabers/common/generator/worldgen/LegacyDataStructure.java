package com.fiskmods.lightsabers.common.generator.worldgen;

import com.fiskmods.lightsabers.common.generator.structure.EnumStructure;
import com.fiskmods.lightsabers.common.generator.structure.StructureSithTomb;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public final class LegacyDataStructure extends Structure {
    public static final MapCodec<LegacyDataStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    EnumStructure.CODEC.fieldOf("structure").forGetter(
                            LegacyDataStructure::structure
                    )
            ).apply(instance, LegacyDataStructure::new)
    );

    private final EnumStructure structure;

    public LegacyDataStructure(StructureSettings settings, EnumStructure structure) {
        super(settings);
        this.structure = structure;
    }

    private EnumStructure structure() {
        return structure;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (structure == EnumStructure.CRYSTAL_CAVE
                && context.chunkGenerator() instanceof FlatLevelSource) {
            return Optional.empty();
        }

        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int y = context.chunkGenerator().getFirstOccupiedHeight(
                x,
                z,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );
        Holder<Biome> biome = context.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(x),
                QuartPos.fromBlock(y),
                QuartPos.fromBlock(z),
                context.randomState().sampler()
        );
        if (!structure.biomePredicate.test(biome)) {
            return Optional.empty();
        }

        BlockPos origin = new BlockPos(x, y, z);
        int sithTombStairLength = structure == EnumStructure.SITH_TOMB
                ? StructureSithTomb.calculateStairLength(
                        context.chunkGenerator(),
                        context.heightAccessor(),
                        context.randomState(),
                        origin
                )
                : 0;
        long pieceSeed = context.random().nextLong();
        return Optional.of(new GenerationStub(
                origin,
                builder -> builder.addPiece(new LegacyStructurePiece(
                        structure,
                        origin,
                        pieceSeed,
                        sithTombStairLength
                ))
        ));
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.LEGACY_STRUCTURE.get();
    }
}
