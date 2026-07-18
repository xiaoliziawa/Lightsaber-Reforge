package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MessagePlayerJoin extends MessageSyncBase {
    public MessagePlayerJoin(Player player) {
        super(player);
    }

    private MessagePlayerJoin(
            Map<ALData<?>, Object> playerData,
            List<StatusEffect> activeEffects
    ) {
        super(playerData, activeEffects);
    }

    public static void encode(MessagePlayerJoin message, FriendlyByteBuf buffer) {
        message.encodeBase(buffer);
    }

    public static MessagePlayerJoin decode(FriendlyByteBuf buffer) {
        SyncData data = decodeBase(buffer);
        return new MessagePlayerJoin(data.playerData(), data.activeEffects());
    }

    public static void handle(
            MessagePlayerJoin message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        Player player = Lightsabers.proxy.getPlayer();
        if (player != null) {
            message.apply(player);
        }
    }
}
