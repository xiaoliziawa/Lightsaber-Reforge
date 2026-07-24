package com.fiskmods.lightsabers.client.integration.jei;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.tileentity.TileEntityDisassemblyStation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record DisassemblyJeiRecipe(
        ResourceLocation id,
        ItemStack input,
        List<Output> outputs
) {
    public static DisassemblyJeiRecipe create(Hilt hilt, boolean doubleBladed) {
        LightsaberData data = hilt.createDefault();
        ItemStack single = data.create();
        ItemStack input = doubleBladed
                ? ItemDoubleLightsaber.create(new LightsaberData[] {data, data})
                : single;
        String type = doubleBladed ? "double" : "single";
        return new DisassemblyJeiRecipe(
                ResourceLocation.fromNamespaceAndPath(
                        Lightsabers.MODID,
                        LightsaberForgeJeiRecipe.recipeId("disassembly", hilt).getPath()
                                + "/" + type
                ),
                input,
                createOutputs(TileEntityDisassemblyStation.getOutput(input))
        );
    }

    private static List<Output> createOutputs(Map<ItemStack, Float> outputMap) {
        return outputMap.entrySet().stream()
                .map(entry -> new Output(entry.getKey().copy(), entry.getValue()))
                .sorted(Comparator.comparing(Output::chance).reversed())
                .toList();
    }

    public record Output(ItemStack stack, float chance) {
    }
}
