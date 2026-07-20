package com.fiskmods.lightsabers.common.generator.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public enum EnumStructure implements StringRepresentable {
    CRYSTAL_CAVE(
            (level, x, y, z, random) -> new StructureCrystalCave(level, x, y, z),
            48,
            64,
            biome -> !biome.is(BiomeTags.IS_OCEAN)
    ),
    SITH_TOMB(
            (level, x, y, z, random) -> new StructureSithTomb(level, x, y, z),
            8,
            32,
            Biomes.DESERT,
            Biomes.BADLANDS,
            Biomes.ERODED_BADLANDS,
            Biomes.WOODED_BADLANDS
    ),
    JEDI_TEMPLE(
            (level, x, y, z, random) -> new StructureJediTemple(0, level, x, y, z),
            24,
            64,
            EnumStructure::canGenerateJediTemple
    );

    public static final Codec<EnumStructure> CODEC = StringRepresentable.fromEnum(
            EnumStructure::values
    );

    public final Predicate<Holder<Biome>> biomePredicate;
    public final int minDistance;
    public final int maxDistance;
    private final Constructor constructor;

    EnumStructure(
            Constructor constructor,
            int minDistance,
            int maxDistance,
            Predicate<Holder<Biome>> biomePredicate
    ) {
        this.constructor = constructor;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.biomePredicate = biomePredicate;
    }

    @SafeVarargs
    EnumStructure(
            Constructor constructor,
            int minDistance,
            int maxDistance,
            ResourceKey<Biome>... biomes
    ) {
        this(constructor, minDistance, maxDistance, biomeSetPredicate(biomes));
    }

    public Structure construct(LevelAccessor level, int x, int y, int z, Random random) {
        return constructor.construct(level, x, y, z, random);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    private static Predicate<Holder<Biome>> biomeSetPredicate(ResourceKey<Biome>[] biomes) {
        Set<ResourceKey<Biome>> allowed = new HashSet<>(Arrays.asList(biomes));
        return biome -> biome.unwrapKey().map(allowed::contains).orElse(false);
    }

    private static boolean canGenerateJediTemple(Holder<Biome> biome) {
        Biome value = biome.value();
        return value.getBaseTemperature() < 1.5F
                && value.getModifiedClimateSettings().downfall() > 0.0F;
    }

    @FunctionalInterface
    private interface Constructor {
        Structure construct(LevelAccessor level, int x, int y, int z, Random random);
    }
}
