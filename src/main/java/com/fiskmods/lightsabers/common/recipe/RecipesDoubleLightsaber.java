package com.fiskmods.lightsabers.common.recipe;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RecipesDoubleLightsaber extends CustomRecipe {
    public RecipesDoubleLightsaber(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput crafting, Level level) {
        return findUpperSlot(crafting) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput crafting, HolderLookup.Provider registries) {
        int upperSlot = findUpperSlot(crafting);
        if (upperSlot < 0) {
            return ItemStack.EMPTY;
        }

        return ItemDoubleLightsaber.create(
                crafting.getItem(upperSlot),
                crafting.getItem(upperSlot + crafting.width())
        );
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.DOUBLE_LIGHTSABER.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DOUBLE_LIGHTSABER.get();
    }

    private static int findUpperSlot(CraftingInput crafting) {
        int upperSlot = -1;
        int filledSlots = 0;

        for (int slot = 0; slot < crafting.size(); slot++) {
            ItemStack stack = crafting.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(ModItems.LIGHTSABER.get())) {
                return -1;
            }
            if (!LightsaberData.get(stack).supportsDoubleLightsaber()) {
                return -1;
            }
            if (upperSlot < 0) {
                upperSlot = slot;
            }
            filledSlots++;
        }

        if (filledSlots != 2) {
            return -1;
        }

        int lowerSlot = upperSlot + crafting.width();
        return lowerSlot < crafting.size()
                && crafting.getItem(lowerSlot).is(ModItems.LIGHTSABER.get())
                ? upperSlot
                : -1;
    }
}
