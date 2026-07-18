package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.helper.ALHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

import java.util.List;

public class PowerEffectStun extends PowerEffect {
    private static final double RANGE = 16.0D;
    private static final double AREA_RANGE = 10.0D;

    public final float duration;
    public final int durationInt;
    public final boolean aoe;

    public PowerEffectStun(int amplifier, float duration, boolean aoe) {
        super(amplifier);
        this.duration = duration;
        durationInt = (int) duration * 20;
        this.aoe = aoe;
    }

    @Override
    public boolean execute(Player player, LogicalSide side) {
        if (aoe) {
            List<LivingEntity> targets = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(AREA_RANGE),
                    entity -> entity != player && !ALHelper.isAlly(player, entity)
            );
            for (LivingEntity target : targets) {
                StatusEffect.add(target, Effect.STUN, durationInt, amplifier);
            }
            return true;
        }

        LivingEntity target = ForceTargeting.findLookTarget(player, RANGE);
        if (target == null) {
            return false;
        }
        StatusEffect.add(target, Effect.STUN, durationInt, amplifier);
        return true;
    }

    @Override
    public String[] getDesc() {
        return new String[] {
                PowerDesc.create(
                        "effect",
                        PowerDesc.format("%s %s%s", Effect.STUN, ChatFormatting.GRAY, duration),
                        aoe ? Target.ENEMIES : Target.TARGET
                )
        };
    }
}
