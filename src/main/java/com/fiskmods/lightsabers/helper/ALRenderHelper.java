package com.fiskmods.lightsabers.helper;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.mixin.PostChainAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ALRenderHelper {
    public static final Identifier SHADER_GRAY = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "desaturate"
    );
    public static final Identifier SHADER_BLUE = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "blue"
    );
    public static final Identifier SHADER_BLUR = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "mild_phosphor"
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
                * Minecraft.getInstance()
                        .getDeltaTracker()
                        .getGameTimeDeltaPartialTick(true));
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
        RenderSystem.enableScissorForRenderTypeDraws(
                scaledX,
                scaledY,
                scaledWidth,
                scaledHeight
        );
    }

    public static void endGlScissor() {
        RenderSystem.disableScissorForRenderTypeDraws();
    }

    public static void startShaders(Identifier location) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!location.equals(minecraft.gameRenderer.currentPostEffect())) {
            if (location.equals(SHADER_BLUR)) {
                resetPersistentTargets(minecraft, location);
            }
            minecraft.gameRenderer.setPostEffect(location);
        }
    }

    public static void stopShaders() {
        Minecraft.getInstance().gameRenderer.clearPostEffect();
    }

    private static void resetPersistentTargets(
            Minecraft minecraft,
            Identifier location
    ) {
        PostChain postChain = minecraft.getShaderManager()
                .getPostChain(location, LevelTargetBundle.MAIN_TARGETS);
        if (!(postChain instanceof PostChainAccessor accessor)) {
            return;
        }
        for (RenderTarget target : accessor.lightsabers$getPersistentTargets().values()) {
            target.destroyBuffers();
        }
        accessor.lightsabers$getPersistentTargets().clear();
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
