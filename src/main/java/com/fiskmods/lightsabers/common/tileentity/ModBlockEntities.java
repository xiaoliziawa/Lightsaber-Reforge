package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Lightsabers.MODID);

    public static final RegistryObject<BlockEntityType<TileEntityCrystal>> CRYSTAL =
            BLOCK_ENTITY_TYPES.register(
                    "lightsaber_crystal",
                    () -> BlockEntityType.Builder.of(
                            TileEntityCrystal::new,
                            ModBlocks.LIGHTSABER_CRYSTAL.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<TileEntityLightsaberStand>>
            LIGHTSABER_STAND = BLOCK_ENTITY_TYPES.register(
                    "lightsaber_stand",
                    () -> BlockEntityType.Builder.of(
                            TileEntityLightsaberStand::new,
                            ModBlocks.LIGHTSABER_STAND.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<TileEntityCrystalDisplayStand>>
            CRYSTAL_DISPLAY_STAND = BLOCK_ENTITY_TYPES.register(
                    "crystal_display_stand",
                    () -> BlockEntityType.Builder.of(
                            TileEntityCrystalDisplayStand::new,
                            ModBlocks.CRYSTAL_DISPLAY_STAND.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<TileEntityDisassemblyStation>>
            DISASSEMBLY_STATION = BLOCK_ENTITY_TYPES.register(
                    "disassembly_station",
                    () -> BlockEntityType.Builder.of(
                            TileEntityDisassemblyStation::new,
                            ModBlocks.DISASSEMBLY_STATION.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<TileEntityHolocron>> HOLOCRON =
            BLOCK_ENTITY_TYPES.register(
                    "holocron",
                    () -> BlockEntityType.Builder.of(
                            TileEntityHolocron::new,
                            ModBlocks.JEDI_HOLOCRON.get(),
                            ModBlocks.HOLOCRON.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<TileEntitySithCoffin>> SITH_COFFIN =
            BLOCK_ENTITY_TYPES.register(
                    "sith_coffin",
                    () -> BlockEntityType.Builder.of(
                            TileEntitySithCoffin::new,
                            ModBlocks.SITH_COFFIN.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<TileEntitySithStoneCoffin>>
            SITH_STONE_COFFIN = BLOCK_ENTITY_TYPES.register(
                    "sith_stone_coffin",
                    () -> BlockEntityType.Builder.of(
                            TileEntitySithStoneCoffin::new,
                            ModBlocks.SITH_STONE_COFFIN.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
