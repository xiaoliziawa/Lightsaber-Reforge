package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.client.render.EnergyBeamRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberRenderTypes;
import com.fiskmods.lightsabers.common.data.ALDataInterp;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fiskfille.utils.helper.VectorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

public enum ClientForceEffectRenderer {
    INSTANCE;

    private static final Vec3 STASIS_COLOR = new Vec3(
            54.0D / 255.0D,
            84.0D / 255.0D,
            181.0D / 255.0D
    );
    private static final Vec3 WOUND_COLOR = new Vec3(0.85D, 0.08D, 0.08D);
    private static final Vec3 CHOKE_COLOR = new Vec3(0.6D, 0.05D, 0.75D);
    private static final Vec3 TORMENT_COLOR = new Vec3(0.95D, 0.05D, 0.35D);
    private static final int MERIDIAN_COUNT = 12;
    private static final int LATITUDE_COUNT = 7;
    private static final int SEGMENTS_PER_CURVE = 32;
    private static final int CHOKE_RING_SEGMENTS = 32;
    private static final float STASIS_FADE_TICKS = 20.0F;
    private static final float CHOKE_FADE_TICKS = 3.0F;
    private static final float STASIS_ALPHA = 0.5F;
    private static final float CHOKE_ALPHA = 0.65F;
    private static final double FIELD_RADIUS_MULTIPLIER = 0.75D;
    private static final double STASIS_BEAM_HALF_WIDTH = 0.018D;
    private static final double CHOKE_BEAM_HALF_WIDTH = 0.022D;
    private static final double FIELD_ROTATION_SPEED = 0.035D;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        for (Player player : level.players()) {
            if (ALDataInterp.DRAIN_LIFE_TIMER.get(player) <= 0.0F
                    && !StatusEffect.getTargets(player, Effect.DRAIN).isEmpty()) {
                ALDataInterp.DRAIN_LIFE_TIMER.setWithoutNotify(player, 1.0F);
            }
        }
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        float partialTick = event.getPartialTick();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        renderCasterEffects(level, partialTick, poseStack, buffer, camera, minecraft.player);
        renderTargetEffects(level, partialTick, poseStack, buffer, camera);

