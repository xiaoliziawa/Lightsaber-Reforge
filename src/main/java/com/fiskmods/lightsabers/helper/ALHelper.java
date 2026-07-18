package com.fiskmods.lightsabers.helper;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.ForceSide;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerData;
import fiskfille.utils.helper.VectorHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public final class ALHelper {
    private ALHelper() {
    }

    public static int getXpBarCap(int level)
    {
        return level >= 30 ? 62 + (level - 30) * 7 : level >= 15 ? 17 + (level - 15) * 3 : 17;
    }

    public static int getTotalXp(Player player)
    {
        int totalXp = Math.round(player.getXpNeededForNextLevel() * player.experienceProgress);

        for (int i = 0; i < player.experienceLevel; ++i)
        {
            totalXp += getXpBarCap(i);
        }

        return totalXp;
    }

    public static int getTotalXpToReachLevel(Player player, int level)
    {
        int i = 0;

        for (int j = 0; j < level; ++j)
        {
            i += getXpBarCap(j + 1);
        }

        return i;
    }

    public static void removeExperience(Player player, int amount)
    {
        if (amount > 0) {
            player.giveExperiencePoints(-Math.min(amount, getTotalXp(player)));
        }
    }

    public static String getConventionalName(String s)
    {
        String s1 = getUnconventionalName(s);
        return s1.substring(0, 1).toLowerCase(Locale.ROOT) + s1.substring(1);
    }

    public static String getUnconventionalName(String s)
    {
        s = s.toLowerCase(Locale.ROOT);

        for (int i = 0; i < s.length(); ++i)
        {
            if (i > 0 && s.charAt(i - 1) == '_' && i < s.length())
            {
                s = s.substring(0, i) + s.substring(i, i + 1).toUpperCase() + s.substring(i + 1);
            }
        }

        s = s.replace(" ", "").replace("'", "").replace("/", "").replace("\\", "").replace("_", "").replace("-", "").replace("(", "").replace(")", "");
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static List<Power> getPowers(ForceSide side)
    {
        List<Power> list = new ArrayList<>();

        for (Power power : Power.POWERS)
        {
            if (side == power.getSide())
            {
                list.add(power);
            }
        }

        return list;
    }

    public static float getCompletion(Player player, ForceSide side)
    {
        int powers = getPowers(side).size();
        float f = 0;

        for (PowerData data : ALData.POWERS.get(player))
        {
            if (data.isUnlocked() && data.power != side.getPower() && side == data.power.getSide())
            {
                f += 1F / (powers - 1);
            }
        }

        return f;
    }

    public static float getForceBalance(Player player)
    {
        float light = getCompletion(player, ForceSide.LIGHT);
        float dark = getCompletion(player, ForceSide.DARK);
        
        if (light != 0 || dark != 0)
        {
            if (light > dark)
            {
                return 1 - dark / light;
            }
            else if (dark > light)
            {
                return -1 + light / dark;
            }
        }
        
        return 0;
    }

    public static byte getBasePower(Player player)
    {
        return ALData.POWERS.get(player).getBasePower();
    }

    public static int getForcePowerMax(Player player)
    {
        return ALData.POWERS.get(player).getForceMax();
    }

    public static float getForcePowerRegen(Player player)
    {
        return ALData.POWERS.get(player).getRegen();
    }

    public static List<PowerData> getRelevantPowers(Player player)
    {
        List<PowerData> powers = new ArrayList<>();
        Predicate<PowerData> isRelevant = ALPredicates.isRelevant(player);
        ALData.POWERS.get(player).forEach(data -> {
            if (isRelevant.test(data)) {
                powers.add(data);
            }
        });
        return powers;
    }
    
    public static Set<Power> getUnlockedChildren(Player player, Power power)
    {
        Set<Power> powers = new HashSet<>();
        power.children.stream().filter(ALPredicates.isUnlocked(player)).forEach(powers::add);
        return powers;
    }

    public static boolean isAlly(LivingEntity to, LivingEntity entity)
    {
        if (entity instanceof TamableAnimal tamable && tamable.getOwner() == to)
        {
            return true;
        }
        else if (entity instanceof Enemy || entity.getLastHurtByMob() == to)
        {
            return false;
        }
        else if (entity.getTeam() != null && entity.isAlliedTo(to))
        {
            return true;
        }
        else if (to.getLastHurtByMob() == entity)
        {
            return to.getLastHurtByMobTimestamp() + 1200 < to.tickCount;
        }

        return to.getVehicle() == entity;
    }

    public static LivingEntity getForceLightningTarget(LivingEntity caster)
    {
        StatusEffect effect = StatusEffect.get(caster, Effect.LIGHTNING);

        if (effect != null)
        {
            Vec3 src = VectorHelper.getOffsetCoords(caster, 0, 0, 0);
            Vec3 dst = VectorHelper.getOffsetCoords(caster, 0, 0, 7);
            HitResult rayTrace = caster.level().clip(new ClipContext(
                    src,
                    dst,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    caster
            ));
            Vec3 hitVec = rayTrace.getType() == HitResult.Type.MISS ? dst : rayTrace.getLocation();
            double distance = caster.distanceToSqr(hitVec.x, hitVec.y, hitVec.z);
            distance = Math.sqrt(distance);

            for (double point = 0; point <= distance; point += 0.15D)
            {
                Vec3 particleVec = VectorHelper.getOffsetCoords(caster, 0, 0, point);

                for (LivingEntity entity : VectorHelper.getEntitiesNear(LivingEntity.class, caster.level(), particleVec, 1))
                {
                    if (entity != caster && caster.getVehicle() != entity)
                    {
                        hitVec = entity.getBoundingBox().getCenter();
                        rayTrace = new EntityHitResult(entity, hitVec);
                        distance = caster.position().distanceTo(hitVec);
                        break;
                    }
                }
            }

            if (rayTrace != null)
            {
                if (rayTrace instanceof EntityHitResult entityHit
                        && entityHit.getEntity() instanceof LivingEntity livingEntity) {
                    return livingEntity;
                }
            }
        }

        return null;
    }

    public static void dropItem(Level level, int x, int y, int z, ItemStack stack, Random random)
    {
        float offsetX = random.nextFloat() * 0.8F + 0.1F;
        float offsetY = random.nextFloat() * 0.8F + 0.1F;
        float offsetZ = random.nextFloat() * 0.8F + 0.1F;

        while (!stack.isEmpty())
        {
            ItemStack droppedStack = stack.split(Math.min(random.nextInt(21) + 10, stack.getCount()));
            ItemEntity entity = new ItemEntity(level, x + offsetX, y + offsetY, z + offsetZ, droppedStack);
            double velocityScale = 0.05D;
            entity.setDeltaMovement(
                    random.nextGaussian() * velocityScale,
                    random.nextGaussian() * velocityScale + 0.2D,
                    random.nextGaussian() * velocityScale
            );
            level.addFreshEntity(entity);
        }
    }
}
