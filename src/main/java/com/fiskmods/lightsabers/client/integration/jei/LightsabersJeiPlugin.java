package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.gui.GuiDisassemblyStation;
import com.fiskmods.lightsabers.client.gui.GuiLightsaberForge;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.container.ContainerLightsaberForge;
import com.fiskmods.lightsabers.common.container.ModMenus;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemFocusingCrystal;
import com.fiskmods.lightsabers.common.item.ItemLightsaberPart;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@OnlyIn(Dist.CLIENT)
public final class LightsabersJeiPlugin implements IModPlugin {
    public static final RecipeType<LightsaberForgeJeiRecipe> LIGHTSABER_FORGE =
            RecipeType.create(
                    Lightsabers.MODID,
                    "lightsaber_forge",
                    LightsaberForgeJeiRecipe.class
            );
    public static final RecipeType<DoubleLightsaberJeiRecipe> DOUBLE_LIGHTSABER =
            RecipeType.create(
                    Lightsabers.MODID,
                    "double_lightsaber",
                    DoubleLightsaberJeiRecipe.class
            );
    public static final RecipeType<DisassemblyJeiRecipe> DISASSEMBLY = RecipeType.create(
            Lightsabers.MODID,
            "disassembly",
            DisassemblyJeiRecipe.class
    );

    private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "jei_plugin"
    );

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                ModItems.LIGHTSABER.get(),
                (stack, context) -> Long.toUnsignedString(LightsaberData.get(stack).hash)
        );
        registration.registerSubtypeInterpreter(
                ModItems.DOUBLE_LIGHTSABER.get(),
                (stack, context) -> {
                    LightsaberData[] data = ItemDoubleLightsaber.get(stack);
                    return Long.toUnsignedString(data[0].hash)
                            + ':' + Long.toUnsignedString(data[1].hash);
                }
        );
        registration.registerSubtypeInterpreter(
                ModItems.FOCUSING_CRYSTAL.get(),
                (stack, context) -> ItemFocusingCrystal.get(stack).name()
        );
        registration.registerSubtypeInterpreter(
                ModItems.EMITTER.get(),
                (stack, context) -> Integer.toString(
                        Hilt.getIdFromHilt(ItemLightsaberPart.get(stack))
                )
        );
        registration.registerSubtypeInterpreter(
                ModItems.SWITCH_SECTION.get(),
                (stack, context) -> Integer.toString(
                        Hilt.getIdFromHilt(ItemLightsaberPart.get(stack))
                )
        );
        registration.registerSubtypeInterpreter(
                ModItems.GRIP.get(),
                (stack, context) -> Integer.toString(
                        Hilt.getIdFromHilt(ItemLightsaberPart.get(stack))
                )
        );
        registration.registerSubtypeInterpreter(
                ModItems.POMMEL.get(),
                (stack, context) -> Integer.toString(
                        Hilt.getIdFromHilt(ItemLightsaberPart.get(stack))
                )
        );
        registration.useNbtForSubtypes(
                ModBlocks.LIGHTSABER_CRYSTAL_ITEM.get(),
                ModItems.CRYSTAL_POUCH.get(),
                ModBlocks.LIGHT_FORCESTONE_ITEM.get(),
                ModBlocks.DARK_FORCESTONE_ITEM.get(),
                ModBlocks.FORCESTONE_SLAB_ITEM.get()
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new LightsaberForgeRecipeCategory(
                        registration.getJeiHelpers().getGuiHelper()
                ),
                new DoubleLightsaberRecipeCategory(
                        registration.getJeiHelpers().getGuiHelper()
                ),
                new DisassemblyRecipeCategory(
                        registration.getJeiHelpers().getGuiHelper()
                )
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<LightsaberForgeJeiRecipe> forgeRecipes = new ArrayList<>();
        List<DoubleLightsaberJeiRecipe> doubleRecipes = new ArrayList<>();
        List<DisassemblyJeiRecipe> disassemblyRecipes = new ArrayList<>();
        for (Hilt hilt : Hilt.REGISTRY) {
            LightsaberData data = hilt.createDefault();
            if (!data.isTooShort()) {
                forgeRecipes.add(LightsaberForgeJeiRecipe.create(hilt));
                if (data.supportsDoubleLightsaber()) {
                    doubleRecipes.add(DoubleLightsaberJeiRecipe.create(hilt));
                }
            }
            disassemblyRecipes.add(DisassemblyJeiRecipe.create(hilt, false));
            disassemblyRecipes.add(DisassemblyJeiRecipe.create(hilt, true));
        }
        registration.addRecipes(LIGHTSABER_FORGE, forgeRecipes);
        registration.addRecipes(DOUBLE_LIGHTSABER, doubleRecipes);
        registration.addRecipes(DISASSEMBLY, disassemblyRecipes);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                ContainerLightsaberForge.class,
                ModMenus.LIGHTSABER_FORGE.get(),
                LIGHTSABER_FORGE,
                0,
                8,
                9,
                36
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(
                LIGHTSABER_FORGE,
                ModBlocks.LIGHTSABER_FORGE_ITEM.get()
        );
        registration.addRecipeCatalysts(
                DISASSEMBLY,
                ModBlocks.DISASSEMBLY_STATION_ITEM.get()
        );
        registration.addRecipeCatalysts(DOUBLE_LIGHTSABER, Blocks.CRAFTING_TABLE);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(
                GuiLightsaberForge.class,
                126,
                76,
                30,
                10,
                LIGHTSABER_FORGE
        );
        registration.addRecipeClickArea(
                GuiDisassemblyStation.class,
                38,
                35,
                26,
                18,
                DISASSEMBLY
        );
    }
}
