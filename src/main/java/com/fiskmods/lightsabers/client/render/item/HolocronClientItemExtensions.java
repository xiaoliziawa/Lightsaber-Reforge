package com.fiskmods.lightsabers.client.render.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class HolocronClientItemExtensions implements IClientItemExtensions {
    public static final HolocronClientItemExtensions INSTANCE =
            new HolocronClientItemExtensions();

    private final HolocronItemRenderer renderer = new HolocronItemRenderer();

    private HolocronClientItemExtensions() {
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
    }
}
