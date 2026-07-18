package com.fiskmods.lightsabers.common.damage;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ALDamageTypes {
    public static final ResourceKey<DamageType> INTO_WALL = create("into_wall");
    public static final ResourceKey<DamageType> LIGHTSABER = create("lightsaber");
    public static final ResourceKey<DamageType> FORCE = create("force");
    public static final ResourceKey<DamageType> LIGHTNING = create("lightning");

    private ALDamageTypes() {
    }

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Lightsabers.MODID, name)
        );
    }
}
