package com.fiskmods.lightsabers.client.sound;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.effect.ForceTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MovingSoundLightning extends AbstractTickableSoundInstance {
    private static final double TARGET_RANGE = 7.0D;

    private final Player caster;

    public MovingSoundLightning(Player caster) {
        super(
                SoundEvent.createVariableRangeEvent(
                        ResourceLocation.parse(ALSounds.player_force_lightning)
                ),
                SoundSource.PLAYERS,
                RandomSource.create()
        );
        this.caster = caster;
        looping = true;
        delay = 0;
        volume = 1.0F;
        updatePosition(caster);
    }

    @Override
    public void tick() {
        if (!caster.isAlive() || caster.isRemoved()) {
            stop();
            return;
        }

        float intensity = ALData.RIGHT_ARM_TIMER.interpolate(caster);
        if (intensity <= 0) {
            stop();
            return;
        }

        LivingEntity target = ForceTargeting.findLookTarget(caster, TARGET_RANGE);
        LivingEntity soundPosition = Minecraft.getInstance().player == target ? target : caster;
        updatePosition(soundPosition);
        volume = intensity;
        pitch = 0.75F + random.nextFloat() * 0.25F;
    }

    private void updatePosition(LivingEntity entity) {
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
    }
}
