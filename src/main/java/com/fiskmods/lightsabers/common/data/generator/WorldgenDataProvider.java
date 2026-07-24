package com.fiskmods.lightsabers.common.data.generator;

import com.fiskmods.lightsabers.Lightsabers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class WorldgenDataProvider implements DataProvider {
    private static final int SITH_TOMB_SPACING = 32;
    private static final int SITH_TOMB_SEPARATION = 8;
    private static final int JEDI_TEMPLE_SPACING = 64;
    private static final int JEDI_TEMPLE_SEPARATION = 24;
    private static final int CRYSTAL_CAVE_SPACING = 64;
    private static final int CRYSTAL_CAVE_SEPARATION = 48;
    private static final int SITH_TOMB_SALT = 235785655;
    private static final int JEDI_TEMPLE_SALT = 235785656;
    private static final int CRYSTAL_CAVE_SALT = 235785657;
    private static final int CRYSTAL_CAVE_MINIMUM_AIR_BLOCKS = 1024;
    private static final int CRYSTAL_CAVE_ENTRANCE_LENGTH = 32;
    private static final int CRYSTAL_CAVE_CRYSTAL_ATTEMPTS = 100;
    private static final int CRYSTAL_CAVE_CRYSTAL_MAX_Y = 64;

    private final Path dataOutput;

    public WorldgenDataProvider(PackOutput output) {
        dataOutput = output.getOutputFolder(PackOutput.Target.DATA_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(
                saveStructure(
                        output,
                        "jedi_temple",
                        "jedi_temple",
                        "surface_structures",
                        "beard_thin"
                ),
                saveStructure(
                        output,
                        "sith_tomb",
                        "sith_tomb",
                        "surface_structures",
                        "bury"
                ),
                saveStructure(
                        output,
                        "crystal_cave",
                        "crystal_cave",
                        "underground_structures",
                        "none"
                ),
                saveStructureSet(
                        output,
                        "jedi_temple",
                        JEDI_TEMPLE_SPACING,
                        JEDI_TEMPLE_SEPARATION,
                        JEDI_TEMPLE_SALT
                ),
                saveStructureSet(
                        output,
                        "sith_tomb",
                        SITH_TOMB_SPACING,
                        SITH_TOMB_SEPARATION,
                        SITH_TOMB_SALT
                ),
                saveStructureSet(
                        output,
                        "crystal_cave",
                        CRYSTAL_CAVE_SPACING,
                        CRYSTAL_CAVE_SEPARATION,
                        CRYSTAL_CAVE_SALT
                ),
                saveBiomeTag(output, "jedi_temple", "#minecraft:is_overworld"),
                saveBiomeTag(output, "crystal_cave", "#minecraft:is_overworld"),
                saveBiomeTag(
                        output,
                        "sith_tomb",
                        "minecraft:desert",
                        "minecraft:badlands",
                        "minecraft:eroded_badlands",
                        "minecraft:wooded_badlands"
                ),
                saveCrystalCaveConfiguredFeature(output),
                saveCrystalCavePlacedFeature(output),
                saveCrystalCaveBiomeModifier(output)
        );
    }

    private CompletableFuture<?> saveCrystalCaveConfiguredFeature(CachedOutput output) {
        JsonObject config = new JsonObject();
        config.addProperty("minimum_air_blocks", CRYSTAL_CAVE_MINIMUM_AIR_BLOCKS);
        config.addProperty("entrance_length", CRYSTAL_CAVE_ENTRANCE_LENGTH);
        config.addProperty("crystal_attempts", CRYSTAL_CAVE_CRYSTAL_ATTEMPTS);
        config.addProperty("crystal_max_y", CRYSTAL_CAVE_CRYSTAL_MAX_Y);

        JsonObject json = new JsonObject();
        json.addProperty("type", Lightsabers.MODID + ":crystal_cave");
        json.add("config", config);
        return DataProvider.saveStable(
                output,
                json,
                dataPath("worldgen/configured_feature/crystal_cave.json")
        );
    }

    private CompletableFuture<?> saveCrystalCavePlacedFeature(CachedOutput output) {
        JsonObject biomePlacement = new JsonObject();
        biomePlacement.addProperty("type", "minecraft:biome");
        JsonArray placement = new JsonArray();
        placement.add(biomePlacement);

        JsonObject json = new JsonObject();
        json.addProperty("feature", Lightsabers.MODID + ":crystal_cave");
        json.add("placement", placement);
        return DataProvider.saveStable(
                output,
                json,
                dataPath("worldgen/placed_feature/crystal_cave.json")
        );
    }

    private CompletableFuture<?> saveCrystalCaveBiomeModifier(CachedOutput output) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:add_features");
        json.addProperty("biomes", "#minecraft:is_overworld");
        json.addProperty("features", Lightsabers.MODID + ":crystal_cave");
        json.addProperty("step", "underground_decoration");
        return DataProvider.saveStable(
                output,
                json,
                dataPath("neoforge/biome_modifier/crystal_cave.json")
        );
    }

    private CompletableFuture<?> saveStructure(
            CachedOutput output,
            String name,
            String structure,
            String step,
            String terrainAdaptation
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", Lightsabers.MODID + ":legacy");
        json.addProperty(
                "biomes",
                "#" + Lightsabers.MODID + ":has_structure/" + name
        );
        json.addProperty("step", step);
        json.add("spawn_overrides", new JsonObject());
        json.addProperty("terrain_adaptation", terrainAdaptation);
        json.addProperty("structure", structure);
        return DataProvider.saveStable(
                output,
                json,
                dataPath("worldgen/structure/" + name + ".json")
        );
    }

    private CompletableFuture<?> saveStructureSet(
            CachedOutput output,
            String name,
            int spacing,
            int separation,
            int salt
    ) {
        JsonObject entry = new JsonObject();
        entry.addProperty("structure", Lightsabers.MODID + ":" + name);
        entry.addProperty("weight", 1);
        JsonArray structures = new JsonArray();
        structures.add(entry);

        JsonObject placement = new JsonObject();
        placement.addProperty("type", "minecraft:random_spread");
        placement.addProperty("spacing", spacing);
        placement.addProperty("separation", separation);
        placement.addProperty("salt", salt);

        JsonObject json = new JsonObject();
        json.add("structures", structures);
        json.add("placement", placement);
        return DataProvider.saveStable(
                output,
                json,
                dataPath("worldgen/structure_set/" + name + ".json")
        );
    }

    private CompletableFuture<?> saveBiomeTag(
            CachedOutput output,
            String name,
            String... biomes
    ) {
        JsonArray values = new JsonArray();
        for (String biome : biomes) {
            values.add(biome);
        }
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        json.add("values", values);
        return DataProvider.saveStable(
                output,
                json,
                dataPath("tags/worldgen/biome/has_structure/" + name + ".json")
        );
    }

    private Path dataPath(String relativePath) {
        return dataOutput.resolve(Lightsabers.MODID).resolve(relativePath);
    }

    @Override
    public String getName() {
        return "Advanced Lightsabers world generation";
    }
}
