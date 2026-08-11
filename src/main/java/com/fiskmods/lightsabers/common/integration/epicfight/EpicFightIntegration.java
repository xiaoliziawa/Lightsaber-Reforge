package com.fiskmods.lightsabers.common.integration.epicfight;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.sound.ModSounds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.deferred.holders.DeferredWeapon;
import yesman.epicfight.registry.entries.EpicFightItemCapabilityPresets;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

import java.util.function.Supplier;

public final class EpicFightIntegration {
    private static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS =
            DeferredRegister.create(EpicFightRegistries.CONDITION, Lightsabers.MODID);
    private static final Supplier<Supplier<Condition<?>>> SPINNING_LIGHTSABER =
            CONDITIONS.register(
                    "spinning_lightsaber",
                    () -> SpinningLightsaberCondition::new
            );
    private static final Supplier<Supplier<Condition<?>>> SPEAR_LIGHTSABER =
            CONDITIONS.register(
                    "spear_lightsaber",
                    () -> SpearLightsaberCondition::new
            );
    private static final Supplier<Supplier<Condition<?>>> DAGGER_LIGHTSABER =
            CONDITIONS.register(
                    "dagger_lightsaber",
                    () -> DaggerLightsaberCondition::new
            );

    private EpicFightIntegration() {
    }

    public static void register(IEventBus modEventBus) {
        CONDITIONS.register(modEventBus);
        EpicFightEventHooks.Registry.WEAPON_CAPABILITY_PRESET.registerEvent(
                EpicFightIntegration::registerWeaponPresets
        );
    }

    private static void registerWeaponPresets(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(
                weaponType("lightsaber_sword"),
                item -> withLightsaberSounds(EpicFightItemCapabilityPresets.SWORD, item)
        );
        event.getTypeEntry().put(
                weaponType("lightsaber_spear"),
                item -> withLightsaberSounds(EpicFightItemCapabilityPresets.SPEAR, item)
        );
        event.getTypeEntry().put(
                weaponType("lightsaber_dagger"),
                item -> withLightsaberSounds(EpicFightItemCapabilityPresets.DAGGER, item)
        );
    }

    private static WeaponCapability.Builder withLightsaberSounds(
            DeferredWeapon preset,
            Item item
    ) {
        WeaponCapability.Builder builder = (WeaponCapability.Builder)
                WeaponCapabilityPresets.registerPreset(preset.value(), item);
        return builder
                .swingSound(ModSounds.PLAYER_LIGHTSABER_SWING)
                .hitSound(ModSounds.PLAYER_LIGHTSABER_HIT);
    }

    public static boolean isBattleMode(Player player) {
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
        return playerPatch != null && playerPatch.isEpicFightMode();
    }

    private static Identifier weaponType(String name) {
        return Identifier.fromNamespaceAndPath(Lightsabers.MODID, name);
    }
}
