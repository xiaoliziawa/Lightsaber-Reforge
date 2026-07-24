package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class PowerEffectStealth extends PowerEffectStatus {
    public PowerEffectStealth() {
        super(Effect.STEALTH, 0);
    }

    @Override
    public String[] getDesc() {
        return new String[] {PowerDesc.create("effect2", Unit.INVISIBILITY, Target.CASTER)};
    }

    @Override
    public void start(Player player, LogicalSide side) {
        if (side == LogicalSide.CLIENT && Lightsabers.proxy.isClientPlayer(player)) {
            Lightsabers.proxy.playStatusEffectSound(player, Effect.STEALTH, ALSounds.ambient_stealth);
            Lightsabers.proxy.playLocalSound(player, ALSounds.player_force_stealth_on, 1.0F, 1.0F);
        }
        player.setInvisible(true);
    }

    @Override
    public void stop(Player player, LogicalSide side) {
        super.stop(player, side);
        if (!player.hasEffect(MobEffects.INVISIBILITY)) {
            player.setInvisible(false);
        }
        if (side == LogicalSide.CLIENT && Lightsabers.proxy.isClientPlayer(player)) {
            Lightsabers.proxy.playLocalSound(player, ALSounds.player_force_stealth_off, 1.0F, 1.0F);
        }
    }
}
