package com.fiskmods.lightsabers.common.sound;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Lightsabers.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SITH_COFFIN_OPEN = register(
            "block.sith_sarcophagus.open"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SITH_COFFIN_CLOSE = register(
            "block.sith_sarcophagus.close"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SITH_GHOST_IDLE = register(
            "mob.sith_ghost.idle"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> SITH_GHOST_DEATH = register(
            "mob.sith_ghost.death"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_LIGHTSABER_SWING = register(
            "player.lightsaber.swing"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_LIGHTSABER_HIT = register(
            "player.lightsaber.hit"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> MOB_LIGHTSABER_SWING = register(
            "mob.lightsaber.swing"
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> MOB_LIGHTSABER_HIT = register(
            "mob.lightsaber.hit"
    );

    private ModSounds() {
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier location = Identifier.fromNamespaceAndPath(
                Lightsabers.MODID,
                name
        );
        return SOUND_EVENTS.register(
                name,
                () -> SoundEvent.createVariableRangeEvent(location)
        );
    }
}
