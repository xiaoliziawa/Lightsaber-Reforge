package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import com.fiskmods.lightsabers.helper.ALHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;

public class PowerEffectDrain extends PowerEffect {
    public static final int DURATION = 30;

    private static final double SINGLE_TARGET_RANGE = 5.0D;
    private static final double AREA_RANGE = 7.0D;
    private static final float ADDITIONAL_TARGET_COST = 15.0F;

    public PowerEffectDrain(int amplifier) {
        super(amplifier);
    }

    public static int getAbsorbAmount(int amplifier) {
        return 4 + amplifier * 2;
    }

    @Override
    public boolean execute(Player player, LogicalSide side) {
        List<LivingEntity> targets = getTargets(player);
        for (LivingEntity target : targets) {
            StatusEffect.add(target, player, Effect.DRAIN, DURATION, amplifier);
        }
        if (targets.isEmpty()) {
            return false;
        }
        ALData.DRAIN_LIFE_TIMER.setWithoutNotify(player, 1.0F);
        return true;
    }

    @Override
    public String[] getDesc() {
        return new String[] {
                PowerDesc.create(
                        "absorb",
                        PowerDesc.format("%s %s", getAbsorbAmount(amplifier), Unit.HEALTH),
                        amplifier < 2 ? Target.TARGET : Target.ENEMIES
                )
        };
    }

    public List<LivingEntity> getTargets(Player player) {
        List<LivingEntity> targets = new ArrayList<>();
        if (amplifier < 2) {
            LivingEntity target = ForceTargeting.findLookTarget(player, SINGLE_TARGET_RANGE);
            if (target != null) {
                targets.add(target);
            }
            return targets;
        }

        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(AREA_RANGE),
                entity -> entity != player && !ALHelper.isAlly(player, entity)
        );
        float force = ALData.FORCE_POWER.get(player) + ADDITIONAL_TARGET_COST;
        float baseCost = super.getUseCost(player, Effect.DRAIN.getPower(amplifier));
        for (LivingEntity entity : nearbyEntities) {
            float requiredForce = baseCost
                    + targets.size() * ADDITIONAL_TARGET_COST
                    + ADDITIONAL_TARGET_COST;
            if (force < requiredForce) {
                break;
            }
            targets.add(entity);
        }
        return targets;
    }

    @Override
    public float getUseCost(Player player, Power power) {
        float cost = super.getUseCost(player, power);
        if (amplifier >= 2 && ALData.FORCE_POWER.get(player) >= cost) {
            cost += (getTargets(player).size() - 1) * ADDITIONAL_TARGET_COST;
        }
        return cost;
    }
}
