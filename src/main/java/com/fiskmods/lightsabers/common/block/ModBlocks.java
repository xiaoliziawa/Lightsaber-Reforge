package com.fiskmods.lightsabers.common.block;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.item.ItemForcestone;
import com.fiskmods.lightsabers.common.item.ItemForcestoneSlab;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemCrystalBlock;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Lightsabers.MODID);

    public static final RegistryObject<BlockForcestone> LIGHT_FORCESTONE = BLOCKS.register(
            "light_forcestone",
            () -> new BlockForcestone(forcestoneProperties())
    );
    public static final RegistryObject<BlockCrystal> LIGHTSABER_CRYSTAL = BLOCKS.register(
            "lightsaber_crystal",
            () -> new BlockCrystal(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2.0F, 30.0F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> CRYSTAL_LIGHT_LEVEL)
                    .noCollission()
                    .noOcclusion()
                    .noLootTable())
    );
    public static final RegistryObject<BlockLightsaberStand> LIGHTSABER_STAND = BLOCKS.register(
            "lightsaber_stand",
            () -> new BlockLightsaberStand(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(
                            LIGHTSABER_STAND_HARDNESS,
                            LIGHTSABER_STAND_EXPLOSION_RESISTANCE
                    )
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion())
    );
    public static final RegistryObject<BlockCrystalDisplayStand> CRYSTAL_DISPLAY_STAND =
            BLOCKS.register(
                    "crystal_display_stand",
                    () -> new BlockCrystalDisplayStand(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(
                                    CRYSTAL_DISPLAY_STAND_HARDNESS,
                                    CRYSTAL_DISPLAY_STAND_EXPLOSION_RESISTANCE
                            )
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .noOcclusion())
            );
    public static final RegistryObject<BlockLightsaberForge> LIGHTSABER_FORGE =
            BLOCKS.register(
                    "lightsaber_forge",
                    () -> new BlockLightsaberForge(lightsaberForgeProperties())
            );
    public static final RegistryObject<BlockDisassemblyStation> DISASSEMBLY_STATION =
            BLOCKS.register(
                    "disassembly_station",
                    () -> new BlockDisassemblyStation(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(
                                    DISASSEMBLY_STATION_HARDNESS,
                                    DISASSEMBLY_STATION_EXPLOSION_RESISTANCE
                            )
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
                            .pushReaction(PushReaction.BLOCK)
                            .noOcclusion())
            );
    public static final RegistryObject<BlockHolocron> HOLOCRON = BLOCKS.register(
            "holocron",
            () -> new BlockHolocron(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.0F, HOLOCRON_EXPLOSION_RESISTANCE)
                    .lightLevel(state -> HOLOCRON_LIGHT_LEVEL)
                    .noOcclusion())
    );
    public static final RegistryObject<BlockSithCoffin> SITH_COFFIN = BLOCKS.register(
            "sith_coffin",
            () -> new BlockSithCoffin(coffinProperties(MapColor.COLOR_BLACK))
    );
    public static final RegistryObject<BlockSithStoneCoffin> SITH_STONE_COFFIN =
            BLOCKS.register(
                    "sith_stone_coffin",
                    () -> new BlockSithStoneCoffin(coffinProperties(MapColor.TERRACOTTA_BLACK))
            );
    public static final RegistryObject<BlockForcestone> DARK_FORCESTONE = BLOCKS.register(
            "dark_forcestone",
            () -> new BlockForcestone(forcestoneProperties())
    );
    public static final RegistryObject<BlockPillar> LIGHT_ACTIVATED_FORCESTONE = BLOCKS.register(
            "light_activated_forcestone_pillar",
            () -> new BlockPillar(forcestoneProperties().lightLevel(state -> FULL_LIGHT_LEVEL))
    );
    public static final RegistryObject<BlockPillar> DARK_ACTIVATED_FORCESTONE = BLOCKS.register(
            "dark_activated_forcestone_pillar",
            () -> new BlockPillar(forcestoneProperties().lightLevel(state -> FULL_LIGHT_LEVEL))
    );
    public static final RegistryObject<BlockModStairs> LIGHT_FORCESTONE_STAIRS = BLOCKS.register(
            "light_forcestone_stairs",
            () -> new BlockModStairs(
                    () -> LIGHT_FORCESTONE.get().defaultBlockState(),
                    forcestoneProperties()
            )
    );
    public static final RegistryObject<BlockModStairs> DARK_FORCESTONE_STAIRS = BLOCKS.register(
            "dark_forcestone_stairs",
            () -> new BlockModStairs(
                    () -> DARK_FORCESTONE.get().defaultBlockState(),
                    forcestoneProperties()
            )
    );
    public static final RegistryObject<BlockModSlab> FORCESTONE_SLAB = BLOCKS.register(
            "forcestone_slab",
            () -> new BlockModSlab(forcestoneProperties())
    );

    public static final RegistryObject<ItemForcestone> LIGHT_FORCESTONE_ITEM =
            ModItems.ITEMS.register(
                    "light_forcestone",
                    () -> new ItemForcestone(LIGHT_FORCESTONE.get())
            );
    public static final RegistryObject<ItemCrystal> LIGHTSABER_CRYSTAL_ITEM =
            ModItems.ITEMS.register(
                    "lightsaber_crystal",
                    ItemCrystal::new
            );
    public static final RegistryObject<ItemCrystalBlock> LIGHTSABER_CRYSTAL_BLOCK_ITEM =
            ModItems.ITEMS.register(
                    "lightsaber_crystal_block",
                    () -> new ItemCrystalBlock(LIGHTSABER_CRYSTAL.get())
            );
    public static final RegistryObject<ItemLightsaberStand> LIGHTSABER_STAND_ITEM =
            ModItems.ITEMS.register(
                    "lightsaber_stand",
                    () -> new ItemLightsaberStand(LIGHTSABER_STAND.get())
            );
    public static final RegistryObject<BlockItem> CRYSTAL_DISPLAY_STAND_ITEM =
            registerBlockItem("crystal_display_stand", CRYSTAL_DISPLAY_STAND);
    public static final RegistryObject<ItemLightsaberForge> LIGHTSABER_FORGE_ITEM =
            ModItems.ITEMS.register(
                    "lightsaber_forge",
                    () -> new ItemLightsaberForge(LIGHTSABER_FORGE.get())
            );
    public static final RegistryObject<ItemDisassemblyTable> DISASSEMBLY_STATION_ITEM =
            ModItems.ITEMS.register(
                    "disassembly_station",
                    () -> new ItemDisassemblyTable(DISASSEMBLY_STATION.get())
            );
    public static final RegistryObject<BlockItem> HOLOCRON_ITEM =
            ModItems.ITEMS.register(
                    "holocron",
                    () -> new BlockItem(HOLOCRON.get(), new Item.Properties().stacksTo(1))
            );
    public static final RegistryObject<ItemSithCoffin> SITH_COFFIN_ITEM =
            ModItems.ITEMS.register(
                    "sith_coffin",
                    () -> new ItemSithCoffin(SITH_COFFIN.get())
            );
    public static final RegistryObject<ItemSithStoneCoffin> SITH_STONE_COFFIN_ITEM =
            ModItems.ITEMS.register(
                    "sith_stone_coffin",
                    () -> new ItemSithStoneCoffin(SITH_STONE_COFFIN.get())
            );
    public static final RegistryObject<ItemForcestone> DARK_FORCESTONE_ITEM =
            ModItems.ITEMS.register(
                    "dark_forcestone",
                    () -> new ItemForcestone(DARK_FORCESTONE.get())
            );
    public static final RegistryObject<BlockItem> LIGHT_ACTIVATED_FORCESTONE_ITEM =
            registerBlockItem("light_activated_forcestone_pillar", LIGHT_ACTIVATED_FORCESTONE);
    public static final RegistryObject<BlockItem> DARK_ACTIVATED_FORCESTONE_ITEM =
            registerBlockItem("dark_activated_forcestone_pillar", DARK_ACTIVATED_FORCESTONE);
    public static final RegistryObject<BlockItem> LIGHT_FORCESTONE_STAIRS_ITEM =
            registerBlockItem("light_forcestone_stairs", LIGHT_FORCESTONE_STAIRS);
    public static final RegistryObject<BlockItem> DARK_FORCESTONE_STAIRS_ITEM =
            registerBlockItem("dark_forcestone_stairs", DARK_FORCESTONE_STAIRS);
    public static final RegistryObject<ItemForcestoneSlab> FORCESTONE_SLAB_ITEM =
            ModItems.ITEMS.register(
                    "forcestone_slab",
                    () -> new ItemForcestoneSlab(FORCESTONE_SLAB.get())
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

    private static BlockBehaviour.Properties coffinProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(SITH_COFFIN_HARDNESS, SITH_COFFIN_EXPLOSION_RESISTANCE)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.BLOCK)
                .noOcclusion()
                .noLootTable();
    }

    private static <T extends Block> RegistryObject<BlockItem> registerBlockItem(
            String name,
            RegistryObject<T> block
    ) {
        return ModItems.ITEMS.register(
                name,
                () -> new BlockItem(block.get(), new Item.Properties())
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
