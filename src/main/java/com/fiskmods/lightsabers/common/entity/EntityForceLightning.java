package com.fiskmods.lightsabers.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class EntityForceLightning extends Entity {
    private static final EntityDataAccessor<Integer> CASTER_ID =
            SynchedEntityData.defineId(
                    EntityForceLightning.class,
                    EntityDataSerializers.INT
            );
    private static final int LIFETIME_TICKS = 2;
    private static final String CASTER_ID_TAG = "CasterId";

    public EntityForceLightning(EntityType<? extends EntityForceLightning> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public EntityForceLightning(Level level, LivingEntity caster) {
        this(ModEntities.FORCE_LIGHTNING.get(), level);
        setCaster(caster);
        moveToCaster(caster);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CASTER_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity caster = getCaster();
        if (tickCount > LIFETIME_TICKS || caster == null || !caster.isAlive()) {
            discard();
            return;
        }
        moveToCaster(caster);
    }

    public void setCaster(LivingEntity caster) {
        entityData.set(CASTER_ID, caster.getId());
    }

    @Nullable
    public LivingEntity getCaster() {
        Entity caster = level().getEntity(entityData.get(CASTER_ID));
        return caster instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(CASTER_ID, tag.getInt(CASTER_ID_TAG));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(CASTER_ID_TAG, entityData.get(CASTER_ID));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    private void moveToCaster(LivingEntity caster) {
        setPos(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());
    }
}
