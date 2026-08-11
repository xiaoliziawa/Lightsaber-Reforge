package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.damage.ALDamageSources;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;

public class PowerEffectLightning extends PowerEffectStatus {
    private static final double TARGET_RANGE = 7.0D;

    public PowerEffectLightning(int amplifier) {
        super(Effect.LIGHTNING, amplifier);
    }

    @Override
    public String[] getDesc() {
        return new String[] {
                PowerDesc.create(
                        "effect",
                        PowerDesc.format(
                                "%s %s%s",
                                4 + amplifier * 2,
                                Unit.DAMAGE,
                                ChatFormatting.GRAY + "/"
                        ),
                        Target.TARGET
                )
        };
    }

    @Override
    public void start(Player player, LogicalSide side) {
        if (side == LogicalSide.CLIENT) {
            Lightsabers.proxy.playLightningSound(player);
        }
    }

    @Override
    public boolean execute(Player player, LogicalSide side) {
        boolean active = super.execute(player, side);
        if (!active) {
            return false;
        }

        LivingEntity target = ForceTargeting.findLookTarget(player, TARGET_RANGE);
        if (target != null) {
            if (!target.level().isClientSide()) {
                target.hurt(
                        ALDamageSources.causeForceLightningDamage(player),
                        4 + amplifier * 2
                );
            }
            Vec3 movement = target.getDeltaMovement();
            target.setDeltaMovement(0, Math.min(movement.y, 0), 0);
            target.hurtMarked = true;
        }
        return true;
    }
}
