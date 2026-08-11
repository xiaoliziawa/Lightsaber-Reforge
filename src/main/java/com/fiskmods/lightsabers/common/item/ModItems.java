package com.fiskmods.lightsabers.common.item;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Lightsabers.MODID);

    public static final Supplier<ItemLightsaber> LIGHTSABER =
            ITEMS.registerItem("lightsaber", ItemLightsaber::new);
    public static final Supplier<ItemDoubleLightsaber> DOUBLE_LIGHTSABER =
            ITEMS.registerItem("double_lightsaber", ItemDoubleLightsaber::new);
    public static final Supplier<ItemCircuitry> CIRCUITRY =
            ITEMS.registerItem("lightsaber_circuitry", ItemCircuitry::new);
    public static final Supplier<ItemFocusingCrystal> FOCUSING_CRYSTAL =
            ITEMS.registerItem("focusing_crystal", ItemFocusingCrystal::new);
    public static final Supplier<ItemCrystalPouch> CRYSTAL_POUCH =
            ITEMS.registerItem("crystal_pouch", ItemCrystalPouch::new);
    public static final Supplier<ItemLightsaberPart> EMITTER = ITEMS.registerItem(
            "lightsaber_blade_emitter",
            properties -> new ItemLightsaberPart(properties, PartType.EMITTER)
    );
    public static final Supplier<ItemLightsaberPart> SWITCH_SECTION = ITEMS.registerItem(
            "lightsaber_switch_module",
            properties -> new ItemLightsaberPart(properties, PartType.SWITCH_SECTION)
    );
    public static final Supplier<ItemLightsaberPart> GRIP = ITEMS.registerItem(
            "lightsaber_grip",
            properties -> new ItemLightsaberPart(properties, PartType.BODY)
    );
    public static final Supplier<ItemLightsaberPart> POMMEL = ITEMS.registerItem(
            "lightsaber_pommel",
            properties -> new ItemLightsaberPart(properties, PartType.POMMEL)
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
