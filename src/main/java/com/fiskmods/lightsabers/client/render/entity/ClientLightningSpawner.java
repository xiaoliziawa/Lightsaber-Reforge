package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.entity.EntityForceLightning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public enum ClientLightningSpawner {
    INSTANCE;

    private final Map<Player, EntityForceLightning> anchors = new IdentityHashMap<>();
    private ClientLevel currentLevel;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != currentLevel) {
            clear();
            currentLevel = level;
        }
        if (level == null) {
            return;
        }

        for (Player player : level.players()) {
            if (!needsLightning(player)) {
                continue;
            }
            EntityForceLightning anchor = anchors.get(player);
            if (anchor == null || !anchor.isAlive()) {
                anchor = new EntityForceLightning(level, player);
                anchors.put(player, anchor);
                level.addFreshEntity(anchor);
            }
        }

        Iterator<Map.Entry<Player, EntityForceLightning>> iterator =
                anchors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Player, EntityForceLightning> entry = iterator.next();
            if (!entry.getKey().isAlive() || !needsLightning(entry.getKey())) {
                entry.getValue().discard();
                iterator.remove();
            }
        }
    }

    private static boolean needsLightning(Player player) {
        return ALData.DRAIN_LIFE_TIMER.get(player) > 0
                || StatusEffect.get(player, Effect.LIGHTNING) != null;
    }

    private void clear() {
        for (EntityForceLightning anchor : anchors.values()) {
            anchor.discard();
        }
        anchors.clear();
    }
}
