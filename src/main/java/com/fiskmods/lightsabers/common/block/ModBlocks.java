package com.fiskmods.lightsabers.common.block;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.item.ItemForcestone;
import com.fiskmods.lightsabers.common.item.ItemForcestoneSlab;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemCrystalBlock;
import com.fiskmods.lightsabers.common.item.ItemHolocron;
import com.fiskmods.lightsabers.common.item.ItemLightsaberStand;
import com.fiskmods.lightsabers.common.item.ItemLightsaberForge;
import com.fiskmods.lightsabers.common.item.ItemDisassemblyTable;
import com.fiskmods.lightsabers.common.item.ItemSithCoffin;
import com.fiskmods.lightsabers.common.item.ItemSithStoneCoffin;
import com.fiskmods.lightsabers.common.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private static final int FULL_LIGHT_LEVEL = 15;
    private static final float FORCESTONE_HARDNESS = 3.0F;
    private static final float FORCESTONE_EXPLOSION_RESISTANCE = 300.0F;
    private static final int CRYSTAL_LIGHT_LEVEL = 4;
    private static final float LIGHTSABER_STAND_HARDNESS = 1.5F;
    private static final float LIGHTSABER_STAND_EXPLOSION_RESISTANCE = 30.0F;
    private static final float LIGHTSABER_FORGE_HARDNESS = 1.5F;
    private static final float LIGHTSABER_FORGE_EXPLOSION_RESISTANCE = 300.0F;
    private static final float DISASSEMBLY_STATION_HARDNESS = 5.0F;
    private static final float DISASSEMBLY_STATION_EXPLOSION_RESISTANCE = 30.0F;
    private static final int HOLOCRON_LIGHT_LEVEL = 8;
    private static final float HOLOCRON_EXPLOSION_RESISTANCE = 6000.0F;
    private static final float SITH_COFFIN_HARDNESS = 50.0F;
    private static final float SITH_COFFIN_EXPLOSION_RESISTANCE = 6000.0F;
    private static final float CRYSTAL_DISPLAY_STAND_HARDNESS = 2.0F;
    private static final float CRYSTAL_DISPLAY_STAND_EXPLOSION_RESISTANCE = 6.0F;

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Lightsabers.MODID);

    public static final Supplier<BlockForcestone> LIGHT_FORCESTONE = BLOCKS.registerBlock(
            "light_forcestone",
            BlockForcestone::new,
            ModBlocks::forcestoneProperties
    );
    public static final Supplier<BlockCrystal> LIGHTSABER_CRYSTAL = BLOCKS.registerBlock(
            "lightsaber_crystal",
            BlockCrystal::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2.0F, 30.0F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> CRYSTAL_LIGHT_LEVEL)
                    .noCollision()
                    .noOcclusion()
                    .noLootTable()
    );
    public static final Supplier<BlockLightsaberStand> LIGHTSABER_STAND = BLOCKS.registerBlock(
            "lightsaber_stand",
            BlockLightsaberStand::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(
                            LIGHTSABER_STAND_HARDNESS,
                            LIGHTSABER_STAND_EXPLOSION_RESISTANCE
                    )
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );
    public static final Supplier<BlockCrystalDisplayStand> CRYSTAL_DISPLAY_STAND =
            BLOCKS.registerBlock(
                    "crystal_display_stand",
                    BlockCrystalDisplayStand::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(
                                    CRYSTAL_DISPLAY_STAND_HARDNESS,
                                    CRYSTAL_DISPLAY_STAND_EXPLOSION_RESISTANCE
                            )
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
            );
    public static final Supplier<BlockLightsaberForge> LIGHTSABER_FORGE =
            BLOCKS.registerBlock(
                    "lightsaber_forge",
                    BlockLightsaberForge::new,
                    ModBlocks::lightsaberForgeProperties
            );
    public static final Supplier<BlockDisassemblyStation> DISASSEMBLY_STATION =
            BLOCKS.registerBlock(
                    "disassembly_station",
                    BlockDisassemblyStation::new,
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(
                                    DISASSEMBLY_STATION_HARDNESS,
                                    DISASSEMBLY_STATION_EXPLOSION_RESISTANCE
                            )
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.BLOCK)
                            .noOcclusion()
            );
    public static final Supplier<BlockHolocron> JEDI_HOLOCRON = BLOCKS.registerBlock(
            "jedi_holocron",
            properties -> new BlockHolocron(properties, HolocronType.JEDI),
            () -> holocronProperties(MapColor.COLOR_LIGHT_BLUE)
    );
    public static final Supplier<BlockHolocron> HOLOCRON = BLOCKS.registerBlock(
            "holocron",
            properties -> new BlockHolocron(properties, HolocronType.SITH),
            () -> holocronProperties(MapColor.COLOR_PURPLE)
    );
    public static final Supplier<BlockSithCoffin> SITH_COFFIN = BLOCKS.registerBlock(
            "sith_coffin",
            BlockSithCoffin::new,
            () -> coffinProperties(MapColor.COLOR_BLACK)
    );
    public static final Supplier<BlockSithStoneCoffin> SITH_STONE_COFFIN =
            BLOCKS.registerBlock(
                    "sith_stone_coffin",
                    BlockSithStoneCoffin::new,
                    () -> coffinProperties(MapColor.TERRACOTTA_BLACK)
            );
    public static final Supplier<BlockForcestone> DARK_FORCESTONE = BLOCKS.registerBlock(
            "dark_forcestone",
            BlockForcestone::new,
            ModBlocks::forcestoneProperties
    );
    public static final Supplier<BlockPillar> LIGHT_ACTIVATED_FORCESTONE = BLOCKS.registerBlock(
            "light_activated_forcestone_pillar",
            BlockPillar::new,
            () -> forcestoneProperties().lightLevel(state -> FULL_LIGHT_LEVEL)
    );
    public static final Supplier<BlockPillar> DARK_ACTIVATED_FORCESTONE = BLOCKS.registerBlock(
            "dark_activated_forcestone_pillar",
            BlockPillar::new,
            () -> forcestoneProperties().lightLevel(state -> FULL_LIGHT_LEVEL)
    );
    public static final Supplier<BlockModStairs> LIGHT_FORCESTONE_STAIRS = BLOCKS.registerBlock(
            "light_forcestone_stairs",
            properties -> new BlockModStairs(
                    LIGHT_FORCESTONE.get().defaultBlockState(),
                    properties
            ),
            ModBlocks::forcestoneProperties
    );
    public static final Supplier<BlockModStairs> DARK_FORCESTONE_STAIRS = BLOCKS.registerBlock(
            "dark_forcestone_stairs",
            properties -> new BlockModStairs(
                    DARK_FORCESTONE.get().defaultBlockState(),
                    properties
            ),
            ModBlocks::forcestoneProperties
    );
    public static final Supplier<BlockModSlab> FORCESTONE_SLAB = BLOCKS.registerBlock(
            "forcestone_slab",
            BlockModSlab::new,
            ModBlocks::forcestoneProperties
    );

    public static final Supplier<ItemForcestone> LIGHT_FORCESTONE_ITEM =
            ModItems.ITEMS.registerItem(
                    "light_forcestone",
                    properties -> new ItemForcestone(LIGHT_FORCESTONE.get(), properties)
            );
    public static final Supplier<ItemCrystal> LIGHTSABER_CRYSTAL_ITEM =
            ModItems.ITEMS.registerItem(
                    "lightsaber_crystal",
                    ItemCrystal::new
            );
    public static final Supplier<ItemCrystalBlock> LIGHTSABER_CRYSTAL_BLOCK_ITEM =
            ModItems.ITEMS.registerItem(
                    "lightsaber_crystal_block",
                    properties -> new ItemCrystalBlock(LIGHTSABER_CRYSTAL.get(), properties)
            );
    public static final Supplier<ItemLightsaberStand> LIGHTSABER_STAND_ITEM =
            ModItems.ITEMS.registerItem(
                    "lightsaber_stand",
                    properties -> new ItemLightsaberStand(LIGHTSABER_STAND.get(), properties)
            );
    public static final Supplier<BlockItem> CRYSTAL_DISPLAY_STAND_ITEM =
            ModItems.ITEMS.registerItem(
                    "crystal_display_stand",
                    properties -> new BlockItem(CRYSTAL_DISPLAY_STAND.get(), properties)
            );
    public static final Supplier<ItemLightsaberForge> LIGHTSABER_FORGE_ITEM =
            ModItems.ITEMS.registerItem(
                    "lightsaber_forge",
                    properties -> new ItemLightsaberForge(LIGHTSABER_FORGE.get(), properties)
            );
    public static final Supplier<ItemDisassemblyTable> DISASSEMBLY_STATION_ITEM =
            ModItems.ITEMS.registerItem(
                    "disassembly_station",
                    properties -> new ItemDisassemblyTable(DISASSEMBLY_STATION.get(), properties)
            );
    public static final Supplier<ItemHolocron> JEDI_HOLOCRON_ITEM =
            ModItems.ITEMS.registerItem(
                    "jedi_holocron",
                    properties -> new ItemHolocron(JEDI_HOLOCRON.get(), properties)
            );
    public static final Supplier<ItemHolocron> HOLOCRON_ITEM =
            ModItems.ITEMS.registerItem(
                    "holocron",
                    properties -> new ItemHolocron(HOLOCRON.get(), properties)
            );
    public static final Supplier<ItemSithCoffin> SITH_COFFIN_ITEM =
            ModItems.ITEMS.registerItem(
                    "sith_coffin",
                    properties -> new ItemSithCoffin(SITH_COFFIN.get(), properties)
            );
    public static final Supplier<ItemSithStoneCoffin> SITH_STONE_COFFIN_ITEM =
            ModItems.ITEMS.registerItem(
                    "sith_stone_coffin",
                    properties -> new ItemSithStoneCoffin(SITH_STONE_COFFIN.get(), properties)
            );
    public static final Supplier<ItemForcestone> DARK_FORCESTONE_ITEM =
            ModItems.ITEMS.registerItem(
                    "dark_forcestone",
                    properties -> new ItemForcestone(DARK_FORCESTONE.get(), properties)
            );
    public static final Supplier<BlockItem> LIGHT_ACTIVATED_FORCESTONE_ITEM =
            registerBlockItem("light_activated_forcestone_pillar", LIGHT_ACTIVATED_FORCESTONE);
    public static final Supplier<BlockItem> DARK_ACTIVATED_FORCESTONE_ITEM =
            registerBlockItem("dark_activated_forcestone_pillar", DARK_ACTIVATED_FORCESTONE);
    public static final Supplier<BlockItem> LIGHT_FORCESTONE_STAIRS_ITEM =
            registerBlockItem("light_forcestone_stairs", LIGHT_FORCESTONE_STAIRS);
    public static final Supplier<BlockItem> DARK_FORCESTONE_STAIRS_ITEM =
            registerBlockItem("dark_forcestone_stairs", DARK_FORCESTONE_STAIRS);
    public static final Supplier<ItemForcestoneSlab> FORCESTONE_SLAB_ITEM =
            ModItems.ITEMS.registerItem(
                    "forcestone_slab",
                    properties -> new ItemForcestoneSlab(FORCESTONE_SLAB.get(), properties)
            );

    public static Block lightsaberCrystal;
    public static Block lightsaberForge;
    public static Block lightsaberStand;
    public static Block crystalDisplayStand;
    public static Block disassemblyStation;
    public static Block sithCoffin;
    public static Block sithStoneCoffin;
    public static Block holocron;
    public static Block lightForcestone;
    public static Block lightActivatedForcestone;
    public static Block lightForcestoneStairs;
    public static Block darkForcestone;
    public static Block darkActivatedForcestone;
    public static Block darkForcestoneStairs;
    public static Block forcestoneDoubleSlab;
    public static Block forcestoneSlab;

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        modEventBus.addListener(ModBlocks::commonSetup);
    }

    private static BlockBehaviour.Properties forcestoneProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(FORCESTONE_HARDNESS, FORCESTONE_EXPLOSION_RESISTANCE)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties lightsaberForgeProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(LIGHTSABER_FORGE_HARDNESS, LIGHTSABER_FORGE_EXPLOSION_RESISTANCE)
                .sound(SoundType.METAL)
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties holocronProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(0.0F, HOLOCRON_EXPLOSION_RESISTANCE)
                .lightLevel(state -> HOLOCRON_LIGHT_LEVEL)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties coffinProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(SITH_COFFIN_HARDNESS, SITH_COFFIN_EXPLOSION_RESISTANCE)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion()
                .noLootTable();
    }

    private static <T extends Block> Supplier<BlockItem> registerBlockItem(
            String name,
            Supplier<T> block
    ) {
        return ModItems.ITEMS.registerItem(
                name,
                properties -> new BlockItem(
                        block.get(),
                        properties.useBlockDescriptionPrefix()
                )
        );
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            lightsaberCrystal = LIGHTSABER_CRYSTAL.get();
            lightsaberStand = LIGHTSABER_STAND.get();
            crystalDisplayStand = CRYSTAL_DISPLAY_STAND.get();
            lightsaberForge = LIGHTSABER_FORGE.get();
            disassemblyStation = DISASSEMBLY_STATION.get();
            sithCoffin = SITH_COFFIN.get();
            sithStoneCoffin = SITH_STONE_COFFIN.get();
            holocron = HOLOCRON.get();
            ModItems.lightsaberCrystal = LIGHTSABER_CRYSTAL_ITEM.get();
            lightForcestone = LIGHT_FORCESTONE.get();
            darkForcestone = DARK_FORCESTONE.get();
            lightActivatedForcestone = LIGHT_ACTIVATED_FORCESTONE.get();
            darkActivatedForcestone = DARK_ACTIVATED_FORCESTONE.get();
            lightForcestoneStairs = LIGHT_FORCESTONE_STAIRS.get();
            darkForcestoneStairs = DARK_FORCESTONE_STAIRS.get();
            forcestoneSlab = FORCESTONE_SLAB.get();
            forcestoneDoubleSlab = FORCESTONE_SLAB.get();
        });
    }
}
