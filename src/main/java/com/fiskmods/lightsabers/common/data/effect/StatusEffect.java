package com.fiskmods.lightsabers.common.data.effect;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.network.ALNetworkManager;
import com.fiskmods.lightsabers.common.network.MessageUpdateEffects;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StatusEffect implements Comparable<StatusEffect> {
    public Effect effect;
    public short duration;
    public byte amplifier;
    public UUID casterUUID;

    public StatusEffect(Effect effect, int duration, int amplifier) {
        this.effect = effect;
        this.duration = (short) duration;
        this.amplifier = (byte) amplifier;
    }

    public StatusEffect(Effect effect, int duration, int amplifier, UUID uuid) {
        this(effect, duration, amplifier);
        casterUUID = uuid;
    }

    public StatusEffect(Effect effect, int duration, int amplifier, LivingEntity entity) {
        this(effect, duration, amplifier, entity != null ? entity.getUUID() : null);
    }

    public static StatusEffect fromBytes(ByteBuf buffer) {
        Effect effect = Effect.getEffectById(buffer.readByte());
        if (effect == null) {
            return null;
        }

        StatusEffect status = new StatusEffect(effect, buffer.readShort(), buffer.readByte());
        if (buffer.readBoolean()) {
            status.casterUUID = new UUID(buffer.readLong(), buffer.readLong());
        }
        return status;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(Effect.getIdFromEffect(effect));
        buffer.writeShort(duration);
        buffer.writeByte(amplifier);
        buffer.writeBoolean(casterUUID != null);
        if (casterUUID != null) {
            buffer.writeLong(casterUUID.getMostSignificantBits());
            buffer.writeLong(casterUUID.getLeastSignificantBits());
        }
    }

    public static StatusEffect readFromNBT(CompoundTag tag) {
        Effect effect = Effect.getEffectFromName(tag.getString("Id"));
        if (effect == null) {
            return null;
        }

        UUID caster = null;
        if (tag.hasUUID("CasterUUID")) {
            caster = tag.getUUID("CasterUUID");
        } else if (tag.contains("CasterUUIDMost", Tag.TAG_ANY_NUMERIC)
                && tag.contains("CasterUUIDLeast", Tag.TAG_ANY_NUMERIC)) {
            caster = new UUID(tag.getLong("CasterUUIDMost"), tag.getLong("CasterUUIDLeast"));
        }
        return new StatusEffect(effect, tag.getInt("Duration"), tag.getInt("Amplifier"), caster);
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putString("Id", effect.delegate.name());
        tag.putInt("Duration", duration);
        tag.putInt("Amplifier", amplifier);
        if (casterUUID != null) {
            tag.putUUID("CasterUUID", casterUUID);
        }
        return tag;
    }

    public boolean isMaxDuration() {
        return duration >= Short.MAX_VALUE;
    }

    public static void add(LivingEntity entity, Effect effect, int duration, int amplifier) {
        add(entity, (LivingEntity) null, effect, duration, amplifier);
    }

    public static void add(LivingEntity entity, Entity caster, Effect effect, int duration, int amplifier) {
        add(entity, caster instanceof LivingEntity livingCaster ? livingCaster : null, effect, duration, amplifier);
    }

    public static void add(
            LivingEntity entity,
            LivingEntity caster,
            Effect effect,
            int duration,
            int amplifier
    ) {
        ALEntityData data = ALEntityData.getDataOrNull(entity);
        if (data == null) {
            return;
        }
        List<StatusEffect> effects = data.activeEffects;
        int clampedDuration = Mth.clamp(duration, Short.MIN_VALUE, Short.MAX_VALUE);

        for (StatusEffect status : effects) {
            if (status.effect != effect) {
                continue;
            }

            short previousDuration = status.duration;
            byte previousAmplifier = status.amplifier;
            status.duration = (short) Math.max(status.duration, clampedDuration);
            status.amplifier = (byte) Math.max(status.amplifier, amplifier);
            if (caster != null) {
                status.casterUUID = caster.getUUID();
            }

            if (previousDuration != status.duration || previousAmplifier != status.amplifier) {
                effects.sort(null);
                sync(entity, effects);
            }
            return;
        }

        effects.add(new StatusEffect(effect, clampedDuration, amplifier, caster));
        effects.sort(null);
        sync(entity, effects);
    }

    public static List<StatusEffect> get(LivingEntity entity) {
        ALEntityData data = ALEntityData.getDataOrNull(entity);
        return data == null ? List.of() : data.activeEffects;
    }

    public static StatusEffect get(LivingEntity entity, Effect effect) {
        for (StatusEffect status : get(entity)) {
            if (status.effect == effect) {
                return status;
            }
        }
        return null;
    }

    public static boolean has(LivingEntity entity, Effect effect) {
        return get(entity, effect) != null;
    }

    public static void clear(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        List<StatusEffect> effects = get(entity);
        if (!effects.isEmpty()) {
            effects.clear();
            sync(entity, effects);
        }
    }

    public static void clear(LivingEntity entity, Effect effect) {
        if (entity.level().isClientSide || !has(entity, effect)) {
            return;
        }

        List<StatusEffect> effects = get(entity);
        Iterator<StatusEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().effect == effect) {
                iterator.remove();
            }
        }
        effects.sort(null);
        sync(entity, effects);
    }

    public static List<LivingEntity> getTargets(LivingEntity caster, Effect effect, int amplifier) {
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : Lightsabers.proxy.getLoadedEntities(caster.level())) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            StatusEffect status = get(livingEntity, effect);
            if (status != null
                    && (amplifier < 0 || amplifier == status.amplifier)
                    && caster.getUUID().equals(status.casterUUID)) {
                targets.add(livingEntity);
            }
        }
        return targets;
    }

    public static List<LivingEntity> getTargets(LivingEntity caster, Effect effect) {
        return getTargets(caster, effect, -1);
    }

    private static void sync(LivingEntity entity, List<StatusEffect> effects) {
        if (!entity.level().isClientSide) {
            ALNetworkManager.sendToTrackingAndSelf(entity, new MessageUpdateEffects(entity, effects));
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Effect[id=%s,d=%s,a=%s]", effect, duration, amplifier);
    }

    @Override
    public int compareTo(StatusEffect other) {
        return Short.compare(other.duration, duration);
    }
}
