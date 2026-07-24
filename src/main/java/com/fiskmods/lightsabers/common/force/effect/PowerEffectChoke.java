package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class PowerEffectChoke extends PowerEffect {
    public static final int DURATION = 60;

    private static final double RANGE = 16.0D;

    public PowerEffectChoke(int amplifier) {
        super(amplifier);
    }

    public static int getDamagePerSecond(int amplifier) {
        return 2 + amplifier * 2;
    }

    @Override
    public boolean execute(Player player, LogicalSide side) {
        LivingEntity target = ForceTargeting.findLookTarget(player, RANGE);
        if (target == null) {
            return false;
        }
        StatusEffect.add(target, player, Effect.CHOKE, DURATION, amplifier);
        return true;
    }

    @Override
    public String[] getDesc() {
        return new String[] {
                PowerDesc.create(
                        "effect",
                        PowerDesc.format(
                                "%s %s%s",
                                getDamagePerSecond(amplifier),
                                Unit.DAMAGE,
                                ChatFormatting.GRAY + "/"
                        ),
                        Target.TARGET
                ),
                PowerDesc.create(
                        "effect",
                        PowerDesc.format(
                                "%s %s%s",
                                Effect.STUN,
                                ChatFormatting.GRAY,
                                getStunDuration(amplifier)
                        ),
                        Target.TARGET
                )
        };
    }

    public static float getStunDuration(int amplifier) {
        float duration = 1.5F;
        if (amplifier > 0) {
            duration += 1 + (amplifier - 1) * 0.5F;
        }
        return duration;
    }
}
