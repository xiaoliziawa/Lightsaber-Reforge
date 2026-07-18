package com.fiskmods.lightsabers.common.data;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@AutoRegisterCapability
public final class ALPlayerData {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, "player_data");
    public static final Capability<ALPlayerData> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    public Map<ALData<?>, Object> data = new HashMap<>();

    public static ALPlayerData getData(Player player) {
        return player.getCapability(CAPABILITY).orElseThrow(
                () -> new IllegalStateException("Missing Advanced Lightsabers player data capability")
        );
    }

    @Nullable
    public static ALPlayerData getDataOrNull(Player player) {
        return player.getCapability(CAPABILITY).orElse(null);
    }

    public static boolean hasData(Player player) {
        return getDataOrNull(player) != null;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Saved", true);
        return ALData.writeToNBT(tag, data);
    }

    public void load(CompoundTag tag) {
        if (tag.getBoolean("Saved")) {
            ALData.readFromNBT(tag, data);
        }
    }

    public void copy(ALPlayerData source) {
        data = new HashMap<>(source.data);
    }

    public <T> void putData(ALData<T> type, T value) {
        data.put(type, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(ALData<T> type) {
        if (data.containsKey(type)) {
            return (T) data.get(type);
        }
        if (!type.defaultValue.canEqual()) {
            T value = type.getDefault();
            putData(type, value);
            return value;
        }
        return type.getDefault();
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ALPlayerData playerData = new ALPlayerData();
        private LazyOptional<ALPlayerData> optional = createOptional();

        private LazyOptional<ALPlayerData> createOptional() {
            return LazyOptional.of(() -> playerData);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(
                @NotNull Capability<T> capability,
                @Nullable Direction side
        ) {
            if (capability != CAPABILITY) {
                return LazyOptional.empty();
            }
            if (!optional.isPresent()) {
                optional = createOptional();
            }
            return optional.cast();
        }

        @Override
        public CompoundTag serializeNBT() {
            return playerData.save();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            playerData.load(tag);
        }

        public void invalidate() {
            optional.invalidate();
        }
    }

    public static final class PlayerCapabilityEvents {
        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                Provider provider = new Provider();
                event.addCapability(ID, provider);
                event.addListener(provider::invalidate);
            }
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            Player original = event.getOriginal();
            original.reviveCaps();
            try {
                getData(event.getEntity()).copy(getData(original));
            } finally {
                original.invalidateCaps();
            }
        }
    }
}
