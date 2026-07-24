package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.input.ForcePowerInput;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MessageUsePower implements ALPayload {
    public static final CustomPacketPayload.Type<MessageUsePower> TYPE =
            ALPayload.registerType(MessageUsePower.class, "use_power");
    public static final StreamCodec<FriendlyByteBuf, MessageUsePower> STREAM_CODEC =
            StreamCodec.ofMember(MessageUsePower::encode, MessageUsePower::decode);

    public static void encode(MessageUsePower message, FriendlyByteBuf buffer) {
    }

    public static MessageUsePower decode(FriendlyByteBuf buffer) {
        return new MessageUsePower();
    }

    public static void handle(
            MessageUsePower message,
            IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer sender) {
            ForcePowerInput.tryUseSelectedPower(sender, LogicalSide.SERVER);
        }
    }
}
