package com.fiskmods.lightsabers.client.sound;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.bus.api.SubscribeEvent;

public enum ClientSoundHandler {
    INSTANCE;

    private static final float STEALTH_VOLUME_SCALE = 0.05F;

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        Player player = Minecraft.getInstance().player;
        SoundInstance sound = event.getSound();
        if (player == null
                || sound == null
                || StatusEffect.get(player, Effect.STEALTH) == null
                || isStealthSound(sound.getLocation())) {
            return;
        }
        event.setSound(new ScaledSoundInstance(sound, STEALTH_VOLUME_SCALE));
    }

    private static boolean isStealthSound(ResourceLocation location) {
        String sound = location.toString();
        return sound.equals(ALSounds.player_force_stealth_on)
                || sound.equals(ALSounds.player_force_stealth_off)
                || sound.startsWith(ALSounds.ambient_stealth);
    }
}
