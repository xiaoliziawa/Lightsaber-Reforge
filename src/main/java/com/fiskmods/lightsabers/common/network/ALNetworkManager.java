package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ALNetworkManager {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextMessageId;

    private ALNetworkManager() {
    }

    public static void register() {
        CHANNEL.messageBuilder(MessagePlayerData.class, nextMessageId++)
                .encoder(MessagePlayerData::encode)
                .decoder(MessagePlayerData::decode)
                .consumerMainThread(MessagePlayerData::handle)
                .add();
        CHANNEL.messageBuilder(
                        MessageUpdateEffects.class,
                        nextMessageId++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(MessageUpdateEffects::encode)
                .decoder(MessageUpdateEffects::decode)
                .consumerMainThread(MessageUpdateEffects::handle)
                .add();
        CHANNEL.messageBuilder(
                        MessagePlayerJoin.class,
                        nextMessageId++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(MessagePlayerJoin::encode)
                .decoder(MessagePlayerJoin::decode)
                .consumerMainThread(MessagePlayerJoin::handle)
                .add();
        CHANNEL.messageBuilder(
                        MessageBroadcastState.class,
                        nextMessageId++,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(MessageBroadcastState::encode)
                .decoder(MessageBroadcastState::decode)
                .consumerMainThread(MessageBroadcastState::handle)
                .add();
        CHANNEL.messageBuilder(
                        MessageToggleLightsaber.class,
                        nextMessageId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(MessageToggleLightsaber::encode)
                .decoder(MessageToggleLightsaber::decode)
                .consumerMainThread(MessageToggleLightsaber::handle)
                .add();
        CHANNEL.messageBuilder(
                        MessageUsePower.class,
                        nextMessageId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(MessageUsePower::encode)
                .decoder(MessageUsePower::decode)
                .consumerMainThread(MessageUsePower::handle)
                .add();
        CHANNEL.messageBuilder(
                        MessageUnlockPower.class,
                        nextMessageId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(MessageUnlockPower::encode)
                .decoder(MessageUnlockPower::decode)
                .consumerMainThread(MessageUnlockPower::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToTrackingAndSelf(Entity entity, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
