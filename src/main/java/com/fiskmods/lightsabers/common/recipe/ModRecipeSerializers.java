package com.fiskmods.lightsabers.common.recipe;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Lightsabers.MODID);

    public static final RegistryObject<RecipeSerializer<RecipesDoubleLightsaber>>
            DOUBLE_LIGHTSABER = RECIPE_SERIALIZERS.register(
                    "double_lightsaber",
                    () -> new SimpleCraftingRecipeSerializer<>(RecipesDoubleLightsaber::new)
            );

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
