package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Lightsabers.MODID);

    public static final RegistryObject<MenuType<ContainerCrystalPouch>> CRYSTAL_POUCH =
            MENU_TYPES.register(
                    "crystal_pouch",
                    () -> IForgeMenuType.create(ContainerCrystalPouch::new)
            );
    public static final RegistryObject<MenuType<ContainerLightsaberForge>> LIGHTSABER_FORGE =
            MENU_TYPES.register(
                    "lightsaber_forge",
                    () -> IForgeMenuType.create(ContainerLightsaberForge::new)
            );
    public static final RegistryObject<MenuType<ContainerDisassemblyStation>>
            DISASSEMBLY_STATION = MENU_TYPES.register(
                    "disassembly_station",
                    () -> IForgeMenuType.create(ContainerDisassemblyStation::new)
            );
    public static final RegistryObject<MenuType<ContainerHolocron>> HOLOCRON =
            MENU_TYPES.register(
                    "holocron",
                    () -> IForgeMenuType.create(ContainerHolocron::new)
            );
    public static final RegistryObject<MenuType<ContainerSithCoffin>> SITH_COFFIN =
            MENU_TYPES.register(
                    "sith_coffin",
                    () -> IForgeMenuType.create(ContainerSithCoffin::new)
            );

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
