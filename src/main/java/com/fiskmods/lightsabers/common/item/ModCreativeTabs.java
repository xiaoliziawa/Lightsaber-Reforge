package com.fiskmods.lightsabers.common.item;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.block.BlockForcestone;
import com.fiskmods.lightsabers.common.block.BlockModSlab;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.hilt.HiltManager;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Lightsabers.MODID);

    public static final Supplier<CreativeModeTab> LIGHTSABERS = TABS.register(
            "lightsabers",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.lightsabers"))
                    .icon(() -> new ItemStack(ModItems.LIGHTSABER.get()))
                    .displayItems(ModCreativeTabs::displayItems)
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }

    private static void displayItems(
            CreativeModeTab.ItemDisplayParameters parameters,
            CreativeModeTab.Output output
    ) {
        for (Hilt hilt : Hilt.REGISTRY) {
            LightsaberData data = hilt == HiltManager.SPINNING
                    ? hilt.createDefault().set(CrystalColor.RED)
                    : hilt.createDefault();
            ItemStack lightsaber = data.create();
            output.accept(lightsaber);
            if (data.supportsDoubleLightsaber()) {
                output.accept(ItemDoubleLightsaber.create(new LightsaberData[] {data, data}));
            }
        }

        output.accept(ModItems.CIRCUITRY.get());
        for (FocusingCrystal crystal : FocusingCrystal.values()) {
            output.accept(ItemFocusingCrystal.create(crystal));
        }
        for (CrystalColor color : CrystalColor.values()) {
            output.accept(ItemCrystal.create(color));
            output.accept(ItemCrystal.create(color, ModItems.CRYSTAL_POUCH.get()));
        }
        for (Hilt hilt : Hilt.REGISTRY) {
            for (PartType partType : PartType.values()) {
                output.accept(ItemLightsaberPart.create(partType, hilt));
            }
        }

        for (BlockForcestone.Variant variant : BlockForcestone.Variant.values()) {
            output.accept(ItemForcestone.create(ModBlocks.LIGHT_FORCESTONE.get(), variant));
            output.accept(ItemForcestone.create(ModBlocks.DARK_FORCESTONE.get(), variant));
        }
        output.accept(ModBlocks.LIGHT_ACTIVATED_FORCESTONE_ITEM.get());
        output.accept(ModBlocks.DARK_ACTIVATED_FORCESTONE_ITEM.get());
        output.accept(ModBlocks.LIGHT_FORCESTONE_STAIRS_ITEM.get());
        output.accept(ModBlocks.DARK_FORCESTONE_STAIRS_ITEM.get());
        for (BlockModSlab.Variant variant : BlockModSlab.Variant.values()) {
            output.accept(ItemForcestoneSlab.create(ModBlocks.FORCESTONE_SLAB.get(), variant));
        }
        for (CrystalColor color : CrystalColor.values()) {
            output.accept(ItemCrystal.createBlock(color));
        }
        output.accept(ModBlocks.LIGHTSABER_STAND_ITEM.get());
        output.accept(ModBlocks.CRYSTAL_DISPLAY_STAND_ITEM.get());
        output.accept(ModBlocks.LIGHTSABER_FORGE_ITEM.get());
        output.accept(ModBlocks.DISASSEMBLY_STATION_ITEM.get());
        output.accept(ModBlocks.SITH_COFFIN_ITEM.get());
        output.accept(ModBlocks.SITH_STONE_COFFIN_ITEM.get());
        output.accept(ModBlocks.JEDI_HOLOCRON_ITEM.get());
        output.accept(ModBlocks.HOLOCRON_ITEM.get());
    }
}
