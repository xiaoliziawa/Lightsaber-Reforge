package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.common.container.ContainerLightsaberForge;
import com.fiskmods.lightsabers.common.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LightsaberForgeRecipeCategory
        implements IRecipeCategory<LightsaberForgeJeiRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 102;

    private final IDrawable icon;

    public LightsaberForgeRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.LIGHTSABER.get()));
    }

    @Override
    public RecipeType<LightsaberForgeJeiRecipe> getRecipeType() {
        return LightsabersJeiPlugin.LIGHTSABER_FORGE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.lightsaber_forge");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            LightsaberForgeJeiRecipe recipe,
            IFocusGroup focuses
    ) {
        for (int slot = 0; slot < recipe.inputs().size(); slot++) {
            ItemStack input = recipe.inputs().get(slot);
            if (input.isEmpty()) {
                continue;
            }
            int[] position = ContainerLightsaberForge.SLOTS[slot];
            builder.addInputSlot(position[0], position[1])
                    .setStandardSlotBackground()
                    .addItemStack(input);
        }
        builder.addOutputSlot(136, 76)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(
            LightsaberForgeJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                recipe.heightText(),
                43,
                51,
                0xFFFFFF,
                false
        );
    }

    @Override
    public ResourceLocation getRegistryName(LightsaberForgeJeiRecipe recipe) {
        return recipe.id();
    }
}
