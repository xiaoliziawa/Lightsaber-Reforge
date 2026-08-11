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
                    () -> new BlockEntityType<>(
                            TileEntityCrystal::new,
                            ModBlocks.LIGHTSABER_CRYSTAL.get()
                    )
            );
    public static final Supplier<BlockEntityType<TileEntityLightsaberStand>>
            LIGHTSABER_STAND = BLOCK_ENTITY_TYPES.register(
                    "lightsaber_stand",
                    () -> new BlockEntityType<>(
                            TileEntityLightsaberStand::new,
                            ModBlocks.LIGHTSABER_STAND.get()
                    )
            );
    public static final Supplier<BlockEntityType<TileEntityCrystalDisplayStand>>
            CRYSTAL_DISPLAY_STAND = BLOCK_ENTITY_TYPES.register(
                    "crystal_display_stand",
                    () -> new BlockEntityType<>(
                            TileEntityCrystalDisplayStand::new,
                            ModBlocks.CRYSTAL_DISPLAY_STAND.get()
                    )
            );
    public static final Supplier<BlockEntityType<TileEntityDisassemblyStation>>
            DISASSEMBLY_STATION = BLOCK_ENTITY_TYPES.register(
                    "disassembly_station",
                    () -> new BlockEntityType<>(
                            TileEntityDisassemblyStation::new,
                            ModBlocks.DISASSEMBLY_STATION.get()
                    )
            );
    public static final Supplier<BlockEntityType<TileEntityHolocron>> HOLOCRON =
            BLOCK_ENTITY_TYPES.register(
                    "holocron",
                    () -> new BlockEntityType<>(
                            TileEntityHolocron::new,
                            ModBlocks.JEDI_HOLOCRON.get(),
                            ModBlocks.HOLOCRON.get()
                    )
            );
    public static final Supplier<BlockEntityType<TileEntitySithCoffin>> SITH_COFFIN =
            BLOCK_ENTITY_TYPES.register(
                    "sith_coffin",
                    () -> new BlockEntityType<>(
                            TileEntitySithCoffin::new,
                            ModBlocks.SITH_COFFIN.get()
                    )
            );
    public static final Supplier<BlockEntityType<TileEntitySithStoneCoffin>>
            SITH_STONE_COFFIN = BLOCK_ENTITY_TYPES.register(
                    "sith_stone_coffin",
                    () -> new BlockEntityType<>(
                            TileEntitySithStoneCoffin::new,
                            ModBlocks.SITH_STONE_COFFIN.get()
                    )
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
