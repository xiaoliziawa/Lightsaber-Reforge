package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ItemLightsaber extends ItemLightsaberBase {
    private static final int SPINNING_USE_DURATION = 72000;

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isActive(stack) || !isSpinningLightsaber(stack)) {
            return super.use(level, player, hand);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return isSpinningLightsaber(stack) ? SPINNING_USE_DURATION : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag != null
                && tag.contains(ALConstants.TAG_LIGHTSABER_SPECIAL, Tag.TAG_STRING)) {
            return Component.literal("FISHSTICKS!!").withStyle(ChatFormatting.LIGHT_PURPLE);
        }
        return super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (!level.isClientSide && tag != null
                && tag.contains("Lightsaber", Tag.TAG_COMPOUND)) {
            LightsaberData.get(stack);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
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
