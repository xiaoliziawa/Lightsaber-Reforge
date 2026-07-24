package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.force.ForceSide;
import com.fiskmods.lightsabers.common.force.Power;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

import java.util.Random;

public class PowerEffect
{
    public final Random rand = new Random();
    public final int amplifier;
    
    public PowerEffect(int amplifier)
    {
        this.amplifier = amplifier;
    }

    public boolean execute(Player player, LogicalSide side)
    {
        return true;
    }

    public float getUseCost(Player player, Power power)
    {
        return power.powerStats.useCost;
    }

    public String getCastSound(ForceSide side)
    {
        return side == ForceSide.DARK ? ALSounds.player_force_dark : ALSounds.player_force_cast;
    }

    public float getCastSoundVolume(ForceSide side)
    {
        return 1;
    }

    public float getCastSoundPitch(ForceSide side)
    {
        return (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1;
    }

    public String[] getDesc()
    {
        return new String[] {};
    }
}
