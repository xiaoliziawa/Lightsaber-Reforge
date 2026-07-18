package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.common.item.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class DoubleLightsaberRecipeCategory
        implements IRecipeCategory<DoubleLightsaberJeiRecipe> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 64;

    private final IDrawable icon;
    private final IDrawable arrow;

    public DoubleLightsaberRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModItems.DOUBLE_LIGHTSABER.get())
        );
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<DoubleLightsaberJeiRecipe> getRecipeType() {
        return LightsabersJeiPlugin.DOUBLE_LIGHTSABER;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.double_lightsaber");
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
            DoubleLightsaberJeiRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(42, 14)
                .setStandardSlotBackground()
                .addItemStack(recipe.upper());
        builder.addInputSlot(42, 34)
                .setStandardSlotBackground()
                .addItemStack(recipe.lower());
        builder.addOutputSlot(111, 25)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(
            DoubleLightsaberJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        arrow.draw(guiGraphics, 72, 25);
    }

    @Override
    public ResourceLocation getRegistryName(DoubleLightsaberJeiRecipe recipe) {
        return recipe.id();
    }
}
