package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectActive;
import com.fiskmods.lightsabers.helper.ALHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public final class GuiOverlay {
    private static final Identifier ICONS = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/icons.png"
    );
    private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/widgets.png"
    );
    private static final int FORCE_BAR_LENGTH = 182;
    private static final int FORCE_BAR_THICKNESS = 5;
    private static final int FORCE_BAR_RIGHT_MARGIN = 8;
    private static final int TEXT_COLOR_FULL = 0xFF55FF55;
    private static final int TEXT_COLOR_CONSUME = 0xFFFFAA00;
    private static final int TEXT_COLOR_LOW = 0xFFFF5555;
    private static final float TEXT_LOW_THRESHOLD = 0.3F;
    private static final float TEXT_CONSUME_SENSITIVITY = 0.05F;

    @SubscribeEvent
    public void onRenderOverlayPost(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        renderActiveEffects(
                player,
                event.getPartialTick().getGameTimeDeltaPartialTick(true)
        );
        renderForceBar(graphics, width, height, player);
        renderPowerSelector(graphics, width, height, player);
        renderStatusEffects(graphics, width, height, player);
    }

    private static void renderActiveEffects(LocalPlayer player, float partialTicks) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            for (StatusEffect status : StatusEffect.get(player)) {
                Power power = status.effect.getPower(status.amplifier);
                if (power != null && power.powerEffect instanceof PowerEffectActive active) {
                    active.render(player, partialTicks);
                }
            }
        }
    }

    private static void renderForceBar(
            GuiGraphicsExtractor graphics,
            int width,
            int height,
            LocalPlayer player
    ) {
        int cap = ALHelper.getForcePowerMax(player);
        if (cap <= 0) {
            return;
        }

        int filled = Mth.clamp(
                (int) (ALDataInterp.FORCE_POWER.interpolate(player) / cap * FORCE_BAR_LENGTH),
                0,
                FORCE_BAR_LENGTH
        );
        int delayed = Mth.clamp(
                (int) (ALDataInterp.FORCE_POWER_DIFF.interpolate(player) / cap * FORCE_BAR_LENGTH),
                0,
                FORCE_BAR_LENGTH
        );

        int barX = width - FORCE_BAR_RIGHT_MARGIN - FORCE_BAR_THICKNESS;
        int barBottom = (height + FORCE_BAR_LENGTH) / 2;
        int barTop = (height - FORCE_BAR_LENGTH) / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(barX, barBottom);
        graphics.pose().rotate((float) -Math.PI / 2.0F);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ICONS,
                0,
                0,
                0,
                74,
                FORCE_BAR_LENGTH,
                FORCE_BAR_THICKNESS,
                256,
                256
        );
        if (delayed > filled) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ICONS,
                    0,
                    0,
                    0,
                    79,
                    delayed,
                    FORCE_BAR_THICKNESS,
                    256,
                    256
            );
        }
        if (filled > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ICONS,
                    0,
                    0,
                    0,
                    84,
                    filled,
                    FORCE_BAR_THICKNESS,
                    256,
                    256
            );
        }
        graphics.pose().popMatrix();

        Font font = Minecraft.getInstance().font;
        String current = String.valueOf(Mth.floor(ALDataInterp.FORCE_POWER.get(player)));
        int barCenterX = barX + FORCE_BAR_THICKNESS / 2;
        graphics.text(
                font,
                current,
                barCenterX - font.width(current) / 2,
                barTop - font.lineHeight - 1,
                forceTextColor(filled, delayed),
                true
        );
    }

    private static int forceTextColor(int filled, int delayed) {
        float ratio = filled / (float) FORCE_BAR_LENGTH;
        float consumeStrength = Mth.clamp(
                (delayed - filled) / (float) FORCE_BAR_LENGTH / TEXT_CONSUME_SENSITIVITY,
                0F,
                1F
        );
        int baseColor = ratio <= TEXT_LOW_THRESHOLD
                ? TEXT_COLOR_LOW
                : lerpColor(
                        TEXT_COLOR_LOW,
                        TEXT_COLOR_FULL,
                        (ratio - TEXT_LOW_THRESHOLD) / (1F - TEXT_LOW_THRESHOLD)
                );
        return lerpColor(baseColor, TEXT_COLOR_CONSUME, consumeStrength);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Mth.clamp(t, 0F, 1F);
        int a = (int) Mth.lerp(t, (from >> 24) & 0xFF, (to >> 24) & 0xFF);
        int r = (int) Mth.lerp(t, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = (int) Mth.lerp(t, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void renderPowerSelector(
            GuiGraphicsExtractor graphics,
            int width,
            int height,
            LocalPlayer player
    ) {
        if (ALHelper.getForcePowerMax(player) <= 0) {
            return;
        }
        int left = width / 2 - 184;
        int top = height - 22;
        int selected = Mth.clamp(ALData.SELECTED_POWER.get(player), 0, 2);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                WIDGETS,
                left,
                top,
                0,
                0,
                62,
                22,
                256,
                256
        );
        List<Power> selectedPowers = ALData.SELECTED_POWERS.get(player);
        for (int i = 0; i < Math.min(selectedPowers.size(), 3); i++) {
            Power power = selectedPowers.get(i);
            if (power != null && power.hasIcon()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        ICONS,
                        left + 3 + i * 20,
                        top + 3,
                        power.getIconX() * 16,
                        power.getIconY() * 16,
                        16,
                        16,
                        256,
                        256
                );
            }
        }
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                WIDGETS,
                left - 1 + selected * 20,
                top - 1,
                0,
                22,
                24,
                24,
                256,
                256
        );
    }

    private static void renderStatusEffects(
            GuiGraphicsExtractor graphics,
            int width,
            int height,
            LocalPlayer player
    ) {
        Font font = Minecraft.getInstance().font;
        List<StatusEffect> effects = StatusEffect.get(player);
        int visibleIndex = 0;
        for (StatusEffect status : effects) {
            if (status.duration < 0) {
                continue;
            }
            Power power = status.effect.getPower(status.amplifier);
            if (power == null) {
                continue;
            }

            int right = width - 3;
            int top = height - 54 - 28 * visibleIndex++;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ICONS,
                    right - 26,
                    top,
                    0,
                    48,
                    26,
                    26,
                    256,
                    256
            );
            if (power.hasIcon()) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        ICONS,
                        right - 21,
                        top + 5,
                        power.getIconX() * 16,
                        power.getIconY() * 16,
                        16,
                        16,
                        256,
                        256
                );
            }

            String name = status.effect.getFormattedString(status);
            String duration = status.effect.getDurationString(status);
            graphics.text(font, name, right - 30 - font.width(name), top + 4, -1, true);
            graphics.text(
                    font,
                    duration,
                    right - 30 - font.width(duration),
                    top + 13,
                    -1,
                    true
            );
        }
    }
}
