package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import fiskfille.utils.helper.NBTHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class MessagePlayerData {
    private final int playerId;
    private final ALData<?> type;
    private final Object value;

    public MessagePlayerData(Player player, ALData<?> type, Object value) {
        this(player.getId(), type, value);
    }

    private MessagePlayerData(int playerId, ALData<?> type, Object value) {
        this.playerId = playerId;
        this.type = type;
        this.value = value;
    }

    public static void encode(MessagePlayerData message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.playerId);
        buffer.writeVarInt(ALData.REGISTRY.getIDForObject(message.type));
        NBTHelper.toBytes(buffer, message.value);
    }

    public static MessagePlayerData decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readVarInt();
        ALData<?> type = ALData.REGISTRY.getObjectById(buffer.readVarInt());
        if (type == null) {
            throw new IllegalArgumentException("Received unknown Advanced Lightsabers data type");
        }
        return new MessagePlayerData(playerId, type, NBTHelper.fromBytes(buffer, type.typeClass));
    }

    public static void handle(MessagePlayerData message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        LogicalSide senderSide = context.getDirection().getOriginationSide();
        Player player = senderSide == LogicalSide.CLIENT
                ? getServerPlayer(message, context)
                : getClientPlayer(message);

        if (player == null) {
            return;
        }
        if (!message.type.hasPerms(senderSide)) {
            Lightsabers.LOGGER.warn(
                    "Player {} tried to set {} from illegal side {}",
                    player.getName().getString(),
                    message.type,
                    senderSide
            );
            return;
        }

        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            setWithoutNotify(message.type, player, message.value);
        } else {
            setAndSync(message.type, player, message.value);
        }
    }

    private static Player getServerPlayer(MessagePlayerData message, NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null || sender.getId() != message.playerId) {
            Lightsabers.LOGGER.warn("Rejected player data packet with mismatched sender entity id");
            return null;
        }
        return sender;
    }

    private static Player getClientPlayer(MessagePlayerData message) {
        Player localPlayer = Lightsabers.proxy.getPlayer();
        if (localPlayer == null) {
            return null;
        }
        Entity entity = localPlayer.level().getEntity(message.playerId);
        return entity instanceof Player player ? player : null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setWithoutNotify(ALData type, Player player, Object value) {
        type.setWithoutNotify(player, value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void setAndSync(ALData type, Player player, Object value) {
        type.set(player, value);
    }
}
