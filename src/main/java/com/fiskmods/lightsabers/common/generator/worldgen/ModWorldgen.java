package com.fiskmods.lightsabers.common.generator.worldgen;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.generator.structure.EnumStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {
    public static final ResourceKey<Structure> JEDI_TEMPLE = structureKey("jedi_temple");
    public static final ResourceKey<Structure> SITH_TOMB = structureKey("sith_tomb");
    public static final ResourceKey<Structure> CRYSTAL_CAVE = structureKey("crystal_cave");

    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Lightsabers.MODID);
    private static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Lightsabers.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Lightsabers.MODID);

    public static final Supplier<StructureType<LegacyDataStructure>> LEGACY_STRUCTURE =
            STRUCTURE_TYPES.register("legacy", () -> () -> LegacyDataStructure.CODEC);
    public static final Supplier<StructurePieceType> LEGACY_PIECE =
            STRUCTURE_PIECE_TYPES.register(
                    "legacy",
                    () -> (StructurePieceType.ContextlessType) LegacyStructurePiece::new
            );
    public static final Supplier<Feature<CrystalCaveConfiguration>> CRYSTAL_CAVE_FEATURE =
            FEATURES.register("crystal_cave", CrystalCaveFeature::new);

    private ModWorldgen() {
    }

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECE_TYPES.register(modEventBus);
        FEATURES.register(modEventBus);
    }

    public static ResourceKey<Structure> key(EnumStructure structure) {
        return switch (structure) {
            case JEDI_TEMPLE -> JEDI_TEMPLE;
            case SITH_TOMB -> SITH_TOMB;
            case CRYSTAL_CAVE -> CRYSTAL_CAVE;
        };
    }

    private static ResourceKey<Structure> structureKey(String name) {
        return ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, name)
        );
    }
}
