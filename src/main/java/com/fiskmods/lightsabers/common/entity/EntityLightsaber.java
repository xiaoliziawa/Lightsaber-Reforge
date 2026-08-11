package com.fiskmods.lightsabers.common.entity;

import com.fiskmods.lightsabers.common.damage.ALDamageSources;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.sound.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityLightsaber extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> RETURNING =
            SynchedEntityData.defineId(EntityLightsaber.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AMPLIFIER =
            SynchedEntityData.defineId(EntityLightsaber.class, EntityDataSerializers.INT);

    private static final int BASE_RETURN_DELAY_TICKS = 20;
    private static final int MAX_THROW_AMPLIFIER = 1;
    private static final int SWING_SOUND_INTERVAL = 3;
    private static final double CATCH_DISTANCE = 2.0D;
    private static final double RETURN_SPEED = 2.0D;
    private static final double BLOCK_BOUNCE_OFFSET = 0.3D;

    public EntityLightsaber(EntityType<? extends EntityLightsaber> entityType, Level level) {
        super(entityType, level);
    }

    public EntityLightsaber(
            Level level,
            LivingEntity thrower,
            ItemStack stack,
            int amplifier
    ) {
        super(ModEntities.LIGHTSABER.get(), thrower, level, stack);
        setAmplifier(amplifier);
        shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0, 2.0F, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RETURNING, false);
        builder.define(AMPLIFIER, 0);
    }

    public boolean isReturning() {
        return entityData.get(RETURNING);
    }

    public void setReturning(boolean returning) {
        entityData.set(RETURNING, returning);
    }

    public int getAmplifier() {
        return entityData.get(AMPLIFIER);
    }

    public void setAmplifier(int amplifier) {
        entityData.set(AMPLIFIER, amplifier);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float divisor = Math.max(2 - getAmplifier(), 1);
        return EntityDimensions.scalable(2.0F / divisor, 0.125F);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.STICK;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            Entity owner = getOwner();
            if (!(owner instanceof LivingEntity thrower) || !thrower.isAlive()) {
                dropLightsaber(this);
                discard();
                return;
            }

            if (tickCount % SWING_SOUND_INTERVAL == 0) {
                level().playSound(
                        null,
                        getX(),
                        getY(),
                        getZ(),
                        thrower instanceof Player
                                ? ModSounds.PLAYER_LIGHTSABER_SWING.get()
                                : ModSounds.MOB_LIGHTSABER_SWING.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }

            if (tickCount > getReturnDelayTicks()) {
                setReturning(true);
            }

            if (isReturning()) {
                if (distanceTo(thrower) <= CATCH_DISTANCE) {
                    returnToThrower(thrower);
                    discard();
                    return;
                }

                Vec3 target = thrower.position().add(0, thrower.getBbHeight() * 0.6D, 0);
                setDeltaMovement(target.subtract(position()).normalize().scale(RETURN_SPEED));
            }
        }

        if (isReturning()) {
            tickReturning();
        } else {
            super.tick();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target != getOwner() && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity owner = getOwner();
        Entity target = hitResult.getEntity();
        if (owner == null || target == owner) {
            return;
        }

        ItemStack stack = getItem();
        if (stack.getItem() instanceof ItemLightsaberBase lightsaber) {
            target.hurt(
                    ALDamageSources.causeLightsaberDamage(owner),
                    lightsaber.getAttackDamage(stack)
            );
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (isReturning() && hitResult.getType() == HitResult.Type.BLOCK) {
            return;
        }
        super.onHit(hitResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        setReturning(true);
        Vec3 surface = hitResult.getLocation().add(
                new Vec3(
                        hitResult.getDirection().getStepX(),
                        hitResult.getDirection().getStepY(),
                        hitResult.getDirection().getStepZ()
                ).scale(BLOCK_BOUNCE_OFFSET)
        );
        setPos(surface.x, surface.y, surface.z);
        setDeltaMovement(Vec3.ZERO);
    }

    private void tickReturning() {
        Vec3 movement = getDeltaMovement();
        Vec3 start = position();
        Vec3 end = start.add(movement);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level(),
                this,
                start,
                end,
                getBoundingBox().expandTowards(movement).inflate(1.0D),
                this::canHitEntity
        );

        if (entityHit != null) {
            Vec3 hitLocation = entityHit.getLocation();
            setPos(hitLocation.x, hitLocation.y, hitLocation.z);
        } else {
            setPos(end.x, end.y, end.z);
        }

        updateRotation();
        applyEffectsFromBlocks();
        checkLeftOwner();
        baseTick();
        if (entityHit != null && isAlive()) {
            hitTargetOrDeflectSelf(entityHit);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Returning", isReturning());
        output.putInt("Amplifier", getAmplifier());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setReturning(input.getBooleanOr("Returning", false));
        setAmplifier(input.getIntOr("Amplifier", 0));
    }

    private void returnToThrower(LivingEntity thrower) {
        ItemStack stack = getItem().copy();
        if (thrower instanceof Player player) {
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            } else if (!player.getInventory().add(stack)) {
                dropLightsaber(player);
            }
        } else if (thrower.getMainHandItem().isEmpty()) {
            thrower.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else {
            dropLightsaber(thrower);
        }
    }

    private int getReturnDelayTicks() {
        int throwLevel = Math.min(Math.max(getAmplifier(), 0), MAX_THROW_AMPLIFIER) + 1;
        return BASE_RETURN_DELAY_TICKS * throwLevel;
    }

    private void dropLightsaber(Entity location) {
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            level().addFreshEntity(new ItemEntity(
                    level(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    stack.copy()
            ));
        }
    }
}
