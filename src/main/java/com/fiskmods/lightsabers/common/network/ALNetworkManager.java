package com.fiskmods.lightsabers.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ALNetworkManager {
    private static final String PROTOCOL_VERSION = "2";

    private ALNetworkManager() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ALNetworkManager::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playBidirectional(
                MessagePlayerData.TYPE,
                MessagePlayerData.STREAM_CODEC,
                MessagePlayerData::handle,
                MessagePlayerData::handle
        );
        registrar.playToClient(
                MessageUpdateEffects.TYPE,
                MessageUpdateEffects.STREAM_CODEC,
                MessageUpdateEffects::handle
        );
        registrar.playToClient(
                MessagePlayerJoin.TYPE,
                MessagePlayerJoin.STREAM_CODEC,
                MessagePlayerJoin::handle
        );
        registrar.playToClient(
                MessageBroadcastState.TYPE,
                MessageBroadcastState.STREAM_CODEC,
                MessageBroadcastState::handle
        );
        registrar.playToServer(
                MessageToggleLightsaber.TYPE,
                MessageToggleLightsaber.STREAM_CODEC,
                MessageToggleLightsaber::handle
        );
        registrar.playToServer(
                MessageUsePower.TYPE,
                MessageUsePower.STREAM_CODEC,
                MessageUsePower::handle
        );
        registrar.playToServer(
                MessageUnlockPower.TYPE,
                MessageUnlockPower.STREAM_CODEC,
                MessageUnlockPower::handle
        );
        registrar.playToServer(
                MessageFlipDoubleLightsaber.TYPE,
                MessageFlipDoubleLightsaber.STREAM_CODEC,
                MessageFlipDoubleLightsaber::handle
        );
    }

    public static void sendToServer(CustomPacketPayload message) {
        ClientPacketDistributor.sendToServer(message);
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload message) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
