package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class MessageFlipDoubleLightsaber {
    public static void encode(MessageFlipDoubleLightsaber message, FriendlyByteBuf buffer) {
    }

    public static MessageFlipDoubleLightsaber decode(FriendlyByteBuf buffer) {
        return new MessageFlipDoubleLightsaber();
    }

    public static void handle(
            MessageFlipDoubleLightsaber message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer sender = contextSupplier.get().getSender();
        if (sender != null) {
            ItemDoubleLightsaber.toggleOrientation(sender.getMainHandItem());
        }
    }
}
