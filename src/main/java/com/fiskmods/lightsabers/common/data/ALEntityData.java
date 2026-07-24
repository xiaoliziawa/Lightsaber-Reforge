package com.fiskmods.lightsabers.common.data;

import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ALEntityData implements ModAttachments.PersistentData {

    public List<StatusEffect> activeEffects = new ArrayList<>();
    public boolean forcePushed;

    public static ALEntityData getData(LivingEntity entity) {
        return entity.getData(ModAttachments.LIVING_DATA);
    }

    @Nullable
    public static ALEntityData getDataOrNull(LivingEntity entity) {
        return entity.getExistingDataOrNull(ModAttachments.LIVING_DATA);
    }

    public static boolean hasData(LivingEntity entity) {
        return true;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ForcePushed", forcePushed);
        if (!activeEffects.isEmpty()) {
            ListTag effectsTag = new ListTag();
            for (StatusEffect effect : activeEffects) {
                effectsTag.add(effect.writeToNBT(new CompoundTag()));
            }
            tag.put("Effects", effectsTag);
        }
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        forcePushed = tag.getBoolean("ForcePushed");
        activeEffects.clear();
        if (tag.contains("Effects", Tag.TAG_LIST)) {
            ListTag effectsTag = tag.getList("Effects", Tag.TAG_COMPOUND);
            for (int i = 0; i < effectsTag.size(); i++) {
                StatusEffect effect = StatusEffect.readFromNBT(effectsTag.getCompound(i));
                if (effect != null) {
                    activeEffects.add(effect);
                }
            }
            activeEffects.sort(null);
        }
    }

}
