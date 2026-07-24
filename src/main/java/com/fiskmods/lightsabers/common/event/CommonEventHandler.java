package com.fiskmods.lightsabers.common.event;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.container.ContainerCrystalPouch;
import com.fiskmods.lightsabers.common.container.InventoryCrystalPouch;
import com.fiskmods.lightsabers.common.damage.ALDamageSources;
import com.fiskmods.lightsabers.common.damage.ALDamageTypes;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.ALPlayerData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import com.fiskmods.lightsabers.common.force.PowerType;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectActive;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectChoke;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectDrain;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectFortify;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectMeditation;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectResist;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.network.ALNetworkManager;
import com.fiskmods.lightsabers.common.network.MessageBroadcastState;
import com.fiskmods.lightsabers.common.network.MessagePlayerJoin;
import com.fiskmods.lightsabers.common.network.MessageUpdateEffects;
import fiskfille.utils.helper.FiskServerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CommonEventHandler {
    private static final int STATUS_DAMAGE_INTERVAL = 5;
    private static final double SPINNING_DEFLECT_DISTANCE = 3.0D;
    private static final double SPINNING_DEFLECT_RADIUS = 1.25D;
    private static final double SPINNING_DEFLECT_MIN_SPEED = 1.0D;

    private static final ResourceLocation STUN_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    Lightsabers.MODID,
                    "stun_movement_lock"
            );
    private static final ResourceLocation FORCE_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(
                    Lightsabers.MODID,
                    "force_speed_boost"
            );
    private static final AttributeModifier STUN_SPEED_MODIFIER = new AttributeModifier(
            STUN_SPEED_MODIFIER_ID,
            -1.0D,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );
    private static final AttributeModifier FORCE_SPEED_MODIFIER = new AttributeModifier(
            FORCE_SPEED_MODIFIER_ID,
            1.0D,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
    );

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerData(player);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerData(player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerData(player);
        }
    }

    private static void syncPlayerData(ServerPlayer player) {
        ALNetworkManager.sendToPlayer(player, new MessagePlayerJoin(player));
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)
                || !(event.getTarget() instanceof Player trackedPlayer)) {
            return;
        }
        ALNetworkManager.sendToPlayer(tracker, new MessageBroadcastState(trackedPlayer));
        if (trackedPlayer instanceof ServerPlayer trackedServerPlayer) {
            ALNetworkManager.sendToPlayer(
                    trackedServerPlayer,
                    new MessageBroadcastState(tracker)
            );
        }
    }

    @SubscribeEvent
    public void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!ALPlayerData.hasData(player)) {
            return;
        }
        ALData.onUpdate(player);
    }

    @SubscribeEvent
    public void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        LogicalSide side = player.level().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
        if (side == LogicalSide.SERVER) {
            handleSpinningLightsaber(player);
        }
        if (!ALPlayerData.hasData(player)) {
            return;
        }

        validateSelectedPowers(player);
        updatePowerUsage(player, side);
        updateInterpolationTimers(player);
        updateDrain(player);
        updateChoke(player);
        updateArmTimers(player);
        updateActivePowerTransition(player, side);
        updateExperienceRewards(player);
        ensureBasePowersUnlocked(player);

        tickLivingEntityEffects(player);

        int forceMax = ALData.POWERS.get(player).getForceMax();
        ALDataInterp.FORCE_POWER.clampWithoutNotify(player, 0.0F, (float) forceMax);
        ALDataInterp.FORCE_POWER_DIFF.clampWithoutNotify(player, 0.0F, (float) forceMax);
        ALData.PREV_XP.setWithoutNotify(player, player.totalExperience);
        ALData.PREV_USING_POWER.setWithoutNotify(player, ALData.USING_POWER.get(player));
        ALDataInterp.RIGHT_ARM_TIMER.clampWithoutNotify(player, 0.0F, 1.0F);
        ALDataInterp.LEFT_ARM_TIMER.clampWithoutNotify(player, 0.0F, 1.0F);
    }

    private static void handleSpinningLightsaber(Player player) {
        if (!player.isUsingItem()) {
            return;
        }

        ItemStack stack = player.getUseItem();
        if (!ItemLightsaberBase.isActive(stack)
                || !ItemLightsaberBase.isSpinningLightsaber(stack)) {
            return;
        }

        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle().normalize();
        AABB searchArea = player.getBoundingBox().inflate(SPINNING_DEFLECT_DISTANCE);
        for (Projectile projectile : player.level().getEntitiesOfClass(
                Projectile.class,
                searchArea,
                Entity::isAlive
        )) {
            Vec3 offset = projectile.position().subtract(origin);
            double forwardDistance = offset.dot(direction);
            if (forwardDistance <= 0.0D || forwardDistance > SPINNING_DEFLECT_DISTANCE) {
                continue;
            }

            Vec3 perpendicular = offset.subtract(direction.scale(forwardDistance));
            if (perpendicular.lengthSqr() > SPINNING_DEFLECT_RADIUS * SPINNING_DEFLECT_RADIUS) {
                continue;
            }

            double speed = Math.max(projectile.getDeltaMovement().length(), SPINNING_DEFLECT_MIN_SPEED);
            projectile.setOwner(player);
            projectile.setDeltaMovement(direction.scale(speed));
            projectile.hurtMarked = true;
        }
    }

    @SubscribeEvent
    public void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)
                || entity instanceof Player) {
            return;
        }
        tickLivingEntityEffects(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getDistance() <= 3.0F) {
            return;
        }

        float force = ALDataInterp.FORCE_POWER.get(player);
        if (force <= 0 || !PowerManager.hasPowerUnlocked(player, Power.REBOUND)) {
            return;
        }

        float cost = Power.REBOUND.getUseCost(player);
        float amount = Math.min(
                Math.max(event.getDistance() - 3.0F, 0.0F),
                Mth.floor(force / cost)
        );
        if (amount <= 0) {
            return;
        }

        event.setDistance(event.getDistance() - amount);
        if (!player.level().isClientSide) {
            ALDataInterp.FORCE_POWER.incr(player, -cost * amount);
        }
        if (Lightsabers.proxy.isClientPlayer(player)) {
            float soundAmount = amount - 3.0F;
            Lightsabers.proxy.playLocalSound(
                    player,
                    ALSounds.player_force_cast,
                    Math.min(soundAmount / 10.0F + 0.2F, 1.0F),
                    1.0F
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && StatusEffect.has(attacker, Effect.STUN)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            StatusEffect meditation = StatusEffect.get(attacker, Effect.MEDITATION);
            if (meditation != null && FiskServerUtils.isMeleeDamage(event.getSource())) {
                event.setAmount(
                        event.getAmount()
                                * PowerEffectMeditation.getModifierAmount(meditation.amplifier)
                );
            }
        }

        StatusEffect fortify = StatusEffect.get(entity, Effect.FORTIFY);
        if (fortify != null && event.getSource().is(ALDamageTypes.FORCE)) {
            event.setAmount(
                    event.getAmount()
                            / PowerEffectFortify.getModifierAmount(fortify.amplifier)
            );
        }

        StatusEffect resist = StatusEffect.get(entity, Effect.RESIST);
        if (resist != null && event.getSource().is(ALDamageTypes.LIGHTSABER)) {
            event.setAmount(
                    event.getAmount()
                            / PowerEffectResist.getModifierAmount(resist.amplifier)
            );
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ALEntityData entityData = ALEntityData.getDataOrNull(entity);
        if (entityData != null) {
            entityData.activeEffects.clear();
        }
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            ALData.FORCE_XP.set(
                    player,
                    (float) Mth.floor(ALData.FORCE_XP.get(player) * 0.7F)
            );
        } else {
            ALData.FORCE_XP.set(player, 0.0F);
        }
        ALData.onDeath(player);
    }

    @SubscribeEvent
    public void onEntityItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack crystalStack = event.getItemEntity().getItem();
        if (crystalStack.isEmpty()
                || !crystalStack.is(ModBlocks.LIGHTSABER_CRYSTAL_ITEM.get())) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.CRYSTAL_POUCH.get())) {
                continue;
            }

            InventoryCrystalPouch pouch = new InventoryCrystalPouch(player, slot);
            if (player.containerMenu instanceof ContainerCrystalPouch container
                    && container.inventory.itemSlot == slot) {
                pouch = container.inventory;
            }
            pouch.addItemStackToInventory(crystalStack);
            pouch.setChanged();
            if (crystalStack.isEmpty()) {
                break;
            }
        }
    }

    private static void validateSelectedPowers(Player player) {
        List<Power> selectedPowers = ALData.SELECTED_POWERS.get(player);
        for (int index = 0; index < selectedPowers.size(); index++) {
            Power power = selectedPowers.get(index);
            if (power == null) {
                continue;
            }

            Power unlockedChild;
            while ((unlockedChild = getUnlockedChild(player, power)) != null) {
                power = unlockedChild;
            }
            while (power != null && !PowerManager.hasPowerUnlocked(player, power)) {
                power = power.parent;
            }
            selectedPowers.set(index, power);
        }
    }

    private static Power getUnlockedChild(Player player, Power power) {
        for (Power child : power.children) {
            if (PowerManager.hasPowerUnlocked(player, child)) {
                return child;
            }
        }
        return null;
    }

    private static void updatePowerUsage(Player player, LogicalSide side) {
        if (ALData.USING_POWER.get(player)) {
            ALData.TICKS_USING_POWER.incrWithoutNotify(player, 1);
        } else {
            ALData.TICKS_USING_POWER.setWithoutNotify(player, 0);
        }

        if (!player.isAlive()) {
            return;
        }
        int forceMax = ALData.POWERS.get(player).getForceMax();
        if (forceMax <= 0) {
            return;
        }

        if (ALData.USE_POWER_COOLDOWN.get(player) > 0) {
            ALData.USE_POWER_COOLDOWN.incrWithoutNotify(player, -1);
        }

        Power power = PowerManager.getSelectedPower(player);
        if (power != null
                && ALData.USING_POWER.get(player)
                && power.powerStats.powerType == PowerType.PER_SECOND
                && ALDataInterp.FORCE_POWER.get(player) >= power.getUseCost(player)
                && power.powerEffect.execute(player, side)) {
            ALDataInterp.FORCE_POWER.incrWithoutNotify(player, -power.getUseCost(player) / 20.0F);
        } else if (ALDataInterp.FORCE_POWER_DIFF.get(player) <= ALDataInterp.FORCE_POWER.get(player)) {
            ALDataInterp.FORCE_POWER.incrWithoutNotify(
                    player,
                    ALData.POWERS.get(player).getRegen() / 20.0F
            );
        }
        ALDataInterp.FORCE_POWER.clampWithoutNotify(player, 0.0F, (float) forceMax);
    }

    private static void updateInterpolationTimers(Player player) {
        if (ALDataInterp.FORCE_POWER_DIFF.get(player) > ALDataInterp.FORCE_POWER.get(player)) {
            ALDataInterp.FORCE_POWER_DIFF.incrWithoutNotify(player, -7.5F);
            ALDataInterp.FORCE_POWER_DIFF.clampWithoutNotify(
                    player,
                    0.0F,
                    ALDataInterp.FORCE_POWER_DIFF.get(player)
            );
        } else {
            ALDataInterp.FORCE_POWER_DIFF.setWithoutNotify(
                    player,
                    ALDataInterp.FORCE_POWER.get(player)
            );
        }

        if (ALDataInterp.FORCE_PUSHING_TIMER.get(player) > 0) {
            ALDataInterp.FORCE_PUSHING_TIMER.incrWithoutNotify(player, -0.075F);
            ALDataInterp.FORCE_PUSHING_TIMER.clampWithoutNotify(player, 0.0F, 1.0F);
        } else {
            ALDataInterp.FORCE_PUSHING_TIMER.setWithoutNotify(player, 0.0F);
        }
    }

    private static void updateDrain(Player player) {
        if (ALDataInterp.DRAIN_LIFE_TIMER.get(player) <= 0) {
            ALDataInterp.DRAIN_LIFE_TIMER.setWithoutNotify(player, 0.0F);
            return;
        }

        ALDataInterp.DRAIN_LIFE_TIMER.incrWithoutNotify(player, -1.0F / PowerEffectDrain.DURATION);
        ALDataInterp.DRAIN_LIFE_TIMER.clampWithoutNotify(player, 0.0F, 1.0F);
        for (LivingEntity target : StatusEffect.getTargets(player, Effect.DRAIN)) {
            StatusEffect effect = StatusEffect.get(target, Effect.DRAIN);
            if (effect == null) {
                continue;
            }
            if (!target.level().isClientSide && isDamageTick(effect)) {
                float damage = PowerEffectDrain.getAbsorbAmount(effect.amplifier)
                        * (float) STATUS_DAMAGE_INTERVAL / PowerEffectDrain.DURATION;
                target.invulnerableTime = 0;
                float previousHealth = target.getHealth();
                target.hurt(ALDamageSources.causeForceLightningDamage(player), damage);
                player.heal(Math.max(previousHealth - target.getHealth(), 0));
            }
            target.setDeltaMovement(0, 0.05D, 0);
            target.hurtMarked = true;
        }
    }

    private static void updateChoke(Player player) {
        for (LivingEntity target : StatusEffect.getTargets(player, Effect.CHOKE)) {
            StatusEffect effect = StatusEffect.get(target, Effect.CHOKE);
            if (effect == null) {
                continue;
            }
            if (!target.level().isClientSide && isDamageTick(effect)) {
                float damage = PowerEffectChoke.getDamagePerSecond(effect.amplifier)
                        * STATUS_DAMAGE_INTERVAL / 20.0F;
                target.invulnerableTime = 0;
                target.hurt(ALDamageSources.causeForceDamage(player), damage);
            }
            target.setDeltaMovement(0, 0.001D * effect.duration, 0);
            target.hurtMarked = true;
        }
    }

    // Targets tick before the caster samples them and effects are removed the moment
    // duration hits 0, so anchoring at INTERVAL - 1 is the only phase where every
    // damage tick of the effect's lifetime is observable.
    private static boolean isDamageTick(StatusEffect effect) {
        return effect.duration % STATUS_DAMAGE_INTERVAL == STATUS_DAMAGE_INTERVAL - 1;
    }

    private static void updateArmTimers(Player player) {
        boolean lightning = StatusEffect.get(player, Effect.LIGHTNING) != null;
        boolean chokeRight = !StatusEffect.getTargets(player, Effect.CHOKE).isEmpty();
        boolean chokeLeft = !StatusEffect.getTargets(player, Effect.CHOKE, 2).isEmpty();
        ALDataInterp.RIGHT_ARM_TIMER.incrWithoutNotify(
                player,
                lightning || chokeRight ? 0.33F : -0.33F
        );
        ALDataInterp.LEFT_ARM_TIMER.incrWithoutNotify(
                player,
                lightning || chokeLeft ? 0.33F : -0.33F
        );
    }

    private static void updateActivePowerTransition(Player player, LogicalSide side) {
        Power power = PowerManager.getSelectedPower(player);
        if (power == null || !(power.powerEffect instanceof PowerEffectActive activeEffect)) {
            return;
        }

        boolean using = ALData.USING_POWER.get(player);
        boolean previouslyUsing = ALData.PREV_USING_POWER.get(player);
        if (using && !previouslyUsing) {
            activeEffect.start(player, side);
        } else if (!using && previouslyUsing) {
            activeEffect.stop(player, side);
        }
    }

    private static void updateExperienceRewards(Player player) {
        int previousXp = ALData.PREV_XP.get(player);
        if (previousXp < player.totalExperience) {
            ALData.FORCE_XP.incrWithoutNotify(
                    player,
                    (player.totalExperience - previousXp) / 2.0F
            );
        }
        if (ALData.BASE_POWER.get(player) < 0) {
            ALData.BASE_POWER.setWithoutNotify(
                    player,
                    (byte) Math.max(ALData.POWERS.get(player).getBasePower(), 0)
            );
        }
    }

    private static void ensureBasePowersUnlocked(Player player) {
        if (!PowerManager.hasPowerUnlocked(player, Power.FORCE_SENSITIVITY)) {
            return;
        }
        PowerManager.getPowerData(player, Power.LIGHT_SIDE).setUnlocked(player, true);
        PowerManager.getPowerData(player, Power.DARK_SIDE).setUnlocked(player, true);
        PowerManager.getPowerData(player, Power.NEUTRAL).setUnlocked(player, true);
    }

    private static void handleForcePushCollision(LivingEntity entity, ALEntityData data) {
        if (!data.forcePushed || !entity.horizontalCollision || entity.onGround()) {
            return;
        }
        data.forcePushed = false;
        entity.invulnerableTime = 0;
        Vec3 movement = new Vec3(
                entity.xo - entity.getX(),
                entity.yo - entity.getY(),
                entity.zo - entity.getZ()
        );
        float damage = (float) Math.max(movement.length() * 5.0D - 3.0D, 0.0D);
        if (damage > 0 && !entity.level().isClientSide) {
            entity.hurt(ALDamageSources.causeIntoWallDamage(entity), damage);
        }
    }

    private static void tickLivingEntityEffects(LivingEntity entity) {
        if (!ALEntityData.hasData(entity)) {
            return;
        }
        ALEntityData data = ALEntityData.getData(entity);
        handleForcePushCollision(entity, data);
        tickStatusEffects(entity, data);
        applyMovementEffects(entity);
    }

    private static void tickStatusEffects(LivingEntity entity, ALEntityData data) {
        List<Integer> expiredChokeAmplifiers = new ArrayList<>();
        boolean changed = false;
        Iterator<StatusEffect> iterator = data.activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect status = iterator.next();
            status.duration--;
            if (status.duration > 0) {
                continue;
            }
            iterator.remove();
            changed = true;
            if (status.effect == Effect.CHOKE) {
                expiredChokeAmplifiers.add((int) status.amplifier);
            }
        }

        for (int amplifier : expiredChokeAmplifiers) {
            StatusEffect.add(
                    entity,
                    Effect.STUN,
                    (int) (PowerEffectChoke.getStunDuration(amplifier) * 20),
                    0
            );
        }
        if (changed && !entity.level().isClientSide) {
            ALNetworkManager.sendToTrackingAndSelf(
                    entity,
                    new MessageUpdateEffects(entity, data.activeEffects)
            );
        }
    }

    private static void applyMovementEffects(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(STUN_SPEED_MODIFIER_ID);
        speed.removeModifier(FORCE_SPEED_MODIFIER_ID);

        if (StatusEffect.has(entity, Effect.STUN)) {
            speed.addTransientModifier(STUN_SPEED_MODIFIER);
            entity.hasImpulse = false;
            Vec3 movement = entity.getDeltaMovement();
            entity.setDeltaMovement(movement.x, -0.2D, movement.z);
            entity.setJumping(false);
        }
        if (StatusEffect.has(entity, Effect.SPEED)) {
            speed.addTransientModifier(FORCE_SPEED_MODIFIER);
        }
    }
}
