package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Lightsabers.MODID);

    public static final RegistryObject<ItemLightsaber> LIGHTSABER =
            ITEMS.register("lightsaber", ItemLightsaber::new);
    public static final RegistryObject<ItemDoubleLightsaber> DOUBLE_LIGHTSABER =
            ITEMS.register("double_lightsaber", ItemDoubleLightsaber::new);
    public static final RegistryObject<ItemCircuitry> CIRCUITRY =
            ITEMS.register("lightsaber_circuitry", ItemCircuitry::new);
    public static final RegistryObject<ItemFocusingCrystal> FOCUSING_CRYSTAL =
            ITEMS.register("focusing_crystal", ItemFocusingCrystal::new);
    public static final RegistryObject<ItemCrystalPouch> CRYSTAL_POUCH =
            ITEMS.register("crystal_pouch", ItemCrystalPouch::new);
    public static final RegistryObject<ItemLightsaberPart> EMITTER = ITEMS.register(
            "lightsaber_blade_emitter",
            () -> new ItemLightsaberPart(PartType.EMITTER)
    );
    public static final RegistryObject<ItemLightsaberPart> SWITCH_SECTION = ITEMS.register(
            "lightsaber_switch_module",
            () -> new ItemLightsaberPart(PartType.SWITCH_SECTION)
    );
    public static final RegistryObject<ItemLightsaberPart> GRIP = ITEMS.register(
            "lightsaber_grip",
            () -> new ItemLightsaberPart(PartType.BODY)
    );
    public static final RegistryObject<ItemLightsaberPart> POMMEL = ITEMS.register(
            "lightsaber_pommel",
            () -> new ItemLightsaberPart(PartType.POMMEL)
    );

    public static Item circuitry;
    public static Item focusingCrystal;
    public static Item lightsaberCrystal;
    public static Item crystalPouch;
    public static Item emitter;
    public static Item switchSection;
    public static Item grip;
    public static Item pommel;
    public static Item lightsaber;
    public static Item doubleLightsaber;

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            lightsaber = LIGHTSABER.get();
            doubleLightsaber = DOUBLE_LIGHTSABER.get();
            circuitry = CIRCUITRY.get();
            focusingCrystal = FOCUSING_CRYSTAL.get();
            crystalPouch = CRYSTAL_POUCH.get();
            emitter = EMITTER.get();
            switchSection = SWITCH_SECTION.get();
            grip = GRIP.get();
            pommel = POMMEL.get();
        });
    }
}
