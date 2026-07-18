package com.fiskmods.lightsabers.common.force;

import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.Set;

public enum ForceSide
{
    NONE(ChatFormatting.WHITE),
    LIGHT(ChatFormatting.AQUA),
    DARK(ChatFormatting.RED),
    NEUTRAL(ChatFormatting.YELLOW);

    public final ChatFormatting theme;
    public final Set<Power> powers = new HashSet<>();

    ForceSide(ChatFormatting formatting)
    {
        theme = formatting;
    }
    
    public Power getPower()
    {
        switch (this)
        {
        case LIGHT:
            return Power.LIGHT_SIDE;
        case DARK:
            return Power.DARK_SIDE;
        case NEUTRAL:
            return Power.NEUTRAL;
        default:
            return null;
        }
    }

    public ForceSide getOpposite()
    {
        switch (this)
        {
        case LIGHT:
            return DARK;
        case DARK:
            return LIGHT;
        default:
            return this;
        }
    }

    public boolean isPolar()
    {
        return this == LIGHT || this == DARK;
    }
}
