package com.fiskmods.lightsabers.common.sound;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Lightsabers.MODID);

    public static final RegistryObject<SoundEvent> SITH_COFFIN_OPEN = register(
            "block.sith_sarcophagus.open"
    );
    public static final RegistryObject<SoundEvent> SITH_COFFIN_CLOSE = register(
            "block.sith_sarcophagus.close"
    );
    public static final RegistryObject<SoundEvent> SITH_GHOST_IDLE = register(
            "mob.sith_ghost.idle"
    );
    public static final RegistryObject<SoundEvent> SITH_GHOST_DEATH = register(
            "mob.sith_ghost.death"
    );
    public static final RegistryObject<SoundEvent> PLAYER_LIGHTSABER_SWING = register(
            "player.lightsaber.swing"
    );
    public static final RegistryObject<SoundEvent> PLAYER_LIGHTSABER_HIT = register(
            "player.lightsaber.hit"
    );
    public static final RegistryObject<SoundEvent> MOB_LIGHTSABER_SWING = register(
            "mob.lightsaber.swing"
    );
    public static final RegistryObject<SoundEvent> MOB_LIGHTSABER_HIT = register(
            "mob.lightsaber.hit"
    );

    private ModSounds() {
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                Lightsabers.MODID,
                name
        );
        return SOUND_EVENTS.register(
                name,
                () -> SoundEvent.createVariableRangeEvent(location)
        );
    }
}
