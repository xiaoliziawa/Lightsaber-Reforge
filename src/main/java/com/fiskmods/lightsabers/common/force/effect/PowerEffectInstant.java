package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

public class PowerEffectInstant extends PowerEffect
{
    public final Effect effect;
    public final float duration;
    public final int durationInt;

    public PowerEffectInstant(Effect effect, float duration, int amplifier)
    {
        super(amplifier);
        this.effect = effect;
        this.duration = duration;
        this.durationInt = (int) (duration * 20);
    }

    @Override
    public boolean execute(Player player, LogicalSide side)
    {
        StatusEffect.add(player, effect, durationInt, amplifier);
        return true;
    }
}
