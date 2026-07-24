package com.fiskmods.lightsabers.common.force;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Locale;

public class PowerDesc
{
    public static String create(String action, Object arg1, Object arg2)
    {
        String gray = ChatFormatting.GRAY.toString();
        String yellow = ChatFormatting.YELLOW.toString();
        return gray + Component.translatable(
                "forcepower.stat." + action,
                yellow + formatObj(arg1) + gray,
                yellow + formatObj(arg2) + gray
        ).getString();
    }

    public static String translateFormatted(String key, Object... args)
    {
        Object[] args1 = new Object[args.length];

        for (int i = 0; i < args.length; ++i)
        {
            args1[i] = formatObj(args[i]);
        }

        return Component.translatable(key, args1).getString();
    }

    public static String format(String key, Object... args)
    {
        Object[] args1 = new Object[args.length];

        for (int i = 0; i < args.length; ++i)
        {
            args1[i] = formatObj(args[i]);
        }

        return String.format(Locale.ROOT, key, args1);
    }

    public static String formatObj(Object obj)
    {
        if (obj instanceof Float || obj instanceof Double)
        {
            return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(obj);
        }
        else if (obj instanceof Effect)
        {
            return ((Effect) obj).getLocalizedName();
        }
        else if (obj instanceof Target)
        {
            return ((Target) obj).getLocalizedName();
        }
        else if (obj instanceof Unit)
        {
            return ((Unit) obj).getLocalizedName();
        }

        return String.valueOf(obj);
    }

    public static String list(Object... args)
    {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < args.length; ++i)
        {
            if (i > 0)
            {
                if (i == args.length - 1)
                {
                    result.append(' ').append(Component.translatable("forcepower.list").getString()).append(' ');
                }
                else if (i > 0)
                {
                    result.append(", ");
                }
            }

            result.append(formatObj(args[i]));
        }

        return result.toString();
    }

    public static enum Unit
    {
        HEALTH,
        DAMAGE,
        ATTACK_DAMAGE,
        FORCE_DAMAGE,
        ENERGY_DAMAGE,
        ABSORPTION,
        SPEED,
        INVISIBILITY,
        IMPACT_RADIUS,
        THROW_RANGE,
        KNOCKBACK,
        FALL_RESISTANCE;

        public String getUnlocalizedName()
        {
            return "forcepower.stat.unit." + name().toLowerCase(Locale.ROOT);
        }
        
        public String getLocalizedName()
        {
            return Component.translatable(getUnlocalizedName()).getString();
        }
    }

    public static enum Target
    {
        CASTER,
        TARGET,
        ALLIES,
        ENEMIES,
        MOBS,
        PLAYERS,
        INVISIBLE;

        public String getUnlocalizedName()
        {
            return "forcepower.stat.target." + name().toLowerCase(Locale.ROOT);
        }
        
        public String getLocalizedName()
        {
            return Component.translatable(getUnlocalizedName()).getString();
        }
    }
}
