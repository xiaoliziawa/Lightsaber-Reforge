package com.fiskmods.lightsabers.common.network;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public interface ALPayload extends CustomPacketPayload {
    Map<Class<? extends ALPayload>, Type<? extends ALPayload>> TYPES = new HashMap<>();

    static <T extends ALPayload> Type<T> registerType(Class<T> payloadClass, String name) {
        Type<T> type = new Type<>(
                ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, name)
        );
        TYPES.put(payloadClass, type);
        return type;
    }

    @Override
    default Type<? extends CustomPacketPayload> type() {
        Type<? extends ALPayload> type = TYPES.get(getClass());
        if (type == null) {
            throw new IllegalStateException("Unregistered payload type: " + getClass().getName());
        }
        return type;
    }
}
