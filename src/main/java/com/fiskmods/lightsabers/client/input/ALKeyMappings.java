package com.fiskmods.lightsabers.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class ALKeyMappings {
    private static final String CATEGORY = "key.categories.lightsabers";

    public static final KeyMapping ACTIVATE_LIGHTSABER = new KeyMapping(
            "key.lightsabers.activate_lightsaber",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping ACTIVATE_POWER = new KeyMapping(
            "key.lightsabers.activate_power",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
    );
    public static final KeyMapping SELECT_POWER = new KeyMapping(
            "key.lightsabers.select_power",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            CATEGORY
    );
    public static final KeyMapping FLIP_DOUBLE_LIGHTSABER = new KeyMapping(
            "key.lightsabers.flip_double_lightsaber",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    private ALKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE_LIGHTSABER);
        event.register(ACTIVATE_POWER);
        event.register(SELECT_POWER);
        event.register(FLIP_DOUBLE_LIGHTSABER);
    }
}
