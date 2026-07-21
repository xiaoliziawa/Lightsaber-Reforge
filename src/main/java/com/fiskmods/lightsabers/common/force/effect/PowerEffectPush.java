package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.common.damage.ALDamageSources;
import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import fiskfille.utils.helper.VectorHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;

public class PowerEffectPush extends PowerEffect {
    private static final double RANGE = 16.0D;

    public PowerEffectPush(int amplifier) {
        super(amplifier);
    }

    @Override
    public boolean execute(Player player, LogicalSide side) {
        LivingEntity target = ForceTargeting.findLookTarget(player, RANGE);
        if (target == null) {
            return false;
        }

        if (!target.level().isClientSide) {
            target.hurt(ALDamageSources.causeForceDamage(player), getDamage(amplifier));
            ALEntityData.getData(target).forcePushed = true;
        }

        Vec3 origin = VectorHelper.getOffsetCoords(player, 0, 0, 0);
        Vec3 knockbackPoint = VectorHelper.getOffsetCoords(
                player,
                0,
                0,
                0.5F * getKnockback(amplifier)
        );
        target.setDeltaMovement(target.getDeltaMovement().add(knockbackPoint.subtract(origin)));
        target.hurtMarked = true;
        ALDataInterp.FORCE_PUSHING_TIMER.setWithoutNotify(player, 1.0F);
        return true;
    }

    @Override
    public String[] getDesc() {
        return new String[] {
                PowerDesc.create(
                        "effect2",
                        PowerDesc.format("+%s %s", getKnockback(amplifier), Unit.KNOCKBACK),
                        Target.TARGET
                ),
                PowerDesc.create(
                        "effect2",
                        PowerDesc.format("%s %s", getDamage(amplifier), Unit.DAMAGE),
                        Target.TARGET
                )
        };
    }

    public static float getKnockback(int amplifier) {
        int amount = 1;
        for (int i = 0; i < amplifier; i++) {
            amount *= 2;
        }
        return 3 + amount;
    }

    public static float getDamage(int amplifier) {
        float damage = 1;
        for (int i = 0; i < amplifier; i++) {
            damage *= damage + 0.5F;
        }
        return damage;
    }
}
