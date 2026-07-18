package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record DoubleLightsaberJeiRecipe(
        ResourceLocation id,
        ItemStack upper,
        ItemStack lower,
        ItemStack output
) {
    public static DoubleLightsaberJeiRecipe create(Hilt hilt) {
        ItemStack upper = hilt.createDefault().create();
        ItemStack lower = upper.copy();
        return new DoubleLightsaberJeiRecipe(
                LightsaberForgeJeiRecipe.recipeId("double_lightsaber", hilt),
                upper,
                lower,
                ItemDoubleLightsaber.create(upper, lower)
        );
    }
}
