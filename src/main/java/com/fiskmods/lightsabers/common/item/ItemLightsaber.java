package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ItemLightsaber extends ItemLightsaberBase {
    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag()
                && stack.getTag().contains(ALConstants.TAG_LIGHTSABER_SPECIAL, Tag.TAG_STRING)) {
            return Component.literal("FISHSTICKS!!").withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        return super.getName(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return stack.hasTag()
                && stack.getTag().contains(ALConstants.TAG_LIGHTSABER_SPECIAL, Tag.TAG_STRING)
                ? Rarity.RARE
                : Rarity.COMMON;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && stack.hasTag()
                && stack.getTag().contains("Lightsaber", Tag.TAG_COMPOUND)) {
            LightsaberData.get(stack);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        LightsaberData data = LightsaberData.get(stack);
        Hilt[] hilts = data.getHilt();
        tooltip.add(Component.translatable("lightsaber.color"));
        tooltip.add(Component.literal("  " + data.getColor().getLocalizedName()));
        tooltip.add(Component.translatable("lightsaber.hilt"));

        if (data.isHiltUniform()) {
            tooltip.add(Component.literal("  " + hilts[0].getLocalizedName()));
        } else {
            for (Hilt hilt : hilts) {
                tooltip.add(Component.literal("  " + hilt.getLocalizedName()));
            }
        }

        FocusingCrystal[] crystals = data.getFocusingCrystals();
        if (crystals.length > 0) {
            tooltip.add(Component.translatable("lightsaber.focusingCrystals"));
            for (FocusingCrystal crystal : crystals) {
                tooltip.add(Component.literal("  " + crystal.getLocalizedName()));
            }
        }

        if (flag.isAdvanced()) {
            tooltip.add(Component.translatable(
                    "lightsaber.code.single",
                    Long.toHexString(data.hash).toUpperCase(Locale.ROOT)
            ));
        }
    }
}
