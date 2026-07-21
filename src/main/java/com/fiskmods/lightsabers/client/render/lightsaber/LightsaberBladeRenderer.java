package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public final class LightsaberBladeRenderer {
    public static final int MAIN_BLADE_LENGTH = 38;
    public static final int CROSSGUARD_BLADE_LENGTH = 4;

    public static float bladeRoll;

    private static final float PIXEL = 1.0F / 16.0F;
    private static final float CORE_HALF_WIDTH = PIXEL / 2.0F;
    private static final float TIP_LENGTH = PIXEL * 2.0F;
    private static final int MAX_GLOW_LAYERS = 80;
    private static final int PICK_ARM_SEGMENTS = 12;
    private static final float PICK_ARM_RADIUS = PIXEL * 9.0F;
    private static final float PICK_ARM_SWEEP = 1.9F;
    private static final float PICK_ARM_BASE_HALF_HEIGHT = PIXEL * 1.1F;
    private static final float PICK_ARM_TIP_HALF_HEIGHT = PIXEL * 0.12F;
    private static final float PICK_GLOW_EXPANSION = 0.12F;
    private static final int KATANA_SEGMENTS = 12;
    private static final float KATANA_CURVATURE = 0.08F;
    private static final float KATANA_TIP_START = 0.72F;
    private static final float KATANA_TIP_MIN_WIDTH = 0.08F;
    private static final float KATANA_WIDTH_SCALE = 0.6F;
    private static final float KATANA_EDGE_SCALE = 1.5F;
    private static final int CYLINDER_SIDES = 10;
    private static final float CYLINDER_RADIUS_SCALE = 1.8F;
    private static final float DAGGER_LENGTH_SCALE = 0.42F;
    private static final float DAGGER_WIDTH_SCALE = 1.25F;
    private static final float DAGGER_TAPER_START = 0.55F;
    private static final float DAGGER_MID_SCALE = 0.7F;
    private static final float DAGGER_TIP_SCALE = 0.04F;
    private static final float DITHER_GOLDEN_RATIO = 0.618034F;
    private static final float WHITE_GLOW_BRIGHTNESS = 0.62F;

    private LightsaberBladeRenderer() {
    }

    public static float getBladeLength(int bladeLength) {
        return bladeLength * PIXEL;
    }

    public static void render(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            boolean inWorld,
            int bladeLength,
            boolean crossguard
    ) {
        float[] rgb = data.getRGB(stack);
        BladeStyle style = BladeStyle.of(data);
        boolean rollBlade = !crossguard && style.katana() && bladeRoll != 0.0F;
        if (rollBlade) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(bladeRoll));
        }

        renderGlow(
                stack,
                poseStack,
                buffer,
                rgb,
                inWorld,
                bladeLength,
                crossguard,
                style
        );

        float coreRed = style.inverting() ? 0.0F : style.prismatic() ? rgb[0] : 1.0F;
        float coreGreen = style.inverting() ? 0.0F : style.prismatic() ? rgb[1] : 1.0F;
        float coreBlue = style.inverting() ? 0.0F : style.prismatic() ? rgb[2] : 1.0F;
        float widthScale = style.compressed() ? 0.6F : 1.0F;
        float lengthScale = crossguard && style.fineCut() ? 1.2F : 1.0F;
        float length = getBladeLength(bladeLength) * lengthScale;
        float xHalf = CORE_HALF_WIDTH * widthScale * (style.fineCut() ? 0.75F : 1.0F);
        float zHalf = CORE_HALF_WIDTH * widthScale * (style.fineCut() ? 1.5F : 1.0F);
        if (!crossguard) {
            if (style.katana()) {
                xHalf *= KATANA_EDGE_SCALE;
                zHalf *= KATANA_WIDTH_SCALE;
            } else if (style.dagger()) {
                xHalf *= DAGGER_WIDTH_SCALE;
                zHalf *= DAGGER_WIDTH_SCALE;
                length *= DAGGER_LENGTH_SCALE;
            } else if (style.cylinder()) {
                xHalf *= CYLINDER_RADIUS_SCALE;
                zHalf *= CYLINDER_RADIUS_SCALE;
            }
        }
        VertexConsumer core = buffer.getBuffer(LightsaberRenderTypes.BLADE_CORE);
        Matrix4f matrix = poseStack.last().pose();

        renderCoreShape(
                core,
                matrix,
                style,
                crossguard,
                0.0F,
                0.0F,
                xHalf,
                zHalf,
                length,
                coreRed,
                coreGreen,
                coreBlue,
                1.0F
        );
        if (style.cracked() && !style.fineCut()) {
            renderCrackedCopies(
                    core,
                    matrix,
                    style,
                    crossguard,
                    xHalf,
                    zHalf,
                    length,
                    coreRed,
                    coreGreen,
                    coreBlue
            );
        }
        if (style.pickaxe() && !crossguard) {
            renderPickaxeHead(
                    core,
                    matrix,
                    length,
                    zHalf,
                    1.0F,
                    coreRed,
                    coreGreen,
                    coreBlue,
                    1.0F
            );
        }
        if (rollBlade) {
            poseStack.popPose();
        }
    }

    private record BladeStyle(
            boolean compressed,
            boolean fineCut,
            boolean inverting,
            boolean prismatic,
            boolean cracked,
            boolean pickaxe,
            boolean katana,
            boolean cylinder,
            boolean dagger
    ) {
        static BladeStyle of(LightsaberData data) {
            return new BladeStyle(
                    data.hasFocusingCrystal(FocusingCrystal.COMPRESSED),
                    data.hasFocusingCrystal(FocusingCrystal.FINE_CUT),
                    data.hasFocusingCrystal(FocusingCrystal.INVERTING),
                    data.hasFocusingCrystal(FocusingCrystal.PRISMATIC),
                    data.hasFocusingCrystal(FocusingCrystal.CRACKED),
                    data.hasFocusingCrystal(FocusingCrystal.PICKAXE),
                    data.hasFocusingCrystal(FocusingCrystal.KATANA),
                    data.hasFocusingCrystal(FocusingCrystal.CYLINDER),
                    data.hasFocusingCrystal(FocusingCrystal.DAGGER)
            );
        }

        boolean darkGlow() {
            return inverting && prismatic;
        }
    }

    private static void renderCoreShape(
            VertexConsumer consumer,
            Matrix4f matrix,
            BladeStyle style,
            boolean crossguard,
            float offsetX,
            float offsetZ,
            float xHalf,
            float zHalf,
            float length,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (!crossguard) {
            if (style.katana()) {
                renderKatanaBlade(consumer, matrix, offsetX, offsetZ, 0.0F, xHalf, zHalf, length, red, green, blue, alpha);
                return;
            }
            if (style.dagger()) {
                renderDaggerBlade(consumer, matrix, offsetX, offsetZ, xHalf, zHalf, length, red, green, blue, alpha);
                return;
            }
            if (style.cylinder()) {
                renderCylinderBlade(consumer, matrix, offsetX, offsetZ, xHalf, zHalf, length, red, green, blue, alpha);
                return;
            }
        }
        renderBladeGeometry(consumer, matrix, offsetX, offsetZ, xHalf, zHalf, length, red, green, blue, alpha);
    }

    private static void renderGlow(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float[] rgb,
            boolean inWorld,
            int bladeLength,
            boolean crossguard,
            BladeStyle style
    ) {
        boolean compressed = style.compressed();
        boolean fineCut = style.fineCut();
        boolean darkGlow = style.darkGlow();
        int smoothing = compressed ? 7 : 10;
        float width = compressed ? 0.28F : crossguard ? 0.2F : 0.3F;
        float opacity = compressed ? 0.055F : 0.075F;
        float xScale = fineCut ? 0.55F : 1.0F;
        float yScale = fineCut ? 0.925F : 1.0F;
        float zScale = fineCut ? (crossguard ? 1.3F : 1.1F) : 1.0F;
        if (crossguard && compressed) {
            yScale *= 0.9F;
        }
        if (!crossguard) {
            if (style.katana()) {
                xScale *= KATANA_EDGE_SCALE;
                zScale *= KATANA_WIDTH_SCALE;
            } else if (style.dagger()) {
                xScale *= DAGGER_WIDTH_SCALE;
                zScale *= DAGGER_WIDTH_SCALE;
                yScale *= DAGGER_LENGTH_SCALE;
            } else if (style.cylinder()) {
                xScale *= CYLINDER_RADIUS_SCALE;
                zScale *= CYLINDER_RADIUS_SCALE;
            }
        }

        if (inWorld) {
            width *= ModConfig.renderGlobalMultiplier * ModConfig.renderWidthMultiplier;
            smoothing = (int) (smoothing
                    * ModConfig.renderGlobalMultiplier
                    * ModConfig.renderSmoothingMultiplier);
        }
        if (stack.getHoverName().getString().equals("jeb_")) {
            smoothing = (int) (smoothing * 0.25F);
        }
        smoothing = Mth.clamp(smoothing, 1, MAX_GLOW_LAYERS / 5);
        int layerCount = Math.min(5 * smoothing, MAX_GLOW_LAYERS);
        float opacityMultiplier = inWorld
                ? ModConfig.renderGlobalMultiplier * ModConfig.renderOpacityMultiplier
                : 1.0F;
        float layerAlpha = opacity / smoothing * opacityMultiplier;
        boolean whiteBlade = !darkGlow
                && rgb[0] > 0.98F
                && rgb[1] > 0.98F
                && rgb[2] > 0.98F;
        float red = darkGlow ? 0.0F : whiteBlade ? WHITE_GLOW_BRIGHTNESS : rgb[0];
        float green = darkGlow ? 0.0F : whiteBlade ? WHITE_GLOW_BRIGHTNESS : rgb[1];
        float blue = darkGlow ? 0.0F : whiteBlade ? WHITE_GLOW_BRIGHTNESS : rgb[2];
        VertexConsumer glow = DeferredGlowRenderer.getBuffer(
                buffer,
                darkGlow
                        ? LightsaberRenderTypes.BLADE_DARK_GLOW
                        : LightsaberRenderTypes.BLADE_GLOW
        );
        Matrix4f matrix = poseStack.last().pose();
        float baseLength = getBladeLength(bladeLength);

        boolean katanaGlow = !crossguard && style.katana();
        for (int layer = 0; layer < layerCount; layer++) {
            float progress = layer / (float) layerCount * 50.0F;
            float radialScale = 1.0F + layer * (width / smoothing);
            float verticalScale = crossguard
                    ? (3.0F - progress * 0.05F) * yScale
                    : (1.2F - progress * (fineCut ? 0.003F : 0.005F)) * yScale;
            float yOffset = (-progress / 400.0F + 0.06F) * verticalScale;
            float xHalf = CORE_HALF_WIDTH * radialScale * xScale;
            float zHalf = CORE_HALF_WIDTH * radialScale * zScale;
            float length = baseLength * verticalScale;
            float fineCutOffset = fineCut ? 0.005F + progress * 0.00001F : 0.0F;
            float dither = (layer * DITHER_GOLDEN_RATIO) % 1.0F;
            float layerRed = darkGlow ? red : premultiply(red, layerAlpha, dither);
            float layerGreen = darkGlow ? green : premultiply(green, layerAlpha, dither);
            float layerBlue = darkGlow ? blue : premultiply(blue, layerAlpha, dither);
            float layerOutAlpha = darkGlow ? layerAlpha : 1.0F;
            if (katanaGlow) {
                renderKatanaBlade(
                        glow,
                        matrix,
                        0.0F,
                        0.0F,
                        yOffset,
                        xHalf,
                        zHalf,
                        length,
                        layerRed,
                        layerGreen,
                        layerBlue,
                        layerOutAlpha
                );
            } else {
                renderPrism(
                        glow,
                        matrix,
                        -xHalf,
                        yOffset - length,
                        -zHalf + fineCutOffset,
                        xHalf,
                        yOffset,
                        zHalf + fineCutOffset,
                        layerRed,
                        layerGreen,
                        layerBlue,
                        layerOutAlpha
                );
            }
        }

        if (style.pickaxe() && !crossguard) {
            float armAlpha = layerAlpha * 5.0F;
            for (int layer = 0; layer < smoothing; layer++) {
                float expansion = 1.0F + (layer + 1) * PICK_GLOW_EXPANSION;
                float dither = (layer * DITHER_GOLDEN_RATIO) % 1.0F;
                renderPickaxeHead(
                        glow,
                        matrix,
                        baseLength,
                        CORE_HALF_WIDTH * expansion,
                        expansion,
                        darkGlow ? red : premultiply(red, armAlpha, dither),
                        darkGlow ? green : premultiply(green, armAlpha, dither),
                        darkGlow ? blue : premultiply(blue, armAlpha, dither),
                        darkGlow ? armAlpha : 1.0F
                );
            }
        }
    }

    private static float premultiply(float component, float alpha, float dither) {
        int quantized = (int) (component * alpha * 255.0F + dither);
        return (quantized + 0.5F) / 255.0F;
    }

    private static void renderCrackedCopies(
            VertexConsumer consumer,
            Matrix4f matrix,
            BladeStyle style,
            boolean crossguard,
            float xHalf,
            float zHalf,
            float length,
            float red,
            float green,
            float blue
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        long tick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        float partialTick = minecraft.getFrameTime();
        for (int copy = 1; copy < 4; copy++) {
            float currentX = randomSigned(tick, copy * 2) / 120.0F;
            float previousX = randomSigned(tick - 1L, copy * 2) / 120.0F;
            float currentZ = randomSigned(tick, copy * 2 + 1) / 120.0F;
            float previousZ = randomSigned(tick - 1L, copy * 2 + 1) / 120.0F;
            renderCoreShape(
                    consumer,
                    matrix,
                    style,
                    crossguard,
                    Mth.lerp(partialTick, previousX, currentX),
                    Mth.lerp(partialTick, previousZ, currentZ),
                    xHalf,
                    zHalf,
                    length,
                    red,
                    green,
                    blue,
                    1.0F
            );
        }
    }

    private static void renderPickaxeHead(
            VertexConsumer consumer,
            Matrix4f matrix,
            float length,
            float zHalf,
            float heightScale,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float baseY = -length + PICK_ARM_BASE_HALF_HEIGHT;
        for (int side = -1; side <= 1; side += 2) {
            float prevTopX = 0.0F;
            float prevTopY = 0.0F;
            float prevBottomX = 0.0F;
            float prevBottomY = 0.0F;
            float topX = 0.0F;
            float topY = 0.0F;
            float bottomX = 0.0F;
            float bottomY = 0.0F;
            for (int segment = 0; segment <= PICK_ARM_SEGMENTS; segment++) {
                float progress = segment / (float) PICK_ARM_SEGMENTS;
                float angle = progress * PICK_ARM_SWEEP;
                float sin = Mth.sin(angle);
                float cos = Mth.cos(angle);
                float centerX = side * PICK_ARM_RADIUS * sin;
                float centerY = baseY + PICK_ARM_RADIUS * (1.0F - cos);
                float halfHeight = Mth.lerp(progress, PICK_ARM_BASE_HALF_HEIGHT, PICK_ARM_TIP_HALF_HEIGHT) * heightScale;
                float normalX = -side * sin * halfHeight;
                float normalY = cos * halfHeight;
                prevTopX = topX;
                prevTopY = topY;
                prevBottomX = bottomX;
                prevBottomY = bottomY;
                topX = centerX + normalX;
                topY = centerY + normalY;
                bottomX = centerX - normalX;
                bottomY = centerY - normalY;
                if (segment == 0) {
                    continue;
                }
                quad(consumer, matrix, prevTopX, prevTopY, -zHalf, topX, topY, -zHalf, topX, topY, zHalf, prevTopX, prevTopY, zHalf, red, green, blue, alpha);
                quad(consumer, matrix, prevBottomX, prevBottomY, zHalf, bottomX, bottomY, zHalf, bottomX, bottomY, -zHalf, prevBottomX, prevBottomY, -zHalf, red, green, blue, alpha);
                quad(consumer, matrix, prevTopX, prevTopY, zHalf, topX, topY, zHalf, bottomX, bottomY, zHalf, prevBottomX, prevBottomY, zHalf, red, green, blue, alpha);
                quad(consumer, matrix, prevBottomX, prevBottomY, -zHalf, bottomX, bottomY, -zHalf, topX, topY, -zHalf, prevTopX, prevTopY, -zHalf, red, green, blue, alpha);
            }
            quad(consumer, matrix, topX, topY, -zHalf, topX, topY, zHalf, bottomX, bottomY, zHalf, bottomX, bottomY, -zHalf, red, green, blue, alpha);
        }
    }

    private static void renderKatanaBlade(
            VertexConsumer consumer,
            Matrix4f matrix,
            float offsetX,
            float offsetZ,
            float offsetY,
            float xHalf,
            float zHalf,
            float length,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float prevY = 0.0F;
        float prevMinX = 0.0F;
        float prevMaxX = 0.0F;
        float prevMinZ = 0.0F;
        float prevMaxZ = 0.0F;
        for (int segment = 0; segment <= KATANA_SEGMENTS; segment++) {
            float progress = segment / (float) KATANA_SEGMENTS;
            float y = offsetY - length * progress;
            float curve = offsetX - length * KATANA_CURVATURE * progress * progress;
            float tipProgress = Mth.clamp((progress - KATANA_TIP_START) / (1.0F - KATANA_TIP_START), 0.0F, 1.0F);
            float thickness = zHalf * Mth.lerp(tipProgress, 1.0F, KATANA_TIP_MIN_WIDTH);
            float minX = curve - xHalf;
            float maxX = curve + xHalf * (1.0F - 2.0F * tipProgress);
            float minZ = offsetZ - thickness;
            float maxZ = offsetZ + thickness;
            if (segment == 0) {
                capQuad(consumer, matrix, y, minX, maxX, minZ, maxZ, red, green, blue, alpha);
            } else {
                loftSegment(consumer, matrix, prevY, prevMinX, prevMaxX, prevMinZ, prevMaxZ, y, minX, maxX, minZ, maxZ, red, green, blue, alpha);
            }
            prevY = y;
            prevMinX = minX;
            prevMaxX = maxX;
            prevMinZ = minZ;
            prevMaxZ = maxZ;
        }
    }

    private static void renderDaggerBlade(
            VertexConsumer consumer,
            Matrix4f matrix,
            float offsetX,
            float offsetZ,
            float xHalf,
            float zHalf,
            float length,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float minX = offsetX - xHalf;
        float maxX = offsetX + xHalf;
        float minZ = offsetZ - zHalf;
        float maxZ = offsetZ + zHalf;
        float midY = -length * DAGGER_TAPER_START;
        float midXHalf = xHalf * DAGGER_MID_SCALE;
        float midZHalf = zHalf * DAGGER_MID_SCALE;
        float tipXHalf = xHalf * DAGGER_TIP_SCALE;
        float tipZHalf = zHalf * DAGGER_TIP_SCALE;
        capQuad(consumer, matrix, 0.0F, minX, maxX, minZ, maxZ, red, green, blue, alpha);
        loftSegment(consumer, matrix, 0.0F, minX, maxX, minZ, maxZ,
                midY, offsetX - midXHalf, offsetX + midXHalf, offsetZ - midZHalf, offsetZ + midZHalf, red, green, blue, alpha);
        loftSegment(consumer, matrix, midY, offsetX - midXHalf, offsetX + midXHalf, offsetZ - midZHalf, offsetZ + midZHalf,
                -length, offsetX - tipXHalf, offsetX + tipXHalf, offsetZ - tipZHalf, offsetZ + tipZHalf, red, green, blue, alpha);
    }

    private static void renderCylinderBlade(
            VertexConsumer consumer,
            Matrix4f matrix,
            float offsetX,
            float offsetZ,
            float radiusX,
            float radiusZ,
            float length,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float step = (float) (Math.PI * 2.0 / CYLINDER_SIDES);
        float bottom = -length;
        float prevX = offsetX + radiusX;
        float prevZ = offsetZ;
        for (int side = 1; side <= CYLINDER_SIDES; side++) {
            float angle = side * step;
            float x = offsetX + radiusX * Mth.cos(angle);
            float z = offsetZ + radiusZ * Mth.sin(angle);
            quad(consumer, matrix, prevX, 0.0F, prevZ, x, 0.0F, z, x, bottom, z, prevX, bottom, prevZ, red, green, blue, alpha);
            triangle(consumer, matrix, prevX, 0.0F, prevZ, x, 0.0F, z, offsetX, 0.0F, offsetZ, red, green, blue, alpha);
            triangle(consumer, matrix, prevX, bottom, prevZ, x, bottom, z, offsetX, bottom, offsetZ, red, green, blue, alpha);
            prevX = x;
            prevZ = z;
        }
    }

    private static void loftSegment(
            VertexConsumer consumer,
            Matrix4f matrix,
            float prevY,
            float prevMinX,
            float prevMaxX,
            float prevMinZ,
            float prevMaxZ,
            float y,
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        quad(consumer, matrix, prevMinX, prevY, prevMinZ, prevMaxX, prevY, prevMinZ, maxX, y, minZ, minX, y, minZ, red, green, blue, alpha);
        quad(consumer, matrix, prevMaxX, prevY, prevMaxZ, prevMinX, prevY, prevMaxZ, minX, y, maxZ, maxX, y, maxZ, red, green, blue, alpha);
        quad(consumer, matrix, prevMinX, prevY, prevMaxZ, prevMinX, prevY, prevMinZ, minX, y, minZ, minX, y, maxZ, red, green, blue, alpha);
        quad(consumer, matrix, prevMaxX, prevY, prevMinZ, prevMaxX, prevY, prevMaxZ, maxX, y, maxZ, maxX, y, minZ, red, green, blue, alpha);
    }

    private static void capQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            float y,
            float minX,
            float maxX,
            float minZ,
            float maxZ,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        quad(consumer, matrix, minX, y, minZ, maxX, y, minZ, maxX, y, maxZ, minX, y, maxZ, red, green, blue, alpha);
    }

    private static float randomSigned(long tick, int salt) {
        long value = tick * 341873128712L + salt * 132897987541L;
        value ^= value >>> 13;
        value *= 1274126177L;
        value ^= value >>> 16;
        return ((value & 0xFFFFL) / 65535.0F) - 0.5F;
    }

    private static void renderBladeGeometry(
            VertexConsumer consumer,
            Matrix4f matrix,
            float offsetX,
            float offsetZ,
            float xHalf,
            float zHalf,
            float length,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float bottom = -length;
        renderPrism(
                consumer,
                matrix,
                offsetX - xHalf,
                bottom,
                offsetZ - zHalf,
                offsetX + xHalf,
                0.0F,
                offsetZ + zHalf,
                red,
                green,
                blue,
                alpha
        );
        renderTip(
                consumer,
                matrix,
                offsetX,
                offsetZ,
                xHalf,
                zHalf,
                bottom,
                red,
                green,
                blue,
                alpha
        );
    }

    private static void renderPrism(
            VertexConsumer consumer,
            Matrix4f matrix,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        quad(consumer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        quad(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        quad(consumer, matrix, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, red, green, blue, alpha);
        quad(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, red, green, blue, alpha);
        quad(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        quad(consumer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, red, green, blue, alpha);
    }

    private static void renderTip(
            VertexConsumer consumer,
            Matrix4f matrix,
            float centerX,
            float centerZ,
            float xHalf,
            float zHalf,
            float baseY,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        float tipY = baseY - TIP_LENGTH;
        triangle(consumer, matrix, centerX - xHalf, baseY, centerZ - zHalf, centerX + xHalf, baseY, centerZ - zHalf, centerX, tipY, centerZ, red, green, blue, alpha);
        triangle(consumer, matrix, centerX + xHalf, baseY, centerZ - zHalf, centerX + xHalf, baseY, centerZ + zHalf, centerX, tipY, centerZ, red, green, blue, alpha);
        triangle(consumer, matrix, centerX + xHalf, baseY, centerZ + zHalf, centerX - xHalf, baseY, centerZ + zHalf, centerX, tipY, centerZ, red, green, blue, alpha);
        triangle(consumer, matrix, centerX - xHalf, baseY, centerZ + zHalf, centerX - xHalf, baseY, centerZ - zHalf, centerX, tipY, centerZ, red, green, blue, alpha);
    }

    private static void triangle(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        vertex(consumer, matrix, x1, y1, z1, red, green, blue, alpha);
        vertex(consumer, matrix, x2, y2, z2, red, green, blue, alpha);
        vertex(consumer, matrix, x3, y3, z3, red, green, blue, alpha);
        vertex(consumer, matrix, x3, y3, z3, red, green, blue, alpha);
    }

    private static void quad(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float x4,
            float y4,
            float z4,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        vertex(consumer, matrix, x1, y1, z1, red, green, blue, alpha);
        vertex(consumer, matrix, x2, y2, z2, red, green, blue, alpha);
        vertex(consumer, matrix, x3, y3, z3, red, green, blue, alpha);
        vertex(consumer, matrix, x4, y4, z4, red, green, blue, alpha);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        double transformedX = matrix.m00() * x
                + matrix.m10() * y
                + matrix.m20() * z
                + matrix.m30();
        double transformedY = matrix.m01() * x
                + matrix.m11() * y
                + matrix.m21() * z
                + matrix.m31();
        double transformedZ = matrix.m02() * x
                + matrix.m12() * y
                + matrix.m22() * z
                + matrix.m32();
        consumer.vertex(transformedX, transformedY, transformedZ)
                .color(red, green, blue, alpha)
                .endVertex();
    }
}
