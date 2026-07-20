package com.fiskmods.lightsabers.common.generator.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record CrystalCaveConfiguration(
        int minimumAirBlocks,
        int entranceLength,
        int crystalAttempts,
        int crystalMaxY
) implements FeatureConfiguration {
    public static final Codec<CrystalCaveConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, Integer.MAX_VALUE)
                            .fieldOf("minimum_air_blocks")
                            .forGetter(CrystalCaveConfiguration::minimumAirBlocks),
                    Codec.intRange(1, 128)
                            .fieldOf("entrance_length")
                            .forGetter(CrystalCaveConfiguration::entranceLength),
                    Codec.intRange(0, 10000)
                            .fieldOf("crystal_attempts")
                            .forGetter(CrystalCaveConfiguration::crystalAttempts),
                    Codec.INT
                            .fieldOf("crystal_max_y")
                            .forGetter(CrystalCaveConfiguration::crystalMaxY)
            ).apply(instance, CrystalCaveConfiguration::new)
    );
}
