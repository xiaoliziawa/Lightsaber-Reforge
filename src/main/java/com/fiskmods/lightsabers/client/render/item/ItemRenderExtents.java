package com.fiskmods.lightsabers.client.render.item;

import java.util.function.Supplier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ItemRenderExtents {
    private static final Vector3fc[] GROUNDED_EXTENTS = {
            new Vector3f(-0.5F, 0.0F, -0.5F),
            new Vector3f(-0.5F, 0.0F, 0.5F),
            new Vector3f(-0.5F, 1.0F, -0.5F),
            new Vector3f(-0.5F, 1.0F, 0.5F),
            new Vector3f(0.5F, 0.0F, -0.5F),
            new Vector3f(0.5F, 0.0F, 0.5F),
            new Vector3f(0.5F, 1.0F, -0.5F),
            new Vector3f(0.5F, 1.0F, 0.5F)
    };
    private static final Supplier<Vector3fc[]> GROUNDED_EXTENTS_SUPPLIER =
            () -> GROUNDED_EXTENTS;

    private ItemRenderExtents() {
    }

    public static Supplier<Vector3fc[]> forDisplayContext(
            ItemDisplayContext displayContext,
            Supplier<Vector3fc[]> defaultExtents
    ) {
        return displayContext == ItemDisplayContext.GROUND
                ? GROUNDED_EXTENTS_SUPPLIER
                : defaultExtents;
    }
}
