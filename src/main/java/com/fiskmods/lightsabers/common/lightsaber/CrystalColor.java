package com.fiskmods.lightsabers.common.lightsaber;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.Random;

public enum CrystalColor
{
    DEEP_BLUE(0, 0x0000FF),
    MEDIUM_BLUE(1, 0x006BFF),
    LIGHT_BLUE(2, 0x59B9FF),
    ARCTIC_BLUE(3, 0xDDF6FF),
    WHITE(4, 0xFFFFFF),
    INDIGO(5, 0x5D00FF),
    PURPLE(6, 0xAD00AD),
    MAGENTA(7, 0xFF00FF),
    PINK(8, 0xFF8FBA),
    RED(9, 0xFF0000),
    BLOOD_ORANGE(10, 0xFF8000),
    AMBER(11, 0xFFB600),
    YELLOW(12, 0xFFFF00),
    GOLD(13, 0xFFFF3A),
    LIME_GREEN(14, 0xBFFF00),
    GREEN(15, 0x00FF00),
    MINT_GREEN(16, 0x00FF9B),
    CYAN(17, 0x00FFFF),
    RGB(18, 0xFF0000);

    private static final long RGB_CYCLE_MILLISECONDS = 6000L;
    private static final float[] RGB_RENDER_VALUES = new float[3];
    
    public static final float[][] COLOR_VALUES = new float[values().length][3];
    
    public static final CrystalColor[] GROUP_BLUE = {DEEP_BLUE, MEDIUM_BLUE, LIGHT_BLUE, ARCTIC_BLUE, CYAN, INDIGO};
    public static final CrystalColor[] GROUP_PURPLE = {INDIGO, PURPLE, MAGENTA, PINK};
    public static final CrystalColor[] GROUP_RED = {PINK, RED, BLOOD_ORANGE};
    public static final CrystalColor[] GROUP_ORANGE = {BLOOD_ORANGE, AMBER};
    public static final CrystalColor[] GROUP_YELLOW = {AMBER, YELLOW, GOLD};
    public static final CrystalColor[] GROUP_GREEN = {LIME_GREEN, GREEN, MINT_GREEN, CYAN};
    
    public static final CrystalColor[] GROUP_COLD = {DEEP_BLUE, MEDIUM_BLUE, LIGHT_BLUE, ARCTIC_BLUE, WHITE, INDIGO, PURPLE, CYAN};
    public static final CrystalColor[] GROUP_HOT = {MAGENTA, PINK, RED, BLOOD_ORANGE, AMBER, YELLOW, GOLD};
    public static final CrystalColor[] GROUP_NEUTRAL = {PINK, LIME_GREEN, GREEN, MINT_GREEN, CYAN};

    public final int id;
    public final int color;

    CrystalColor(int id, int color)
    {
        this.id = id;
        this.color = color;
    }

    public String getUnlocalizedName()
    {
        return "lightsaber.color." + name().toLowerCase(Locale.ROOT);
    }

    public String getLocalizedName()
    {
        return Component.translatable(getUnlocalizedName()).getString().trim();
    }
    
    public float[] getRGB()
    {
        return COLOR_VALUES[ordinal()];
    }

    public int getRenderColor()
    {
        if (this != RGB)
        {
            return color;
        }

        float[] rgb = getRenderRGB();
        int red = Math.round(rgb[0] * 255.0F);
        int green = Math.round(rgb[1] * 255.0F);
        int blue = Math.round(rgb[2] * 255.0F);
        return red << 16 | green << 8 | blue;
    }

    public float[] getRenderRGB()
    {
        if (this != RGB)
        {
            return getRGB();
        }

        float hue = Util.getMillis() % RGB_CYCLE_MILLISECONDS
                / (float) RGB_CYCLE_MILLISECONDS;
        int packed = Mth.hsvToRgb(hue, 1.0F, 1.0F);
        RGB_RENDER_VALUES[0] = (packed >> 16 & 0xFF) / 255.0F;
        RGB_RENDER_VALUES[1] = (packed >> 8 & 0xFF) / 255.0F;
        RGB_RENDER_VALUES[2] = (packed & 0xFF) / 255.0F;
        return RGB_RENDER_VALUES;
    }

    private static float[] getRGB(int hex)
    {
        float r = (float) ((hex & 0xFF0000) >> 16) / 255;
        float g = (float) ((hex & 0xFF00) >> 8) / 255;
        float b = (float) (hex & 0xFF) / 255;
        return new float[] {r, g, b};
    }

    public static CrystalColor get(int id)
    {
        return values()[Math.abs(id) % values().length];
    }

    public static CrystalColor getRandom(Random rand)
    {
        return values()[rand.nextInt(RGB.ordinal())];
    }

    public static CrystalColor getRandom()
    {
        return getRandom(new Random());
    }
    
    static
    {
        for (CrystalColor color : values())
        {
            COLOR_VALUES[color.ordinal()] = getRGB(color.color);
        }
    }
}
