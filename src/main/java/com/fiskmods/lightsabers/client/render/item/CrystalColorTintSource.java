package com.fiskmods.lightsabers.client.render.item;

import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CrystalColorTintSource implements ItemTintSource {
    public static final CrystalColorTintSource INSTANCE =
            new CrystalColorTintSource();
    public static final MapCodec<CrystalColorTintSource> MAP_CODEC =
            MapCodec.unit(INSTANCE);

    private CrystalColorTintSource() {
    }

    @Override
    public int calculate(
            ItemStack stack,
            @Nullable ClientLevel level,
            @Nullable LivingEntity owner
    ) {
        return ARGB.opaque(ItemCrystal.get(stack).getRenderColor());
    }

    @Override
    public MapCodec<CrystalColorTintSource> type() {
        return MAP_CODEC;
    }
}
