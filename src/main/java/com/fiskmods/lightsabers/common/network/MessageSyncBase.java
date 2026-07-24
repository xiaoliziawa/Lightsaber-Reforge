package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALEntityData;
import com.fiskmods.lightsabers.common.data.ALPlayerData;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MessageSyncBase {
    protected final Map<ALData<?>, Object> playerData;
    protected final List<StatusEffect> activeEffects;

    protected MessageSyncBase(Player player) {
        this(
                new HashMap<>(ALPlayerData.getData(player).data),
                List.copyOf(StatusEffect.get(player))
        );
    }

    protected MessageSyncBase(
            Map<ALData<?>, Object> playerData,
            List<StatusEffect> activeEffects
    ) {
        this.playerData = playerData;
        this.activeEffects = activeEffects;
    }

    protected void encodeBase(FriendlyByteBuf buffer) {
        ALData.toBytes(buffer, playerData);
        buffer.writeVarInt(activeEffects.size());
        for (StatusEffect effect : activeEffects) {
            effect.toBytes(buffer);
        }
    }

    protected static SyncData decodeBase(FriendlyByteBuf buffer) {
        Map<ALData<?>, Object> playerData = ALData.fromBytes(buffer, new HashMap<>());
        int length = buffer.readVarInt();
        List<StatusEffect> activeEffects = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            StatusEffect effect = StatusEffect.fromBytes(buffer);
            if (effect != null) {
                activeEffects.add(effect);
            }
        }
        activeEffects.sort(null);
        return new SyncData(playerData, activeEffects);
    }

    protected void apply(Player player) {
        ALPlayerData playerCapability = ALPlayerData.getData(player);
        ALEntityData entityCapability = ALEntityData.getData(player);

        playerCapability.data.clear();
        for (Map.Entry<ALData<?>, Object> entry : playerData.entrySet()) {
            setWithoutNotify(entry.getKey(), player, entry.getValue());
        }
        entityCapability.activeEffects = new ArrayList<>(activeEffects);
    }

    @SuppressWarnings("unchecked")
    private static <T> void setWithoutNotify(ALData<T> type, Player player, Object value) {
        type.setWithoutNotify(player, (T) value);
    }

    protected record SyncData(
            Map<ALData<?>, Object> playerData,
            List<StatusEffect> activeEffects
    ) {
    }
}
