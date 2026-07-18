package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MessageUpdateEffects {
    private final int entityId;
    private final List<StatusEffect> activeEffects;

    public MessageUpdateEffects(LivingEntity entity, List<StatusEffect> activeEffects) {
        this(entity.getId(), List.copyOf(activeEffects));
    }

    private MessageUpdateEffects(int entityId, List<StatusEffect> activeEffects) {
        this.entityId = entityId;
        this.activeEffects = activeEffects;
    }

    public static void encode(MessageUpdateEffects message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.activeEffects.size());
        for (StatusEffect effect : message.activeEffects) {
            effect.toBytes(buffer);
        }
    }

    public static MessageUpdateEffects decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int length = buffer.readVarInt();
        List<StatusEffect> effects = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            StatusEffect effect = StatusEffect.fromBytes(buffer);
            if (effect != null) {
                effects.add(effect);
            }
        }
        effects.sort(null);
        return new MessageUpdateEffects(entityId, effects);
    }

    public static void handle(MessageUpdateEffects message, Supplier<NetworkEvent.Context> contextSupplier) {
        Player player = Lightsabers.proxy.getPlayer();
        if (player == null) {
            return;
        }

        Entity entity = player.level().getEntity(message.entityId);
        if (entity instanceof LivingEntity livingEntity) {
            ALEntityData.getData(livingEntity).activeEffects = new ArrayList<>(message.activeEffects);
        }
    }
}
