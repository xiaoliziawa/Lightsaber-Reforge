package com.fiskmods.lightsabers.common.entity;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Lightsabers.MODID);

    public static final RegistryObject<EntityType<EntityLightsaber>> LIGHTSABER = ENTITY_TYPES.register(
            "lightsaber",
            () -> EntityType.Builder.<EntityLightsaber>of(EntityLightsaber::new, MobCategory.MISC)
                    .sized(1.0F, 0.125F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightsabers:lightsaber")
    );
    public static final RegistryObject<EntityType<EntitySithGhost>> SITH_GHOST =
            ENTITY_TYPES.register(
                    "sith_ghost",
                    () -> EntityType.Builder.of(EntitySithGhost::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(3)
                            .build("lightsabers:sith_ghost")
            );
    public static final RegistryObject<EntityType<EntityForceLightning>> FORCE_LIGHTNING =
            ENTITY_TYPES.register(
                    "force_lightning",
                    () -> EntityType.Builder.<EntityForceLightning>of(
                                    EntityForceLightning::new,
                                    MobCategory.MISC
                            )
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("lightsabers:force_lightning")
            );

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SITH_GHOST.get(), EntitySithGhost.createAttributes().build());
    }
}
