package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemFocusingCrystal;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.item.ItemLightsaberPart;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public record LightsaberForgeJeiRecipe(
        Identifier id,
        List<ItemStack> inputs,
        ItemStack output,
        Component heightText
) {
    public static LightsaberForgeJeiRecipe create(Hilt hilt) {
        LightsaberData data = hilt.createDefault();
        NonNullList<ItemStack> inputs = NonNullList.withSize(8, ItemStack.EMPTY);
        for (PartType type : PartType.values()) {
            inputs.set(type.ordinal(), ItemLightsaberPart.create(type, hilt));
        }
        inputs.set(4, new ItemStack(ModItems.CIRCUITRY.get()));
        inputs.set(5, ItemCrystal.create(data.getColor()));

        FocusingCrystal[] crystals = data.getFocusingCrystals();
        for (int index = 0; index < Math.min(crystals.length, 2); index++) {
            inputs.set(index + 6, ItemFocusingCrystal.create(crystals[index]));
        }

        return new LightsaberForgeJeiRecipe(
                recipeId("lightsaber_forge", hilt),
                List.copyOf(inputs),
                ItemLightsaberBase.setActive(data.create(), true),
                Component.translatable(
                        "gui.lightsaber_forge.height",
                        String.format(Locale.ROOT, "%.1f", data.getHeightCm())
                )
        );
    }

    static Identifier recipeId(String category, Hilt hilt) {
        String name = Hilt.getNameForHilt(hilt);
        Identifier hiltId = Identifier.tryParse(name);
        String path = hiltId == null
                ? Integer.toString(Hilt.getIdFromHilt(hilt))
                : hiltId.getPath();
        return Identifier.fromNamespaceAndPath(
                Lightsabers.MODID,
                "jei/" + category + "/" + path
        );
    }
}
