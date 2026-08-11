package com.fiskmods.lightsabers.common.recipe;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RecipesDoubleLightsaber extends CustomRecipe {
    public static final RecipesDoubleLightsaber INSTANCE = new RecipesDoubleLightsaber();
    public static final MapCodec<RecipesDoubleLightsaber> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipesDoubleLightsaber> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public boolean matches(CraftingInput crafting, Level level) {
        return findUpperSlot(crafting) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput crafting) {
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
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
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
            if (!isCombinable(stack)) {
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
                && isCombinable(crafting.getItem(lowerSlot))
                ? upperSlot
                : -1;
    }

    private static boolean isCombinable(ItemStack stack) {
        return stack.is(ModItems.LIGHTSABER.get())
                && LightsaberData.get(stack).supportsDoubleLightsaber();
    }
}
