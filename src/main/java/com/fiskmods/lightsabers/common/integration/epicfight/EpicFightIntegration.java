package com.fiskmods.lightsabers.common.integration.epicfight;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

import java.util.function.Function;
import java.util.function.Supplier;

public final class EpicFightIntegration {
    private static final ResourceLocation CONDITION_REGISTRY =
            ResourceLocation.fromNamespaceAndPath("epicfight", "conditions");
    private static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS =
            DeferredRegister.create(CONDITION_REGISTRY, Lightsabers.MODID);
    private static final RegistryObject<Supplier<Condition<?>>> SPINNING_LIGHTSABER =
            CONDITIONS.register(
                    "spinning_lightsaber",
                    () -> SpinningLightsaberCondition::new
            );
    private static final RegistryObject<Supplier<Condition<?>>> DAGGER_LIGHTSABER =
            CONDITIONS.register(
                    "dagger_lightsaber",
                    () -> DaggerLightsaberCondition::new
            );

    private EpicFightIntegration() {
    }

    public static void register(IEventBus modEventBus) {
        CONDITIONS.register(modEventBus);
        modEventBus.addListener(EpicFightIntegration::registerWeaponPresets);
    }

    private static void registerWeaponPresets(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(
                weaponType("lightsaber_sword"),
                item -> withLightsaberSounds(WeaponCapabilityPresets.SWORD, item)
        );
        event.getTypeEntry().put(
                weaponType("lightsaber_spear"),
                item -> withLightsaberSounds(WeaponCapabilityPresets.SPEAR, item)
        );
        event.getTypeEntry().put(
                weaponType("lightsaber_dagger"),
                item -> withLightsaberSounds(WeaponCapabilityPresets.DAGGER, item)
        );
    }

    private static WeaponCapability.Builder withLightsaberSounds(
            Function<Item, CapabilityItem.Builder> preset,
            Item item
    ) {
        WeaponCapability.Builder builder = (WeaponCapability.Builder) preset.apply(item);
        return builder
                .swingSound(ModSounds.PLAYER_LIGHTSABER_SWING.get())
                .hitSound(ModSounds.PLAYER_LIGHTSABER_HIT.get());
    }

    public static boolean isBattleMode(Player player) {
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
        return playerPatch != null && playerPatch.isEpicFightMode();
    }

    private static ResourceLocation weaponType(String name) {
        return ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, name);
    }
}
