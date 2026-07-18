package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.PowerManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class MessageUnlockPower {
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
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPlayer sender = contextSupplier.get().getSender();
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
