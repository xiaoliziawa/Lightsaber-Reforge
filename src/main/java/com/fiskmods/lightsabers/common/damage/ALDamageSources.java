package com.fiskmods.lightsabers.common.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class ALDamageSources {
    private ALDamageSources() {
    }

    public static DamageSource causeIntoWallDamage(Entity entity) {
        return create(entity.level(), ALDamageTypes.INTO_WALL, null);
    }

    public static DamageSource causeLightsaberDamage(Entity entity) {
        return create(entity.level(), ALDamageTypes.LIGHTSABER, entity);
    }

    public static DamageSource causeForceDamage(Entity entity) {
        return create(entity.level(), ALDamageTypes.FORCE, entity);
    }

    public static DamageSource causeForceLightningDamage(Entity entity) {
        return create(entity.level(), ALDamageTypes.LIGHTNING, entity);
    }

    private static DamageSource create(
            Level level,
            ResourceKey<DamageType> damageType,
            Entity causingEntity
    ) {
        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(damageType);
        return causingEntity == null ? new DamageSource(holder) : new DamageSource(holder, causingEntity);
    }
}
