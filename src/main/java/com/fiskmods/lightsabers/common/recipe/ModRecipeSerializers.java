package com.fiskmods.lightsabers.common.recipe;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Lightsabers.MODID);

    public static final Supplier<RecipeSerializer<RecipesDoubleLightsaber>>
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
