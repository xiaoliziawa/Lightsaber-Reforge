package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MessageFlipDoubleLightsaber implements ALPayload {
    public static final CustomPacketPayload.Type<MessageFlipDoubleLightsaber> TYPE =
            ALPayload.registerType(MessageFlipDoubleLightsaber.class, "flip_double_lightsaber");
    public static final StreamCodec<FriendlyByteBuf, MessageFlipDoubleLightsaber> STREAM_CODEC =
            StreamCodec.ofMember(
                    MessageFlipDoubleLightsaber::encode,
                    MessageFlipDoubleLightsaber::decode
            );

    public static void encode(MessageFlipDoubleLightsaber message, FriendlyByteBuf buffer) {
    }

    public static MessageFlipDoubleLightsaber decode(FriendlyByteBuf buffer) {
        return new MessageFlipDoubleLightsaber();
    }

    public static void handle(
            MessageFlipDoubleLightsaber message,
            IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer sender) {
            ItemDoubleLightsaber.toggleOrientation(sender.getMainHandItem());
        }
    }
}
