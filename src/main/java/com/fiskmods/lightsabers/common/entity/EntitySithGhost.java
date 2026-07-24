package com.fiskmods.lightsabers.common.entity;

import com.fiskmods.lightsabers.common.entity.ai.EntityAIBreakBlock;
import com.fiskmods.lightsabers.common.entity.ai.EntityAIRest;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.sound.ModSounds;
import com.fiskmods.lightsabers.common.tileentity.TileEntitySithStoneCoffin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class EntitySithGhost extends Monster {
    private static final byte SMOKE_EVENT = 62;
    private static final int THROW_MIN_COOLDOWN = 40;
    private static final int THROW_RANDOM_COOLDOWN = 60;
    private static final int STRAFE_MIN_DURATION = 100;
    private static final int STRAFE_RANDOM_DURATION = 1000;

    private boolean hasRestingPlace;
    private BlockPos restingPlace = BlockPos.ZERO;
    private int throwLightsaberCooldown;
    private int swingItemCooldown;
    private int strafeTimer;
    private int strafeDirection = 1;
    private int taskFinished;

    public EntitySithGhost(EntityType<? extends EntitySithGhost> entityType, Level level) {
        super(entityType, level);
        getNavigation().setCanFloat(true);
        if (getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
            navigation.setCanPassDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new EntityAIBreakBlock(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.5D, true));
        goalSelector.addGoal(2, new EntityAIRest(this, 0.4D));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 4.0F, 0.1F));
        goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.6D));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(this, Player.class, 0, false, false, null)
        );
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (entity instanceof EntitySithGhost) {
            return false;
        }
        boolean hurt = super.doHurtTarget(entity);
        if (hurt) {
            swingMainHand();
        }
        return hurt;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (damageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(damageSource, Math.min(damage, 10.0F));
    }

    private void swingMainHand() {
        if (swingItemCooldown == 0) {
            swingItemCooldown = 5;
            swing(InteractionHand.MAIN_HAND);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SITH_GHOST_IDLE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SITH_GHOST_DEATH.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }

        LivingEntity target = getTarget();
        ItemStack heldItem = getMainHandItem();
        if (throwLightsaberCooldown > 0) {
            throwLightsaberCooldown--;
        }
        if (swingItemCooldown > 0) {
            swingItemCooldown--;
        }
        if (--strafeTimer <= 0) {
            strafeDirection *= -1;
            strafeTimer = STRAFE_MIN_DURATION + getRandom().nextInt(STRAFE_RANDOM_DURATION);
        }

        if (!heldItem.isEmpty()) {
            if (tickCount > 5 && !ItemLightsaberBase.isActive(heldItem)) {
                ItemLightsaberBase.ignite(this, true);
            }
            handleLightsaberCombat(target, heldItem);
        }

        if (target != null) {
            taskFinished = 1;
        } else if (taskFinished == 1) {
            taskFinished = 2;
        }
        if (taskFinished == 2) {
            tryReturnToCoffin();
        }
    }

    private void handleLightsaberCombat(
            @Nullable LivingEntity target,
            ItemStack heldItem
    ) {
        if (target == null || !target.isAlive()) {
            return;
        }

        getLookControl().setLookAt(target, 100.0F, 100.0F);
        double distance = distanceTo(target);
        if (ItemLightsaberBase.isActive(heldItem)
                && distance > 5.0D
                && hasLineOfSight(target)
                && throwLightsaberCooldown == 0) {
            throwLightsaberCooldown = THROW_MIN_COOLDOWN
                    + getRandom().nextInt(THROW_RANDOM_COOLDOWN);
            swingMainHand();
            ItemLightsaberBase.throwLightsaber(this, heldItem, 1);
            return;
        }
        if (distance < 5.0D && hasLineOfSight(target)) {
            getMoveControl().strafe(0.0F, 0.3F * strafeDirection);
        }
    }

    private void tryReturnToCoffin() {
        if (!hasRestingPlace || distanceToSqr(restingPlace.getCenter()) > 4.0D) {
            return;
        }
        taskFinished = 3;
        if (level() instanceof ServerLevel serverLevel
                && serverLevel.getBlockEntity(restingPlace)
                instanceof TileEntitySithStoneCoffin coffin) {
            ItemStack returnedEquipment = getMainHandItem().copy();
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            coffin.restoreFromGhost(returnedEquipment);
            serverLevel.levelEvent(null, 1017, blockPosition(), 0);
            spawnSmokeAndDiscard();
        }
    }

    private void spawnSmokeAndDiscard() {
        level().broadcastEntityEvent(this, SMOKE_EVENT);
        discard();
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == SMOKE_EVENT) {
            spawnSmokeParticles();
            return;
        }
        super.handleEntityEvent(eventId);
    }

    private void spawnSmokeParticles() {
        RandomSource random = getRandom();
        for (int i = 0; i < 128; i++) {
            double offsetX = (random.nextFloat() * 2.0F - 1.0F) * 1.2D;
            double offsetY = random.nextFloat() * 2.4D - 1.0D;
            double offsetZ = (random.nextFloat() * 2.0F - 1.0F) * 1.2D;
            level().addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    getX() + offsetX * getBbWidth(),
                    getBoundingBox().minY + offsetY * getBbHeight(),
                    getZ() + offsetZ * getBbWidth(),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData
    ) {
        SpawnGroupData result = super.finalizeSpawn(
                level,
                difficulty,
                reason,
                spawnData
        );
        if (getMainHandItem().isEmpty()) {
            Random random = new Random(getRandom().nextLong());
            setItemSlot(
                    EquipmentSlot.MAINHAND,
                    LightsaberData.createRandom(random, CrystalColor.RED)
            );
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HAND || slot.isArmor()) {
                setDropChance(slot, 0.0F);
            }
        }
        return result;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!isDeadOrDying()) {
            level().broadcastEntityEvent(this, SMOKE_EVENT);
        }
        super.die(damageSource);
    }

    public void setRestingPlace(BlockPos pos) {
        restingPlace = pos.immutable();
        hasRestingPlace = true;
    }

    public boolean hasRestingPlace() {
        return hasRestingPlace;
    }

    public boolean isReturningToRestingPlace() {
        return taskFinished == 2;
    }

    public int getRestingPlaceX() {
        return restingPlace.getX();
    }

    public int getRestingPlaceY() {
        return restingPlace.getY();
    }

    public int getRestingPlaceZ() {
        return restingPlace.getZ();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        hasRestingPlace = tag.getBoolean("HasRestingPlace");
        restingPlace = new BlockPos(
                tag.getInt("RestX"),
                tag.getInt("RestY"),
                tag.getInt("RestZ")
        );
        throwLightsaberCooldown = tag.getInt("ThrowCooldown");
        swingItemCooldown = tag.getInt("SwingCooldown");
        strafeTimer = tag.getInt("StrafeTimer");
        strafeDirection = tag.getInt("Strafe");
        taskFinished = tag.getInt("TaskFinished");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HasRestingPlace", hasRestingPlace);
        tag.putInt("RestX", restingPlace.getX());
        tag.putInt("RestY", restingPlace.getY());
        tag.putInt("RestZ", restingPlace.getZ());
        tag.putInt("ThrowCooldown", throwLightsaberCooldown);
        tag.putInt("SwingCooldown", swingItemCooldown);
        tag.putInt("StrafeTimer", strafeTimer);
        tag.putInt("Strafe", strafeDirection);
        tag.putInt("TaskFinished", taskFinished);
    }
}