        buffer.endBatch(LightsaberRenderTypes.FORCE_EFFECT_GLOW);
        buffer.endBatch(LightsaberRenderTypes.FORCE_EFFECT_CORE);
        poseStack.popPose();
    }

    private static void renderCasterEffects(
            ClientLevel level,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 camera,
            Player localPlayer
    ) {
        for (Player caster : level.players()) {
            boolean lightning = StatusEffect.has(caster, Effect.LIGHTNING);
            boolean draining = ALDataInterp.DRAIN_LIFE_TIMER.get(caster) > 0.0F;
            if (!lightning && !draining) {
                continue;
            }
            boolean firstPerson = caster == localPlayer
                    && Minecraft.getInstance().options.getCameraType().isFirstPerson();
            RenderForceLightning.renderForCaster(
                    caster,
                    partialTick,
                    poseStack,
                    buffer,
                    Vec3.ZERO,
                    camera,
                    firstPerson
            );
        }
    }

    private static void renderTargetEffects(
            ClientLevel level,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 camera
    ) {
        VertexConsumer glow = buffer.getBuffer(LightsaberRenderTypes.FORCE_EFFECT_GLOW);
        Matrix4f matrix = poseStack.last().pose();
        for (Entity loadedEntity : level.entitiesForRendering()) {
            if (!(loadedEntity instanceof LivingEntity entity)) {
                continue;
            }
            StatusEffect stasis = StatusEffect.get(entity, Effect.STUN);
            if (stasis != null) {
                renderStasis(glow, matrix, entity, stasis, partialTick, camera);
            }
            StatusEffect choke = StatusEffect.get(entity, Effect.CHOKE);
            if (choke != null) {
                renderChoke(glow, matrix, entity, choke, partialTick, camera);
            }
        }
    }

    private static void renderStasis(
            VertexConsumer consumer,
            Matrix4f matrix,
            LivingEntity entity,
            StatusEffect stasis,
            float partialTick,
            Vec3 camera
    ) {
        float remainingTicks = stasis.duration - partialTick;
        float alpha = STASIS_ALPHA
                * Mth.clamp(remainingTicks / STASIS_FADE_TICKS, 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }

        Vec3 position = VectorHelper.getPosition(entity, partialTick);
        double radius = Math.max(entity.getBbWidth(), entity.getBbHeight())
                * FIELD_RADIUS_MULTIPLIER;
        double centerY = position.y + entity.getBbHeight() * 0.5D;
        double animationTime = entity.tickCount + partialTick;
        renderMeridians(
                consumer,
                matrix,
                camera,
                position.x,
                centerY,
                position.z,
                radius,
                animationTime,
                alpha
        );
        renderLatitudes(
                consumer,
                matrix,
                camera,
                position.x,
                centerY,
                position.z,
                radius,
                animationTime,
                alpha
        );
    }

    private static void renderChoke(
            VertexConsumer consumer,
            Matrix4f matrix,
            LivingEntity entity,
            StatusEffect choke,
            float partialTick,
            Vec3 camera
    ) {
        float remainingTicks = choke.duration - partialTick;
        float fadeIn = Math.min(60.0F - remainingTicks, CHOKE_FADE_TICKS)
                / CHOKE_FADE_TICKS;
        float fadeOut = Math.min(remainingTicks, CHOKE_FADE_TICKS) / CHOKE_FADE_TICKS;
        float alpha = CHOKE_ALPHA * Mth.clamp(Math.min(fadeIn, fadeOut), 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }

        Vec3 position = VectorHelper.getPosition(entity, partialTick);
        int amplifier = Mth.clamp(choke.amplifier, 0, 2);
        int ringCount = 2 + amplifier * 2;
        Vec3 color = amplifier == 0
                ? WOUND_COLOR
                : amplifier == 1 ? CHOKE_COLOR : TORMENT_COLOR;
        double animationTime = entity.tickCount + partialTick;
        double baseRadius = Math.max(entity.getBbWidth() * 0.75D, 0.35D);
        for (int ring = 0; ring < ringCount; ring++) {
            double centerY = position.y
                    + entity.getBbHeight() * (0.38D + ring * 0.1D / ringCount);
            double radius = baseRadius
                    * (1.0D + Math.sin(animationTime * 0.25D + ring) * 0.12D);
            double phase = animationTime * (0.08D + amplifier * 0.025D)
                    + Math.PI * 2.0D * ring / ringCount;
            Vec3 previous = pointOnRing(position.x, centerY, position.z, radius, phase);
            for (int segment = 1; segment <= CHOKE_RING_SEGMENTS; segment++) {
                double angle = phase + Math.PI * 2.0D * segment / CHOKE_RING_SEGMENTS;
                double verticalWave = Math.sin(angle * 3.0D + animationTime * 0.2D)
                        * entity.getBbHeight() * 0.025D;
                Vec3 current = pointOnRing(
                        position.x,
                        centerY + verticalWave,
                        position.z,
                        radius,
                        angle
                );
                renderSegment(
                        consumer,
                        matrix,
                        previous,
                        current,
                        camera,
                        CHOKE_BEAM_HALF_WIDTH,
                        color,
                        alpha
                );
                previous = current;
            }
        }
    }

    private static void renderMeridians(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 camera,
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            double animationTime,
            float alpha
    ) {
        double rotation = animationTime * FIELD_ROTATION_SPEED;
        for (int meridian = 0; meridian < MERIDIAN_COUNT; meridian++) {
            double longitude = Math.PI * 2.0D * meridian / MERIDIAN_COUNT + rotation;
            Vec3 previous = pointOnSphere(
                    centerX,
                    centerY,
                    centerZ,
                    radius,
                    -Math.PI * 0.5D,
                    longitude
            );
            for (int segment = 1; segment <= SEGMENTS_PER_CURVE; segment++) {
                double latitude = -Math.PI * 0.5D
                        + Math.PI * segment / SEGMENTS_PER_CURVE;
                Vec3 current = pointOnSphere(
                        centerX,
                        centerY,
                        centerZ,
                        radius,
                        latitude,
                        longitude
                );
                renderSegment(
                        consumer,
                        matrix,
                        previous,
                        current,
                        camera,
                        STASIS_BEAM_HALF_WIDTH,
                        STASIS_COLOR,
                        alpha
                );
                previous = current;
            }
        }
    }

    private static void renderLatitudes(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 camera,
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            double animationTime,
            float alpha
    ) {
        double rotation = -animationTime * FIELD_ROTATION_SPEED * 0.7D;
        for (int ring = 1; ring <= LATITUDE_COUNT; ring++) {
            double latitude = -Math.PI * 0.5D
                    + Math.PI * ring / (LATITUDE_COUNT + 1.0D);
            Vec3 previous = pointOnSphere(
                    centerX,
                    centerY,
                    centerZ,
                    radius,
                    latitude,
                    rotation
            );
            for (int segment = 1; segment <= SEGMENTS_PER_CURVE; segment++) {
                double longitude = rotation
                        + Math.PI * 2.0D * segment / SEGMENTS_PER_CURVE;
                Vec3 current = pointOnSphere(
                        centerX,
                        centerY,
                        centerZ,
                        radius,
                        latitude,
                        longitude
                );
                renderSegment(
                        consumer,
                        matrix,
                        previous,
                        current,
                        camera,
                        STASIS_BEAM_HALF_WIDTH,
                        STASIS_COLOR,
                        alpha
                );
                previous = current;
            }
        }
    }

    private static Vec3 pointOnSphere(
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            double latitude,
            double longitude
    ) {
        double horizontalRadius = Math.cos(latitude) * radius;
        return new Vec3(
                centerX + Math.cos(longitude) * horizontalRadius,
                centerY + Math.sin(latitude) * radius,
                centerZ + Math.sin(longitude) * horizontalRadius
        );
    }

    private static Vec3 pointOnRing(
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            double angle
    ) {
        return new Vec3(
                centerX + Math.cos(angle) * radius,
                centerY,
                centerZ + Math.sin(angle) * radius
        );
    }

    private static void renderSegment(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            Vec3 camera,
            double halfWidth,
            Vec3 color,
            float alpha
    ) {
        EnergyBeamRenderer.renderCameraFacingQuad(
                consumer,
                matrix,
                start,
                end,
                camera,
                halfWidth,
                color,
                alpha
        );
    }
}
