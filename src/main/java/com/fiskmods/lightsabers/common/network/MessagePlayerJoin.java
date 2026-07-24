package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public final class MessagePlayerJoin extends MessageSyncBase implements ALPayload {
    public static final CustomPacketPayload.Type<MessagePlayerJoin> TYPE =
            ALPayload.registerType(MessagePlayerJoin.class, "player_join");
    public static final StreamCodec<FriendlyByteBuf, MessagePlayerJoin> STREAM_CODEC =
            StreamCodec.ofMember(MessagePlayerJoin::encode, MessagePlayerJoin::decode);

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
            IPayloadContext context
    ) {
        Player player = Lightsabers.proxy.getPlayer();
        if (player != null) {
            message.apply(player);
        }
    }
}
