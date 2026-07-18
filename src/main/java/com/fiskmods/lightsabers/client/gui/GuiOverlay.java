package com.fiskmods.lightsabers.client.gui;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.data.ALData;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.force.Power;
import com.fiskmods.lightsabers.common.force.effect.PowerEffectActive;
import com.fiskmods.lightsabers.helper.ALHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class GuiOverlay {
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/icons.png"
    );
    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(
            Lightsabers.MODID,
            "textures/gui/widgets.png"
    );
    private static final int FORCE_BAR_WIDTH = 182;
    private static final int VANILLA_HUD_OFFSET = 9;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null
                || ALHelper.getForcePowerMax(player) <= 0
                || !shouldOffsetVanillaHud(event.getOverlay())) {
            return;
        }
        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(0, -VANILLA_HUD_OFFSET, 0);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void restoreOverlayPose(RenderGuiOverlayEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null
                || ALHelper.getForcePowerMax(player) <= 0
                || !shouldOffsetVanillaHud(event.getOverlay())) {
            return;
        }
        event.getGuiGraphics().pose().popPose();
    }

    private static boolean shouldOffsetVanillaHud(NamedGuiOverlay overlay) {
        return overlay == VanillaGuiOverlay.PLAYER_HEALTH.type()
                || overlay == VanillaGuiOverlay.ARMOR_LEVEL.type()
                || overlay == VanillaGuiOverlay.FOOD_LEVEL.type()
                || overlay == VanillaGuiOverlay.AIR_LEVEL.type()
                || overlay == VanillaGuiOverlay.MOUNT_HEALTH.type()
                || overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type()
                || overlay == VanillaGuiOverlay.JUMP_BAR.type();
    }

    @SubscribeEvent
    public void onRenderOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        renderActiveEffects(player, event.getPartialTick());
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
            GuiGraphics graphics,
            int width,
            int height,
            LocalPlayer player
    ) {
        int cap = ALHelper.getForcePowerMax(player);
        if (cap <= 0) {
            return;
        }

        int left = width / 2 - 91;
        int top = height - 29;
        int filled = Mth.clamp(
                (int) (ALData.FORCE_POWER.interpolate(player) / cap * FORCE_BAR_WIDTH),
                0,
                FORCE_BAR_WIDTH
        );
        int delayed = Mth.clamp(
                (int) (ALData.FORCE_POWER_DIFF.interpolate(player) / cap * FORCE_BAR_WIDTH),
                0,
                FORCE_BAR_WIDTH
        );

        RenderSystem.enableBlend();
        graphics.blit(ICONS, left, top, 0, 74, FORCE_BAR_WIDTH, 5);
        if (delayed > filled) {
            graphics.blit(ICONS, left, top, 0, 79, delayed, 5);
        }
        if (filled > 0) {
            graphics.blit(ICONS, left, top, 0, 84, filled, 5);
        }

        Font font = Minecraft.getInstance().font;
        String value = Mth.floor(ALData.FORCE_POWER.get(player)) + "/" + cap;
        graphics.drawString(
                font,
                value,
                left + (FORCE_BAR_WIDTH - font.width(value)) / 2,
                top - 2,
                0xFFFFFFFF,
                true
        );
        RenderSystem.disableBlend();
    }

    private static void renderPowerSelector(
            GuiGraphics graphics,
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

        RenderSystem.enableBlend();
        graphics.blit(WIDGETS, left, top, 0, 0, 62, 22);
        List<Power> selectedPowers = ALData.SELECTED_POWERS.get(player);
        for (int i = 0; i < Math.min(selectedPowers.size(), 3); i++) {
            Power power = selectedPowers.get(i);
            if (power != null && power.hasIcon()) {
                graphics.blit(
                        ICONS,
                        left + 3 + i * 20,
                        top + 3,
                        power.getIconX() * 16,
                        power.getIconY() * 16,
                        16,
                        16
                );
            }
        }
        graphics.blit(WIDGETS, left - 1 + selected * 20, top - 1, 0, 22, 24, 24);
        RenderSystem.disableBlend();
    }

    private static void renderStatusEffects(
            GuiGraphics graphics,
            int width,
            int height,
            LocalPlayer player
    ) {
        Font font = Minecraft.getInstance().font;
        List<StatusEffect> effects = StatusEffect.get(player);
        int visibleIndex = 0;
        RenderSystem.enableBlend();
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
            graphics.blit(ICONS, right - 26, top, 0, 48, 26, 26);
            if (power.hasIcon()) {
                graphics.blit(
                        ICONS,
                        right - 21,
                        top + 5,
                        power.getIconX() * 16,
                        power.getIconY() * 16,
                        16,
                        16
                );
            }

            String name = status.effect.getFormattedString(status);
            String duration = status.effect.getDurationString(status);
            graphics.drawString(font, name, right - 30 - font.width(name), top + 4, -1, true);
            graphics.drawString(
                    font,
                    duration,
                    right - 30 - font.width(duration),
                    top + 13,
                    -1,
                    true
            );
        }
        RenderSystem.disableBlend();
    }
}
