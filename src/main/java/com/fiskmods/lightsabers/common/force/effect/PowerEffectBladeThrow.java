package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.force.ForceSide;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;

import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;

public class PowerEffectBladeThrow extends PowerEffect
{
    public PowerEffectBladeThrow(int amplifier)
    {
        super(amplifier);
    }
    
    @Override
    public boolean execute(Player player, LogicalSide side)
    {
        ItemStack heldItem = player.getMainHandItem();

        if (!heldItem.isEmpty()
                && heldItem.getItem() instanceof ItemLightsaberBase
                && ItemLightsaberBase.isActive(heldItem))
        {
            if (side == LogicalSide.SERVER)
            {
                ItemLightsaberBase.throwLightsaber(player, heldItem, amplifier);
                player.swing(InteractionHand.MAIN_HAND);
            }

            return true;
        }

        return false;
    }

    @Override
    public String[] getDesc()
    {
        return new String[][] {
                {},
                {
                        PowerDesc.create("multiply", Unit.IMPACT_RADIUS, PowerDesc.format("%s%s", ChatFormatting.GRAY, 2)),
                        PowerDesc.create("multiply", Unit.THROW_RANGE, PowerDesc.format("%s%s", ChatFormatting.GRAY, 2))
                }
        }[amplifier];
    }

    @Override
    public String getCastSound(ForceSide side)
    {
        return ALSounds.player_lightsaber_swing;
    }

    @Override
    public float getCastSoundPitch(ForceSide side)
    {
        return 1;
    }
}
