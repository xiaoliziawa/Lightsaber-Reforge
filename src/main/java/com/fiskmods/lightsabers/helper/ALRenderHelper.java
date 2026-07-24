package com.fiskmods.lightsabers.helper;

import com.fiskmods.lightsabers.Lightsabers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ALRenderHelper {
    public static final ResourceLocation SHADER_GRAY = ResourceLocation.withDefaultNamespace(
            "shaders/post/desaturate.json"
    );
    public static final ResourceLocation SHADER_BLUE = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "shaders/post/blue.json"
    );
    public static final ResourceLocation SHADER_BLUR = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "shaders/post/mild_phosphor.json"
    );

    public static boolean overrideColor;

    private static int gazeAmplifier = -1;

    private ALRenderHelper() {
    }

    public static void setGazeAmplifier(int amplifier) {
        gazeAmplifier = amplifier;
    }

    public static boolean shouldGazeGlow(Entity entity) {
        if (gazeAmplifier < 0 || !(entity instanceof LivingEntity living)) {
            return false;
        }
        Player viewer = Minecraft.getInstance().player;
        return viewer != null
                && entity != viewer
                && canGazeEntity(viewer, living, gazeAmplifier);
    }

    public static void setLighting(int lighting) {
    }

    public static void resetLighting() {
    }

    public static void overrideColor(boolean override) {
        overrideColor = override;
    }

    public static float median(double current, double previous) {
        return (float) (previous
                + (current - previous)
                * Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));
    }

    public static void startGlScissor(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        double scale = minecraft.getWindow().getGuiScale();
        int scaledX = (int) Math.floor(Math.max(0, x) * scale);
        int scaledY = (int) Math.floor(
                minecraft.getWindow().getHeight() - (Math.max(0, y) + height) * scale
        );
        int scaledWidth = Math.max(0, (int) Math.ceil(width * scale));
        int scaledHeight = Math.max(0, (int) Math.ceil(height * scale));
        RenderSystem.enableScissor(scaledX, scaledY, scaledWidth, scaledHeight);
    }

    public static void endGlScissor() {
        RenderSystem.disableScissor();
    }

    public static void startShaders(ResourceLocation location) {
        Minecraft minecraft = Minecraft.getInstance();
        PostChain currentEffect = minecraft.gameRenderer.currentEffect();
        if (currentEffect == null || !currentEffect.getName().equals(location.toString())) {
            minecraft.gameRenderer.loadEffect(location);
        }
    }

    public static void stopShaders() {
        Minecraft.getInstance().gameRenderer.shutdownEffect();
    }

    public static boolean canGazeEntity(
            Player player,
            LivingEntity entity,
            int amplifier
    ) {
        if (amplifier == 0) {
            return !entity.isInvisibleTo(player) && !(entity instanceof Player);
        }
        if (amplifier == 1) {
            return !entity.isInvisibleTo(player);
        }
        return true;
    }
}
