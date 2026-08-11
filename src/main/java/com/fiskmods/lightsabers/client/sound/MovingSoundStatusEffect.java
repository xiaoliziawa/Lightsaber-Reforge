package com.fiskmods.lightsabers.client.sound;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

public class MovingSoundStatusEffect extends AbstractTickableSoundInstance {
    private final LivingEntity entity;
    private final Effect effect;

    public MovingSoundStatusEffect(LivingEntity entity, Effect effect, String soundName) {
        super(
                SoundEvent.createVariableRangeEvent(Identifier.parse(soundName)),
                SoundSource.PLAYERS,
                RandomSource.create()
        );
        this.entity = entity;
        this.effect = effect;
        looping = true;
        delay = 0;
        volume = 1.0F;
        updatePosition();
    }

    @Override
    public void tick() {
        if (!entity.isAlive() || entity.isRemoved() || StatusEffect.get(entity, effect) == null) {
            stop();
            return;
        }
        updatePosition();
    }

    private void updatePosition() {
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
    }
}
