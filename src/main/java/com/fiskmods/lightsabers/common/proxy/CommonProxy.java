package com.fiskmods.lightsabers.common.proxy;

import com.fiskmods.lightsabers.common.data.effect.Effect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;

public class CommonProxy {
    public void registerModEvents(IEventBus modEventBus) {
    }

    public Dist getSide() {
        return Dist.DEDICATED_SERVER;
    }

    public float getRenderTick() {
        return 1.0F;
    }

    public Player getPlayer() {
        return null;
    }

    public boolean isClientPlayer(LivingEntity entity) {
        return false;
    }

    public Iterable<Entity> getLoadedEntities(Level level) {
        return level instanceof ServerLevel serverLevel ? serverLevel.getAllEntities() : List.of();
    }

    public void playStatusEffectSound(Player player, Effect effect, String soundName) {
    }

    public void playLightningSound(Player player) {
    }

    public void playLocalSound(Player player, String soundName, float volume, float pitch) {
    }

    public void spawnHealParticles(LivingEntity entity) {
    }
}
