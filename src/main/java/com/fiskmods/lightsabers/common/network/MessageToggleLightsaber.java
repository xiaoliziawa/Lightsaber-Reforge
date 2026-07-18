package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MessageToggleLightsaber(boolean active) {
    public static void encode(MessageToggleLightsaber message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.active);
    }

    public static MessageToggleLightsaber decode(FriendlyByteBuf buffer) {
        return new MessageToggleLightsaber(buffer.readBoolean());
    }

    public static void handle(
            MessageToggleLightsaber message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer sender = contextSupplier.get().getSender();
        if (sender != null) {
            ItemLightsaberBase.ignite(sender, message.active);
        }
    }
}
