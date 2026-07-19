package com.fiskmods.lightsabers.common.lightsaber;

import java.util.Locale;

import net.minecraft.network.chat.Component;

public enum FocusingCrystal
{
    COMPRESSED,
    CRACKED,
    INVERTING,
    FINE_CUT,
//    CHARGED,
    PRISMATIC,
    PICKAXE;
    
    public long getCode()
    {
        return (1 << ordinal()) & 0xFFFF;
    }

    public String getUnlocalizedName()
    {
        return "lightsaber.focusingCrystal." + name().toLowerCase(Locale.ROOT);
    }

    public String getLocalizedName()
    {
        return Component.translatable(getUnlocalizedName()).getString().trim();
    }
}
