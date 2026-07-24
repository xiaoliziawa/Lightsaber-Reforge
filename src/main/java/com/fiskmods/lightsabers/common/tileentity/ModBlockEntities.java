package com.fiskmods.lightsabers.common.tileentity;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Lightsabers.MODID);

    public static final Supplier<BlockEntityType<TileEntityCrystal>> CRYSTAL =
            BLOCK_ENTITY_TYPES.register(
                    "lightsaber_crystal",
                    () -> BlockEntityType.Builder.of(
                            TileEntityCrystal::new,
                            ModBlocks.LIGHTSABER_CRYSTAL.get()
                    ).build(null)
            );
    public static final Supplier<BlockEntityType<TileEntityLightsaberStand>>
            LIGHTSABER_STAND = BLOCK_ENTITY_TYPES.register(
                    "lightsaber_stand",
                    () -> BlockEntityType.Builder.of(
                            TileEntityLightsaberStand::new,
                            ModBlocks.LIGHTSABER_STAND.get()
                    ).build(null)
            );
    public static final Supplier<BlockEntityType<TileEntityCrystalDisplayStand>>
            CRYSTAL_DISPLAY_STAND = BLOCK_ENTITY_TYPES.register(
                    "crystal_display_stand",
                    () -> BlockEntityType.Builder.of(
                            TileEntityCrystalDisplayStand::new,
                            ModBlocks.CRYSTAL_DISPLAY_STAND.get()
                    ).build(null)
            );
    public static final Supplier<BlockEntityType<TileEntityDisassemblyStation>>
            DISASSEMBLY_STATION = BLOCK_ENTITY_TYPES.register(
                    "disassembly_station",
                    () -> BlockEntityType.Builder.of(
                            TileEntityDisassemblyStation::new,
                            ModBlocks.DISASSEMBLY_STATION.get()
                    ).build(null)
            );
    public static final Supplier<BlockEntityType<TileEntityHolocron>> HOLOCRON =
            BLOCK_ENTITY_TYPES.register(
                    "holocron",
                    () -> BlockEntityType.Builder.of(
                            TileEntityHolocron::new,
                            ModBlocks.JEDI_HOLOCRON.get(),
                            ModBlocks.HOLOCRON.get()
                    ).build(null)
            );
    public static final Supplier<BlockEntityType<TileEntitySithCoffin>> SITH_COFFIN =
            BLOCK_ENTITY_TYPES.register(
                    "sith_coffin",
                    () -> BlockEntityType.Builder.of(
                            TileEntitySithCoffin::new,
                            ModBlocks.SITH_COFFIN.get()
                    ).build(null)
            );
    public static final Supplier<BlockEntityType<TileEntitySithStoneCoffin>>
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
