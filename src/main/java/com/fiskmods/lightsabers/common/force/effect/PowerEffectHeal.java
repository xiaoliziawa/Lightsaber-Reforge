package com.fiskmods.lightsabers.common.force.effect;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.force.ForceSide;
import com.fiskmods.lightsabers.common.force.PowerDesc;
import com.fiskmods.lightsabers.common.force.PowerDesc.Target;
import com.fiskmods.lightsabers.common.force.PowerDesc.Unit;
import com.fiskmods.lightsabers.helper.ALHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;

public class PowerEffectHeal extends PowerEffect {
    private static final double AREA_RANGE = 6.0D;

    public final float heal;
    public final float aoeHeal;

    public PowerEffectHeal(float heal, float aoeHeal) {
        super(0);
        this.heal = heal;
        this.aoeHeal = aoeHeal;
    }

    @Override
    public boolean execute(Player player, LogicalSide side) {
        player.heal(heal);
        if (aoeHeal > 0) {
            List<LivingEntity> entities = player.level().getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(AREA_RANGE),
                    entity -> entity == player || ALHelper.isAlly(player, entity)
            );
            for (LivingEntity entity : entities) {
                if (entity != player) {
                    entity.heal(aoeHeal);
                }
                if (side == LogicalSide.CLIENT) {
                    Lightsabers.proxy.spawnHealParticles(entity);
                }
            }
        } else if (side == LogicalSide.CLIENT) {
            Lightsabers.proxy.spawnHealParticles(player);
        }
        return true;
    }

    @Override
    public String[] getDesc() {
        List<String> descriptions = new ArrayList<>();
        descriptions.add(PowerDesc.create(
                "to",
                PowerDesc.format("+%s %s", heal, Unit.HEALTH),
                Target.CASTER
        ));
        if (aoeHeal > 0) {
            descriptions.add(PowerDesc.create(
                    "to",
                    PowerDesc.format("+%s %s", aoeHeal, Unit.HEALTH),
                    Target.ALLIES
            ));
        }
        return descriptions.toArray(String[]::new);
    }

    @Override
    public String getCastSound(ForceSide side) {
        return ALSounds.player_force_heal;
    }

    @Override
    public float getCastSoundPitch(ForceSide side) {
        return 1.0F;
    }
}
