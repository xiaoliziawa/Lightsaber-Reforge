package com.fiskmods.lightsabers.common.force.effect;

import static com.fiskmods.lightsabers.common.force.PowerDesc.*;
import static com.fiskmods.lightsabers.common.force.PowerDesc.Target.*;
import static com.fiskmods.lightsabers.common.force.PowerDesc.Unit.*;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

public class PowerEffectMeditation extends PowerEffectInstant
{
    private static final Identifier ABSORPTION_MODIFIER_ID = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "meditation_absorption"
    );

    public PowerEffectMeditation(int amplifier)
    {
        super(Effect.MEDITATION, 90, amplifier);
    }
    
    @Override
    public boolean execute(Player player, LogicalSide side)
    {
        if (!super.execute(player, side))
        {
            return false;
        }

        updateAbsorptionCapacity(player);
        StatusEffect meditation = StatusEffect.get(player, Effect.MEDITATION);
        if (meditation != null)
        {
            player.setAbsorptionAmount(getAbsorption(meditation.amplifier));
        }
        return true;
    }

    public static void updateAbsorptionCapacity(LivingEntity entity)
    {
        AttributeInstance maxAbsorption = entity.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorption == null)
        {
            return;
        }

        StatusEffect meditation = StatusEffect.get(entity, Effect.MEDITATION);
        if (meditation == null)
        {
            maxAbsorption.removeModifier(ABSORPTION_MODIFIER_ID);
            return;
        }

        double absorption = getAbsorption(meditation.amplifier);
        AttributeModifier currentModifier = maxAbsorption.getModifier(ABSORPTION_MODIFIER_ID);
        if (currentModifier == null || currentModifier.amount() != absorption)
        {
            maxAbsorption.addOrUpdateTransientModifier(new AttributeModifier(
                    ABSORPTION_MODIFIER_ID,
                    absorption,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    @Override
    public String[] getDesc()
    {
        return new String[]
        {
            create("effect", format("%s %s%s", translateFormatted("forcepower.stat.multiply", ATTACK_DAMAGE, getModifierAmount(amplifier)), ChatFormatting.GRAY, 90), CASTER),
            create("to", format("+%s %s", getAbsorption(amplifier), ABSORPTION), CASTER)
        };
    }

    public static float getModifierAmount(int amplifier)
    {
        float f = 0.25F;

        for (int i = 0; i < amplifier; ++i)
        {
            f *= 2;
        }

        return 1 + f;
    }
    
    public static float getAbsorption(int amplifier)
    {
        return 4 + amplifier * 2;
    }
}
