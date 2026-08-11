package com.fiskmods.lightsabers.client.render.entity;

import com.fiskmods.lightsabers.client.render.EnergyBeamRenderer;
import com.fiskmods.lightsabers.client.render.RenderSubmissionHelper;
import com.fiskmods.lightsabers.client.render.lightsaber.DeferredGlowRenderer;
import com.fiskmods.lightsabers.client.render.lightsaber.LightsaberRenderTypes;
import com.fiskmods.lightsabers.common.data.effect.Effect;
import com.fiskmods.lightsabers.common.data.effect.StatusEffect;
import com.fiskmods.lightsabers.common.entity.EntityForceLightning;
import com.fiskmods.lightsabers.common.force.effect.ForceTargeting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fiskfille.utils.helper.VectorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class RenderForceLightning extends EntityRenderer<
        EntityForceLightning,
        RenderForceLightning.RenderState
> {
    private static final double TARGET_RANGE = 7.0D;
    private static final Vec3 LIGHTNING_COLOR = new Vec3(0.0D, 0.0D, 1.0D);
    private static final Vec3 DRAIN_COLOR = new Vec3(1.0D, 0.4D, 0.0D);
    private static final Vec3 CORE_COLOR = new Vec3(1.0D, 1.0D, 1.0D);
    private static final double THIRD_PERSON_GLOW_WIDTH = 0.035D;
    private static final double FIRST_PERSON_GLOW_WIDTH = 0.07D;
    private static final double THIRD_PERSON_CORE_WIDTH = 0.008D;
    private static final double FIRST_PERSON_CORE_WIDTH = 0.016D;

    public RenderForceLightning(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {
        if (state.caster == null || !state.caster.isAlive()) {
            return;
        }
        renderForCaster(
                state.caster,
                state.partialTick,
                poseStack,
                collector,
                state.anchor,
                state.camera,
                state.firstPerson
        );
        super.submit(state, poseStack, collector, cameraState);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            EntityForceLightning lightning,
            RenderState state,
            float partialTicks
    ) {
        super.extractRenderState(lightning, state, partialTicks);
        LivingEntity caster = lightning.getCaster();
        state.caster = caster;
        state.anchor = VectorHelper.getPosition(lightning, partialTicks);
        state.camera = Minecraft.getInstance()
                .gameRenderer
                .getMainCamera()
                .position()
                .subtract(state.anchor);
        state.firstPerson = caster != null
                && caster == Minecraft.getInstance().player
                && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static void renderForCaster(
            LivingEntity caster,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Vec3 anchor,
            Vec3 camera,
            boolean firstPerson
    ) {
        long tick = caster.tickCount;

        int drainIndex = 0;
        for (LivingEntity target : StatusEffect.getTargets(caster, Effect.DRAIN)) {
            Vec3 targetPosition = VectorHelper.getPosition(target, partialTick)
                    .add(0.0D, target.getEyeHeight() * 0.5D, 0.0D);
            for (int bolt = 0; bolt < 2; bolt++) {
                renderBolt(
                        caster,
                        targetPosition,
                        DRAIN_COLOR,
                        tick * 100000L + drainIndex * 31L + bolt,
                        1.0F,
                        true,
                        firstPerson,
                        partialTick,
                        anchor,
                        camera,
                        poseStack,
                        collector
                );
            }
            drainIndex++;
        }

        StatusEffect effect = StatusEffect.get(caster, Effect.LIGHTNING);
        if (effect != null) {
            Vec3 targetPosition = getLightningTarget(caster, partialTick);
            for (int hand = 0; hand < 2; hand++) {
                for (int bolt = 0; bolt < 2 + effect.amplifier; bolt++) {
                    renderBolt(
                            caster,
                            targetPosition,
                            LIGHTNING_COLOR,
                            tick * 100000L + hand * 4099L + bolt,
                            1.5F + effect.amplifier * 0.5F,
                            hand == 0,
                            firstPerson,
                            partialTick,
                            anchor,
                            camera,
                            poseStack,
                            collector
                    );
                }
            }
        }
    }

    private static Vec3 getLightningTarget(LivingEntity caster, float partialTick) {
        if (caster instanceof Player player) {
            LivingEntity target = ForceTargeting.findLookTarget(player, TARGET_RANGE);
            if (target != null) {
                return VectorHelper.getPosition(target, partialTick)
                        .add(0.0D, target.getEyeHeight() * 0.5D, 0.0D);
            }
        }

        Vec3 start = VectorHelper.getOffsetCoords(caster, 0.0D, 0.0D, 0.0D, partialTick);
        Vec3 end = VectorHelper.getOffsetCoords(
                caster,
                0.0D,
                0.0D,
                TARGET_RANGE,
                partialTick
        );
        HitResult blockHit = caster.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));
        return blockHit.getType() == HitResult.Type.MISS
                ? end
                : blockHit.getLocation();
    }

    private static void renderBolt(
            LivingEntity caster,
            Vec3 target,
            Vec3 color,
            long seed,
            float spreadFactor,
            boolean rightHand,
            boolean firstPerson,
            float partialTick,
            Vec3 anchor,
            Vec3 camera,
            PoseStack poseStack,
            SubmitNodeCollector collector
    ) {
        Vec3 currentSource = getHandPosition(
                caster,
                rightHand,
                firstPerson,
                partialTick
        );
        Vec3 previousSource = getHandPosition(caster, rightHand, firstPerson, 0.0F);
        BoltPoints current = createBoltPoints(
                currentSource,
                target,
                seed,
                spreadFactor
        );
        BoltPoints previous = createBoltPoints(
                previousSource,
                target,
                seed - 100000L,
                spreadFactor
        );
        BoltPoints points = previous.lerp(current, partialTick).relativeTo(anchor);

        double glowWidth = firstPerson
                ? FIRST_PERSON_GLOW_WIDTH
                : THIRD_PERSON_GLOW_WIDTH;
        double coreWidth = firstPerson
                ? FIRST_PERSON_CORE_WIDTH
                : THIRD_PERSON_CORE_WIDTH;
        DeferredGlowRenderer.submitGeometry(
                collector,
                poseStack,
                LightsaberRenderTypes.FORCE_EFFECT_GLOW,
                LightsaberRenderTypes.FORCE_EFFECT_GLOW,
                true,
                (renderPose, glow) -> renderBoltSegments(
                        glow,
                        renderPose,
                        points,
                        camera,
                        glowWidth,
                        color,
                        0.85F
                )
        );
        RenderSubmissionHelper.submitGeometry(
                collector,
                poseStack,
                LightsaberRenderTypes.FORCE_EFFECT_CORE,
                (renderPose, core) -> renderBoltSegments(
                        core,
                        renderPose,
                        points,
                        camera,
                        coreWidth,
                        CORE_COLOR,
                        1.0F
                )
        );
    }

    private static void renderBoltSegments(
            VertexConsumer consumer,
            PoseStack poseStack,
            BoltPoints points,
            Vec3 camera,
            double halfWidth,
            Vec3 color,
            float alpha
    ) {
        renderSegment(
                consumer,
                poseStack,
                points.source,
                points.first,
                camera,
                halfWidth,
                color,
                alpha
        );
        renderSegment(
                consumer,
                poseStack,
                points.first,
                points.second,
                camera,
                halfWidth,
                color,
                alpha
        );
        renderSegment(
                consumer,
                poseStack,
                points.second,
                points.target,
                camera,
                halfWidth,
                color,
                alpha
        );
    }

    private static Vec3 getHandPosition(
            LivingEntity caster,
            boolean rightHand,
            boolean firstPerson,
            float partialTick
    ) {
        double side = (firstPerson ? 0.45D : 0.275D)
                * (rightHand ? -1.0D : 1.0D);
        double forward = firstPerson ? 0.6D : 0.8D;
        return VectorHelper.getOffsetCoords(
                caster,
                side,
                -0.25D,
                forward,
                partialTick
        );
    }

    private static BoltPoints createBoltPoints(
            Vec3 source,
            Vec3 target,
            long seed,
            float spreadFactor
    ) {
        RandomSource random = RandomSource.create(seed);
        double distance = source.distanceTo(target);
        double amount = Math.min(distance * 0.05D, 1.0D);
        double targetSpread = Math.min(0.2D, amount) * spreadFactor;
        Vec3 direction = target.subtract(source);
        Vec3 first = source.add(direction.scale(0.33D))
                .add(randomOffset(random, amount));
        Vec3 second = source.add(direction.scale(0.66D))
                .add(randomOffset(random, amount));
        Vec3 jitteredTarget = target.add(
                randomOffset(random, targetSpread * 0.125D)
        );
        return new BoltPoints(source, first, second, jitteredTarget);
    }

    private static Vec3 randomOffset(RandomSource random, double scale) {
        return new Vec3(
                Mth.nextDouble(random, -scale, scale),
                Mth.nextDouble(random, -scale, scale),
                Mth.nextDouble(random, -scale, scale)
        );
    }

    private static void renderSegment(
            VertexConsumer consumer,
            PoseStack poseStack,
            Vec3 start,
            Vec3 end,
            Vec3 camera,
            double halfWidth,
            Vec3 color,
            float alpha
    ) {
        EnergyBeamRenderer.renderCameraFacingQuad(
                consumer,
                poseStack.last().pose(),
                start,
                end,
                camera,
                halfWidth,
                color,
                alpha
        );
    }

    public static final class RenderState extends EntityRenderState {
        private LivingEntity caster;
        private Vec3 anchor = Vec3.ZERO;
        private Vec3 camera = Vec3.ZERO;
        private boolean firstPerson;
    }

    private record BoltPoints(Vec3 source, Vec3 first, Vec3 second, Vec3 target) {
        private BoltPoints lerp(BoltPoints other, double partialTick) {
            return new BoltPoints(
                    source.lerp(other.source, partialTick),
                    first.lerp(other.first, partialTick),
                    second.lerp(other.second, partialTick),
                    target.lerp(other.target, partialTick)
            );
        }

        private BoltPoints relativeTo(Vec3 origin) {
            return new BoltPoints(
                    source.subtract(origin),
                    first.subtract(origin),
                    second.subtract(origin),
                    target.subtract(origin)
            );
        }
    }
}
