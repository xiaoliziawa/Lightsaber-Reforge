package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.tileentity.TileEntityDisassemblyStation;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class DisassemblyRecipeCategory
        implements IRecipeCategory<DisassemblyJeiRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 70;
    private static final int OUTPUT_COLUMNS = 6;

    private final IDrawable icon;
    private final IDrawable arrow;
    private final List<ItemStack> fuels;

    public DisassemblyRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModBlocks.DISASSEMBLY_STATION_ITEM.get())
        );
        arrow = guiHelper.getRecipeArrow();
        fuels = TileEntityDisassemblyStation.getFuels().keySet().stream()
                .map(ItemStack::copy)
                .toList();
    }

    @Override
    public IRecipeType<DisassemblyJeiRecipe> getRecipeType() {
        return LightsabersJeiPlugin.DISASSEMBLY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.disassembly_station");
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
            DisassemblyJeiRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(8, 8)
                .setStandardSlotBackground()
                .add(recipe.input());
        builder.addInputSlot(8, 44)
                .setStandardSlotBackground()
                .addItemStacks(fuels);

        for (int index = 0; index < recipe.outputs().size(); index++) {
            DisassemblyJeiRecipe.Output output = recipe.outputs().get(index);
            int x = 56 + 18 * (index % OUTPUT_COLUMNS);
            int y = 8 + 18 * (index / OUTPUT_COLUMNS);
            builder.addOutputSlot(x, y)
                    .setStandardSlotBackground()
                    .add(output.stack())
                    .addRichTooltipCallback((slot, tooltip) -> tooltip.add(
                            Component.translatable(
                                    "jei.lightsabers.chance",
                                    Math.round(output.chance() * 1000.0F) / 10.0F
                            )
                    ));
        }
    }

    @Override
    public void draw(
            DisassemblyJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY
    ) {
        arrow.draw(guiGraphics, 31, 24);
    }

    @Override
    public Identifier getIdentifier(DisassemblyJeiRecipe recipe) {
        return recipe.id();
    }
}
