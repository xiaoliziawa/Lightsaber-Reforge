package com.fiskmods.lightsabers.common.event;

import atomicstryker.dynamiclights.server.DynamicLights;
import atomicstryker.dynamiclights.server.IDynamicLightSource;
import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.entity.EntityLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class CommonEventHandlerDL {
    private static final int LIGHT_LEVEL = 15;
    private static final int MILLIS_PER_TICK = 50;

    private final Map<ServerLevel, LevelLights> levelLights = new IdentityHashMap<>();

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!ModConfig.dynamicLightsEnabled
                || (!isLightsaber(event.getFrom())
                && !isLightsaber(event.getTo()))) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            levelLights.computeIfAbsent(level, ignored -> new LevelLights())
                    .refresh(entity);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!ModConfig.dynamicLightsEnabled
                || !(event.getEntity() instanceof EntityLightsaber entity)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        levelLights.computeIfAbsent(level, ignored -> new LevelLights())
                .refresh(entity);
    }

    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        LevelLights lights = levelLights.get(level);
        if (lights != null) {
            lights.remove(event.getEntity());
        }
    }

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        LevelLights lights = levelLights.computeIfAbsent(level, ignored -> new LevelLights());
        if (!ModConfig.dynamicLightsEnabled) {
            lights.clear();
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime < lights.nextScanTick) {
            return;
        }
        int intervalTicks = Math.max(
                1,
                (ModConfig.dynamicLightsUpdateInterval + MILLIS_PER_TICK - 1)
                        / MILLIS_PER_TICK
        );
        lights.nextScanTick = gameTime + intervalTicks;
        lights.scan(level);
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelLights lights = levelLights.remove(level);
            if (lights != null) {
                lights.clear();
            }
        }
    }

    private static int getEntityLight(Entity entity) {
        if (entity instanceof EntityLightsaber) {
            return LIGHT_LEVEL;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return 0;
        }

        if (isActiveLightsaber(livingEntity.getMainHandItem())
                || isActiveLightsaber(livingEntity.getOffhandItem())) {
            return LIGHT_LEVEL;
        }
        return 0;
    }

    private static boolean isActiveLightsaber(ItemStack stack) {
        return isLightsaber(stack)
                && ItemLightsaberBase.isActive(stack);
    }

    private static boolean isLightsaber(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemLightsaberBase;
    }

    private static final class LevelLights {
        private final Map<Entity, EntityLightAdapter> tracked = new IdentityHashMap<>();
        private long nextScanTick;

        private void refresh(Entity entity) {
            int lightLevel = entity.isAlive() ? getEntityLight(entity) : 0;
            update(entity, lightLevel);
        }

        private void update(Entity entity, int lightLevel) {
            EntityLightAdapter adapter = tracked.get(entity);
            if (lightLevel <= 0) {
                remove(entity);
                return;
            }

            if (adapter == null) {
                adapter = new EntityLightAdapter(entity, lightLevel);
                tracked.put(entity, adapter);
                DynamicLights.addLightSource(adapter);
            } else if (adapter.lightLevel != lightLevel) {
                DynamicLights.removeLightSource(adapter);
                adapter.lightLevel = lightLevel;
                DynamicLights.addLightSource(adapter);
            }
        }

        private void remove(Entity entity) {
            EntityLightAdapter adapter = tracked.remove(entity);
            if (adapter != null) {
                DynamicLights.removeLightSource(adapter);
            }
        }

        private void scan(ServerLevel level) {
            Set<Entity> activeEntities = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Entity entity : level.getAllEntities()) {
                int lightLevel = entity.isAlive() ? getEntityLight(entity) : 0;
                if (lightLevel <= 0) {
                    continue;
                }

                activeEntities.add(entity);
                update(entity, lightLevel);
            }

            Iterator<Map.Entry<Entity, EntityLightAdapter>> iterator =
                    tracked.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Entity, EntityLightAdapter> entry = iterator.next();
                if (!activeEntities.contains(entry.getKey())) {
                    DynamicLights.removeLightSource(entry.getValue());
                    iterator.remove();
                }
            }
        }

        private void clear() {
            for (EntityLightAdapter adapter : tracked.values()) {
                DynamicLights.removeLightSource(adapter);
            }
            tracked.clear();
        }
    }

    private static final class EntityLightAdapter implements IDynamicLightSource {
        private final Entity entity;
        private int lightLevel;

        private EntityLightAdapter(Entity entity, int lightLevel) {
            this.entity = entity;
            this.lightLevel = lightLevel;
        }

        @Override
        public Entity getAttachmentEntity() {
            return entity;
        }

        @Override
        public int getLightLevel() {
            return lightLevel;
        }
    }
}
