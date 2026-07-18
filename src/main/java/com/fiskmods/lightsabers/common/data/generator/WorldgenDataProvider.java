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
    private static final int SITH_TOMB_SALT = 235785655;
    private static final int JEDI_TEMPLE_SALT = 235785656;

    private final Path dataOutput;

    public WorldgenDataProvider(PackOutput output) {
        dataOutput = output.getOutputFolder(PackOutput.Target.DATA_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.allOf(
                saveStructure(output, "jedi_temple", "jedi_temple", "beard_thin"),
                saveStructure(output, "sith_tomb", "sith_tomb", "bury"),
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
                saveBiomeTag(output, "jedi_temple", "#minecraft:is_overworld"),
                saveBiomeTag(
                        output,
                        "sith_tomb",
                        "minecraft:desert",
                        "minecraft:badlands",
                        "minecraft:eroded_badlands",
                        "minecraft:wooded_badlands"
                )
        );
    }

    private CompletableFuture<?> saveStructure(
            CachedOutput output,
            String name,
            String structure,
            String terrainAdaptation
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", Lightsabers.MODID + ":legacy");
        json.addProperty(
                "biomes",
                "#" + Lightsabers.MODID + ":has_structure/" + name
        );
        json.addProperty("step", "surface_structures");
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
