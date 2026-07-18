package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.input.ForcePowerInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class MessageUsePower {
    public static void encode(MessageUsePower message, FriendlyByteBuf buffer) {
    }

    public static MessageUsePower decode(FriendlyByteBuf buffer) {
        return new MessageUsePower();
    }

    public static void handle(
            MessageUsePower message,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer sender = contextSupplier.get().getSender();
        if (sender != null) {
            ForcePowerInput.tryUseSelectedPower(sender, LogicalSide.SERVER);
        }
    }
}
