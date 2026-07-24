package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class PowerEffectFortify extends PowerEffectStatus {
    public PowerEffectFortify(int amplifier) {
        super(Effect.FORTIFY, amplifier);
    }

    @Override
    public String[] getDesc() {
        return new String[] {
                PowerDesc.create("divide", Unit.FORCE_DAMAGE, getModifierAmount(amplifier))
        };
    }

    @Override
    public void start(Player player, LogicalSide side) {
        if (side == LogicalSide.CLIENT && Lightsabers.proxy.isClientPlayer(player)) {
            Lightsabers.proxy.playStatusEffectSound(player, Effect.FORTIFY, ALSounds.ambient_fortify);
        }
    }

    public static float getModifierAmount(int amplifier) {
        float modifier = 0.25F;
        for (int i = 0; i < amplifier; i++) {
            modifier *= 2;
        }
        return 1 + modifier;
    }
}
