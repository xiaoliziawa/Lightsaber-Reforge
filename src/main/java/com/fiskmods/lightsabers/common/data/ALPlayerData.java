package com.fiskmods.lightsabers.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ALPlayerData implements ModAttachments.PersistentData {

    public Map<ALData<?>, Object> data = new HashMap<>();

    public static ALPlayerData getData(Player player) {
        return player.getData(ModAttachments.PLAYER_DATA);
    }

    @Nullable
    public static ALPlayerData getDataOrNull(Player player) {
        return player.getExistingDataOrNull(ModAttachments.PLAYER_DATA);
    }

    public static boolean hasData(Player player) {
        return true;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Saved", true);
        return ALData.writeToNBT(tag, data);
    }

    @Override
    public void load(CompoundTag tag) {
        data.clear();
        ALData.readFromNBT(tag, data);
    }

    public void copy(ALPlayerData source) {
        load(source.save());
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

}
