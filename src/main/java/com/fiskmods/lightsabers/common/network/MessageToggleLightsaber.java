package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageToggleLightsaber(boolean active) implements ALPayload {
    public static final CustomPacketPayload.Type<MessageToggleLightsaber> TYPE =
            ALPayload.registerType(MessageToggleLightsaber.class, "toggle_lightsaber");
    public static final StreamCodec<FriendlyByteBuf, MessageToggleLightsaber> STREAM_CODEC =
            StreamCodec.ofMember(MessageToggleLightsaber::encode, MessageToggleLightsaber::decode);

    public static void encode(MessageToggleLightsaber message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.active);
    }

    public static MessageToggleLightsaber decode(FriendlyByteBuf buffer) {
        return new MessageToggleLightsaber(buffer.readBoolean());
    }

    public static void handle(
            MessageToggleLightsaber message,
            IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer sender) {
            ItemLightsaberBase.ignite(sender, message.active);
        }
    }
}
