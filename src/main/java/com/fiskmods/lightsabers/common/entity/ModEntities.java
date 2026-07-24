package com.fiskmods.lightsabers.common.entity;

import java.util.function.Supplier;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Lightsabers.MODID);

    public static final Supplier<EntityType<EntityLightsaber>> LIGHTSABER = ENTITY_TYPES.register(
            "lightsaber",
            () -> EntityType.Builder.<EntityLightsaber>of(EntityLightsaber::new, MobCategory.MISC)
                    .sized(1.0F, 0.125F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("lightsabers:lightsaber")
    );
    public static final Supplier<EntityType<EntitySithGhost>> SITH_GHOST =
            ENTITY_TYPES.register(
                    "sith_ghost",
                    () -> EntityType.Builder.of(EntitySithGhost::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(3)
                            .build("lightsabers:sith_ghost")
            );
    public static final Supplier<EntityType<EntityForceLightning>> FORCE_LIGHTNING =
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
