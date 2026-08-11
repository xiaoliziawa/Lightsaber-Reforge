package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.damage.ALDamageSources;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import fiskfille.utils.helper.VectorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ItemDoubleLightsaber extends ItemLightsaberBase {
    private static final String FLIPPED_TAG = "Flipped";
    private static final float DOUBLE_ATTACK_DAMAGE = 20.0F;

    public ItemDoubleLightsaber(Item.Properties properties) {
        super(properties);
    }

    @Override
    public float getAttackDamage(ItemStack stack) {
        return DOUBLE_ATTACK_DAMAGE;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag == null) {
            return;
        }
        if (tag.getCompound("UpperLightsaber").isPresent()
                && tag.getCompound("LowerLightsaber").isPresent()) {
            get(stack);
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
        LightsaberData[] sabers = get(stack);
        Hilt[][] hilts = {sabers[0].getHilt(), sabers[1].getHilt()};
        tooltip.accept(Component.translatable("lightsaber.color"));
        tooltip.accept(Component.literal("  " + sabers[0].getColor().getLocalizedName()));
        if (sabers[0].getColor() != sabers[1].getColor()) {
            tooltip.accept(Component.literal("  " + sabers[1].getColor().getLocalizedName()));
        }

        tooltip.accept(Component.translatable("lightsaber.hilt"));
        if (sabers[0].isHiltUniform() && sabers[1].isHiltUniform()) {
            tooltip.accept(Component.literal("  " + hilts[0][0].getLocalizedName()));
            if (hilts[0][0] != hilts[1][0]) {
                tooltip.accept(Component.literal("  " + hilts[1][0].getLocalizedName()));
            }
        } else {
            for (int i = 0; i < (sabers[0].getHiltBits() == sabers[1].getHiltBits() ? 1 : 2); i++) {
                List<Hilt> orderedHilts = new ArrayList<>(Arrays.asList(hilts[i]));
                if (i == 1) {
                    Collections.reverse(orderedHilts);
                }
                for (Hilt hilt : orderedHilts) {
                    tooltip.accept(Component.literal("  " + hilt.getLocalizedName()));
                }
            }
        }

        FocusingCrystal[][] crystals = {
                sabers[0].getFocusingCrystals(),
                sabers[1].getFocusingCrystals()
        };
        if (crystals[0].length > 0 || crystals[1].length > 0) {
            tooltip.accept(Component.translatable("lightsaber.focusingCrystals"));
        }
        String[] sides = {"upper", "lower"};
        for (int i = 0; i < sides.length; i++) {
            if (crystals[i].length == 0) {
                continue;
            }
            tooltip.accept(Component.translatable("lightsaber." + sides[i]).plainCopy().append("  "));
            for (FocusingCrystal crystal : crystals[i]) {
                tooltip.accept(Component.literal("    " + crystal.getLocalizedName()));
            }
        }

        if (flag.isAdvanced()) {
            tooltip.accept(Component.translatable(
                    "lightsaber.code.double",
                    Long.toHexString(sabers[0].hash).toUpperCase(Locale.ROOT),
                    Long.toHexString(sabers[1].hash).toUpperCase(Locale.ROOT)
            ));
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        float width = attacker.getBbWidth() * 2;
        Vec3 center = VectorHelper.getOffsetCoords(attacker, 0, 0, -0.2F);
        AABB area = new AABB(
                center.x - width,
                attacker.getBoundingBox().minY,
                center.z - width,
                center.x + width,
                attacker.getBoundingBox().minY + attacker.getBbHeight(),
                center.z + width
        );
        float damage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity entity : attacker.level().getEntitiesOfClass(
                LivingEntity.class,
                area,
                entity -> entity != attacker && entity != target
        )) {
            entity.hurt(ALDamageSources.causeLightsaberDamage(attacker), damage);
        }
    }

    public static LightsaberData[] readFromNBT(CompoundTag tag) {
        LightsaberData[] sabers = {LightsaberData.EMPTY, LightsaberData.EMPTY};
        if (tag.getCompound("UpperLightsaber").isPresent()
                && tag.getCompound("LowerLightsaber").isPresent()) {
            ListTag list = new ListTag();
            String[] oldKeys = {"UpperLightsaber", "LowerLightsaber"};
            for (int i = 0; i < oldKeys.length; i++) {
                CompoundTag oldTag = tag.getCompoundOrEmpty(oldKeys[i]);
                oldTag.put("Lightsaber", oldTag.copy());
                sabers[i] = LightsaberData.readFromNBT(oldTag);
                list.add(LongTag.valueOf(sabers[i].hash));
                tag.remove(oldKeys[i]);
            }
            tag.put(ALConstants.TAG_LIGHTSABER, list);
        } else {
            Tag lightsaberTag = tag.get(ALConstants.TAG_LIGHTSABER);
            if (lightsaberTag instanceof ListTag list) {
                for (int i = 0; i < Math.min(list.size(), sabers.length); i++) {
                    Tag entry = list.get(i);
                    if (entry instanceof NumericTag numericTag) {
                        sabers[i] = new LightsaberData(numericTag.longValue()).strip();
                    }
                }
            } else if (lightsaberTag instanceof LongArrayTag array) {
                long[] hashes = array.getAsLongArray();
                for (int i = 0; i < Math.min(hashes.length, sabers.length); i++) {
                    sabers[i] = new LightsaberData(hashes[i]).strip();
                }
            }
        }
        return sabers;
    }

    public static LightsaberData[] get(ItemStack stack) {
        if (stack.isEmpty()) {
            return new LightsaberData[] {LightsaberData.EMPTY, LightsaberData.EMPTY};
        }

        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag == null) {
            return new LightsaberData[] {LightsaberData.EMPTY, LightsaberData.EMPTY};
        }

        boolean legacyFormat = tag.getCompound("UpperLightsaber").isPresent()
                && tag.getCompound("LowerLightsaber").isPresent();
        LightsaberData[] sabers = readFromNBT(tag);
        if (legacyFormat) {
            ItemDataHelper.setCustomData(stack, tag);
        }
        return sabers;
    }

    public static boolean isFlipped(ItemStack stack) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        return !stack.isEmpty() && tag != null && tag.getBooleanOr(FLIPPED_TAG, false);
    }

    public static void toggleOrientation(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDoubleLightsaber)) {
            return;
        }

        ItemDataHelper.updateCustomData(stack, tag -> {
            if (tag.getBooleanOr(FLIPPED_TAG, false)) {
                tag.remove(FLIPPED_TAG);
            } else {
                tag.putBoolean(FLIPPED_TAG, true);
            }
        });
    }

    public static ItemStack create(LightsaberData[] sabers) {
        ItemStack stack = new ItemStack(ModItems.DOUBLE_LIGHTSABER.get());
        long[] hashes = new long[Math.min(sabers.length, 2)];
        for (int i = 0; i < hashes.length; i++) {
            hashes[i] = sabers[i].hash;
        }
        ItemDataHelper.updateCustomData(
                stack,
                tag -> tag.put(ALConstants.TAG_LIGHTSABER, new LongArrayTag(hashes))
        );
        return stack;
    }

    public static ItemStack create(ItemStack upper, ItemStack lower) {
        return create(new LightsaberData[] {
                LightsaberData.get(upper),
                LightsaberData.get(lower)
        });
    }
}
