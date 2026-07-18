package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.damage.ALDamageSources;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import fiskfille.utils.helper.NBTHelper;
import fiskfille.utils.helper.VectorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ItemDoubleLightsaber extends ItemLightsaberBase {
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains("UpperLightsaber", Tag.TAG_COMPOUND)
                    && tag.contains("LowerLightsaber", Tag.TAG_COMPOUND)) {
                get(stack);
            }
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        LightsaberData[] sabers = get(stack);
        Hilt[][] hilts = {sabers[0].getHilt(), sabers[1].getHilt()};
        tooltip.add(Component.translatable("lightsaber.color"));
        tooltip.add(Component.literal("  " + sabers[0].getColor().getLocalizedName()));
        if (sabers[0].getColor() != sabers[1].getColor()) {
            tooltip.add(Component.literal("  " + sabers[1].getColor().getLocalizedName()));
        }

        tooltip.add(Component.translatable("lightsaber.hilt"));
        if (sabers[0].isHiltUniform() && sabers[1].isHiltUniform()) {
            tooltip.add(Component.literal("  " + hilts[0][0].getLocalizedName()));
            if (hilts[0][0] != hilts[1][0]) {
                tooltip.add(Component.literal("  " + hilts[1][0].getLocalizedName()));
            }
        } else {
            for (int i = 0; i < (sabers[0].getHiltBits() == sabers[1].getHiltBits() ? 1 : 2); i++) {
                List<Hilt> orderedHilts = new ArrayList<>(Arrays.asList(hilts[i]));
                if (i == 1) {
                    Collections.reverse(orderedHilts);
                }
                for (Hilt hilt : orderedHilts) {
                    tooltip.add(Component.literal("  " + hilt.getLocalizedName()));
                }
            }
        }

        FocusingCrystal[][] crystals = {
                sabers[0].getFocusingCrystals(),
                sabers[1].getFocusingCrystals()
        };
        if (crystals[0].length > 0 || crystals[1].length > 0) {
            tooltip.add(Component.translatable("lightsaber.focusingCrystals"));
        }
        String[] sides = {"upper", "lower"};
        for (int i = 0; i < sides.length; i++) {
            if (crystals[i].length == 0) {
                continue;
            }
            tooltip.add(Component.translatable("lightsaber." + sides[i]).plainCopy().append("  "));
            for (FocusingCrystal crystal : crystals[i]) {
                tooltip.add(Component.literal("    " + crystal.getLocalizedName()));
            }
        }

        if (flag.isAdvanced()) {
            tooltip.add(Component.translatable(
                    "lightsaber.code.double",
                    Long.toHexString(sabers[0].hash).toUpperCase(Locale.ROOT),
                    Long.toHexString(sabers[1].hash).toUpperCase(Locale.ROOT)
            ));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
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
        return true;
    }

    public static LightsaberData[] readFromNBT(CompoundTag tag) {
        LightsaberData[] sabers = {LightsaberData.EMPTY, LightsaberData.EMPTY};
        if (tag.contains("UpperLightsaber", Tag.TAG_COMPOUND)
                && tag.contains("LowerLightsaber", Tag.TAG_COMPOUND)) {
            ListTag list = new ListTag();
            String[] oldKeys = {"UpperLightsaber", "LowerLightsaber"};
            for (int i = 0; i < oldKeys.length; i++) {
                CompoundTag oldTag = tag.getCompound(oldKeys[i]);
                oldTag.put("Lightsaber", oldTag.copy());
                sabers[i] = LightsaberData.readFromNBT(oldTag);
                list.add(LongTag.valueOf(sabers[i].hash));
                tag.remove(oldKeys[i]);
            }
            tag.put(ALConstants.TAG_LIGHTSABER, list);
        } else if (tag.contains(ALConstants.TAG_LIGHTSABER, Tag.TAG_LIST)) {
            ListTag list = tag.getList(ALConstants.TAG_LIGHTSABER, Tag.TAG_LONG);
            for (int i = 0; i < Math.min(list.size(), sabers.length); i++) {
                LightsaberData data = NBTHelper.readFromNBT(list.get(i), LightsaberData.class);
                if (data != null) {
                    sabers[i] = data.strip();
                }
            }
        }
        return sabers;
    }

    public static LightsaberData[] get(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag()
                ? readFromNBT(stack.getTag())
                : new LightsaberData[] {LightsaberData.EMPTY, LightsaberData.EMPTY};
    }

    public static ItemStack create(LightsaberData[] sabers) {
        ItemStack stack = new ItemStack(ModItems.DOUBLE_LIGHTSABER.get());
        ListTag list = new ListTag();
        for (LightsaberData saber : sabers) {
            list.add(LongTag.valueOf(saber.hash));
        }
        stack.getOrCreateTag().put(ALConstants.TAG_LIGHTSABER, list);
        return stack;
    }

    public static ItemStack create(ItemStack upper, ItemStack lower) {
        return create(new LightsaberData[] {
                LightsaberData.get(upper),
                LightsaberData.get(lower)
        });
    }
}
