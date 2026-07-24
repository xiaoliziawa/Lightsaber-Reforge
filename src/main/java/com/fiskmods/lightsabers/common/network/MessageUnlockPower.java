package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MessageUnlockPower implements ALPayload {
    public static final CustomPacketPayload.Type<MessageUnlockPower> TYPE =
            ALPayload.registerType(MessageUnlockPower.class, "unlock_power");
    public static final StreamCodec<FriendlyByteBuf, MessageUnlockPower> STREAM_CODEC =
            StreamCodec.ofMember(MessageUnlockPower::encode, MessageUnlockPower::decode);

    private final String powerName;

    public MessageUnlockPower(Power power) {
        powerName = power.getName();
    }

    private MessageUnlockPower(String name) {
        powerName = name;
    }

    public static void encode(MessageUnlockPower message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.powerName);
    }

    public static MessageUnlockPower decode(FriendlyByteBuf buffer) {
        return new MessageUnlockPower(buffer.readUtf());
    }

    public static void handle(
            MessageUnlockPower message,
            IPayloadContext context
    ) {
        ServerPlayer sender = context.player() instanceof ServerPlayer serverPlayer
                ? serverPlayer
                : null;
        Power power = Power.getPowerFromName(message.powerName);
        if (sender == null || power == null) {
            return;
        }

        if (PowerManager.investXp(sender, power) != PowerManager.InvestResult.NONE) {
            ALData.FORCE_XP.sync(sender);
            ALData.POWERS.sync(sender);
            ALData.BASE_POWER.sync(sender);
        }
    }
}
