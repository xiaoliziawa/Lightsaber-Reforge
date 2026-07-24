package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class PowerEffectStatus extends PowerEffectActive
{
    public final Effect effect;

    public PowerEffectStatus(Effect effect, int amplifier)
    {
        super(amplifier);
        this.effect = effect;
    }

    @Override
    public boolean execute(Player player, LogicalSide side)
    {
        float force = ALDataInterp.FORCE_POWER.get(player);

        if (effect.getPower(amplifier) != null)
        {
            StatusEffect.add(player, effect, (int) (force / (effect.getPower(amplifier).getUseCost(player) / 20)), amplifier);
        }

        return true;
    }
    
    @Override
    public void stop(Player player, LogicalSide side)
    {
        super.stop(player, side);
        StatusEffect.clear(player, effect);
    }
}
