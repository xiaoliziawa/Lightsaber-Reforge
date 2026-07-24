package com.fiskmods.lightsabers.client.render.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class CrystalClientItemExtensions implements IClientItemExtensions {
    public static final CrystalClientItemExtensions INSTANCE =
            new CrystalClientItemExtensions();

    private final CrystalItemRenderer renderer = new CrystalItemRenderer();

    private CrystalClientItemExtensions() {
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
    }
}
