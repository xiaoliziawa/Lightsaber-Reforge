package com.fiskmods.lightsabers.common.config;

import com.fiskmods.lightsabers.Lightsabers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Lightsabers.MODID)
public final class ModConfig {
    public static final String CATEGORY_DYNAMIC_LIGHTS = "dynamic_lights";
    public static final String CATEGORY_RENDERING = "rendering";

    private static final ModConfigSpec.BooleanValue DYNAMIC_LIGHTS_ENABLED;
    private static final ModConfigSpec.IntValue DYNAMIC_LIGHTS_UPDATE_INTERVAL;
    private static final ModConfigSpec.DoubleValue RENDER_GLOBAL_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue RENDER_WIDTH_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue RENDER_OPACITY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue RENDER_SMOOTHING_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue RENDER_LIGHTING_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue ENABLE_SHADERS;

    public static final ModConfigSpec SPEC;

    public static boolean dynamicLightsEnabled = true;
    public static int dynamicLightsUpdateInterval = 1000;
    public static float renderGlobalMultiplier = 1.0F;
    public static float renderWidthMultiplier = 1.0F;
    public static float renderOpacityMultiplier = 1.0F;
    public static float renderSmoothingMultiplier = 1.0F;
    public static float renderLightingMultiplier = 1.0F;
    public static boolean enableShaders = true;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(CATEGORY_DYNAMIC_LIGHTS);
        DYNAMIC_LIGHTS_ENABLED = builder.define("enabled", true);
        DYNAMIC_LIGHTS_UPDATE_INTERVAL = builder.defineInRange(
                "updateIntervalMilliseconds",
                1000,
                0,
                Integer.MAX_VALUE
        );
        builder.pop();

        builder.push(CATEGORY_RENDERING);
        RENDER_GLOBAL_MULTIPLIER = defineMultiplier(builder, "globalMultiplier");
        RENDER_WIDTH_MULTIPLIER = defineMultiplier(builder, "widthMultiplier");
        RENDER_OPACITY_MULTIPLIER = defineMultiplier(builder, "opacityMultiplier");
        RENDER_SMOOTHING_MULTIPLIER = defineMultiplier(builder, "smoothingMultiplier");
        RENDER_LIGHTING_MULTIPLIER = defineMultiplier(builder, "lightingMultiplier");
        ENABLE_SHADERS = builder.define("enableShaders", true);
        builder.pop();

        SPEC = builder.build();
    }

    private ModConfig() {
    }

    private static ModConfigSpec.DoubleValue defineMultiplier(
            ModConfigSpec.Builder builder,
            String name
    ) {
        return builder.defineInRange(name, 1.0D, 0.0D, Double.MAX_VALUE);
    }

    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            refreshValues();
        }
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            refreshValues();
        }
    }

    private static void refreshValues() {
        dynamicLightsEnabled = DYNAMIC_LIGHTS_ENABLED.get();
        dynamicLightsUpdateInterval = DYNAMIC_LIGHTS_UPDATE_INTERVAL.get();
        renderGlobalMultiplier = RENDER_GLOBAL_MULTIPLIER.get().floatValue();
        renderWidthMultiplier = RENDER_WIDTH_MULTIPLIER.get().floatValue();
        renderOpacityMultiplier = RENDER_OPACITY_MULTIPLIER.get().floatValue();
        renderSmoothingMultiplier = RENDER_SMOOTHING_MULTIPLIER.get().floatValue();
        renderLightingMultiplier = RENDER_LIGHTING_MULTIPLIER.get().floatValue();
        enableShaders = ENABLE_SHADERS.get();
    }
}
