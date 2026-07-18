package com.fiskmods.lightsabers.common.data;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterCapability
public final class ALEntityData {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, "living_data");
    public static final Capability<ALEntityData> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    public List<StatusEffect> activeEffects = new ArrayList<>();
    public boolean forcePushed;

    public static ALEntityData getData(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElseThrow(
                () -> new IllegalStateException("Missing Advanced Lightsabers living entity capability")
        );
    }

    @Nullable
    public static ALEntityData getDataOrNull(LivingEntity entity) {
        return entity.getCapability(CAPABILITY).orElse(null);
    }

    public static boolean hasData(LivingEntity entity) {
        return getDataOrNull(entity) != null;
    }

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

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ALEntityData entityData = new ALEntityData();
        private LazyOptional<ALEntityData> optional = createOptional();

        private LazyOptional<ALEntityData> createOptional() {
            return LazyOptional.of(() -> entityData);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(
                @NotNull Capability<T> capability,
                @Nullable Direction side
        ) {
            if (capability != CAPABILITY) {
                return LazyOptional.empty();
            }
            if (!optional.isPresent()) {
                optional = createOptional();
            }
            return optional.cast();
        }

        @Override
        public CompoundTag serializeNBT() {
            return entityData.save();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            entityData.load(tag);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }

    public static final class EntityCapabilityEvents {
        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity) {
                Provider provider = new Provider();
                event.addCapability(ID, provider);
                event.addListener(provider::invalidate);
            }
        }
    }
}
