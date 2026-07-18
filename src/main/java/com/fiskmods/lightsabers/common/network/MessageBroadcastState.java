package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MessageBroadcastState extends MessageSyncBase {
    private final int playerId;

    public MessageBroadcastState(Player player) {
        super(player);
        playerId = player.getId();
    }

    private MessageBroadcastState(
            int playerId,
            Map<ALData<?>, Object> playerData,
            List<StatusEffect> activeEffects
    ) {
        super(playerData, activeEffects);
        this.playerId = playerId;
    }

    public static void encode(MessageBroadcastState message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.playerId);
        message.encodeBase(buffer);
    }

    public static MessageBroadcastState decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readVarInt();
        SyncData data = decodeBase(buffer);
        return new MessageBroadcastState(
                playerId,
                data.playerData(),
                data.activeEffects()
        );
    }

    public static void handle(
            MessageBroadcastState message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        Player clientPlayer = Lightsabers.proxy.getPlayer();
        if (clientPlayer == null) {
            return;
        }

        Entity entity = clientPlayer.level().getEntity(message.playerId);
        if (entity instanceof Player player && !Lightsabers.proxy.isClientPlayer(player)) {
            message.apply(player);
        }
    }
}
