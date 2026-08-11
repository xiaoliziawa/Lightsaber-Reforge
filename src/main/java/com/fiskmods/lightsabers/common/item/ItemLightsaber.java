package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.function.Consumer;

public class ItemLightsaber extends ItemLightsaberBase {
    private static final int SPINNING_USE_DURATION = 72000;

    public ItemLightsaber(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isActive(stack) || !isSpinningLightsaber(stack)) {
            return super.use(level, player, hand);
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return isSpinningLightsaber(stack) ? SPINNING_USE_DURATION : 0;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag != null
                && tag.getString(ALConstants.TAG_LIGHTSABER_SPECIAL).isPresent()) {
            return Component.literal("FISHSTICKS!!").withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        return super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag != null && tag.getCompound("Lightsaber").isPresent()) {
            LightsaberData.get(stack);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        LightsaberData data = LightsaberData.get(stack);
        Hilt[] hilts = data.getHilt();
        tooltip.accept(Component.translatable("lightsaber.color"));
        tooltip.accept(Component.literal("  " + data.getColor().getLocalizedName()));
        tooltip.accept(Component.translatable("lightsaber.hilt"));

        if (data.isHiltUniform()) {
            tooltip.accept(Component.literal("  " + hilts[0].getLocalizedName()));
        } else {
            for (Hilt hilt : hilts) {
                tooltip.accept(Component.literal("  " + hilt.getLocalizedName()));
            }
        }

        FocusingCrystal[] crystals = data.getFocusingCrystals();
        if (crystals.length > 0) {
            tooltip.accept(Component.translatable("lightsaber.focusingCrystals"));
            for (FocusingCrystal crystal : crystals) {
                tooltip.accept(Component.literal("  " + crystal.getLocalizedName()));
            }
        }

        if (flag.isAdvanced()) {
            tooltip.accept(Component.translatable(
                    "lightsaber.code.single",
                    Long.toHexString(data.hash).toUpperCase(Locale.ROOT)
            ));
        }
    }

}
