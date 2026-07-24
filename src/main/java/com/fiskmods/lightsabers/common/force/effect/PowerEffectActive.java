package com.fiskmods.lightsabers.common.force.effect;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class PowerEffectActive extends PowerEffect
{
    public PowerEffectActive(int amplifier)
    {
        super(amplifier);
    }
    
    public void start(Player player, LogicalSide side)
    {
    }

    public void stop(Player player, LogicalSide side)
    {
    }

    public void render(Player player, float partialTicks)
    {
    }
}
