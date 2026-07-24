package com.fiskmods.lightsabers.common.container;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Lightsabers.MODID);

    public static final Supplier<MenuType<ContainerCrystalPouch>> CRYSTAL_POUCH =
            MENU_TYPES.register(
                    "crystal_pouch",
                    () -> IMenuTypeExtension.create(ContainerCrystalPouch::new)
            );
    public static final Supplier<MenuType<ContainerLightsaberForge>> LIGHTSABER_FORGE =
            MENU_TYPES.register(
                    "lightsaber_forge",
                    () -> IMenuTypeExtension.create(ContainerLightsaberForge::new)
            );
    public static final Supplier<MenuType<ContainerDisassemblyStation>>
            DISASSEMBLY_STATION = MENU_TYPES.register(
                    "disassembly_station",
                    () -> IMenuTypeExtension.create(ContainerDisassemblyStation::new)
            );
    public static final Supplier<MenuType<ContainerHolocron>> HOLOCRON =
            MENU_TYPES.register(
                    "holocron",
                    () -> IMenuTypeExtension.create(ContainerHolocron::new)
            );
    public static final Supplier<MenuType<ContainerSithCoffin>> SITH_COFFIN =
            MENU_TYPES.register(
                    "sith_coffin",
                    () -> IMenuTypeExtension.create(ContainerSithCoffin::new)
            );

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
