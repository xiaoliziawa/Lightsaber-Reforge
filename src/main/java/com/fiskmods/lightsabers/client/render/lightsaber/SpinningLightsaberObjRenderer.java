package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.render.RenderSubmissionHelper;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public final class SpinningLightsaberObjRenderer {
    public static final float MODEL_UNITS_PER_BLOCK = 8.0F;
    public static final int BLADE_LENGTH = 80;

    private static final StandaloneModelKey<QuadCollection> GRIP_MODEL =
            modelKey("grip");
    private static final StandaloneModelKey<QuadCollection> OUTER_MODEL =
            modelKey("outer");
    private static final StandaloneModelKey<QuadCollection> UPPER_EMITTER_MODEL =
            modelKey("upper_emitter");
    private static final StandaloneModelKey<QuadCollection> LOWER_EMITTER_MODEL =
            modelKey("lower_emitter");
    private static final StandaloneModelKey<QuadCollection> SWITCH_MODEL =
            modelKey("switch");

    private static final float OBJ_SCALE = 1.0F / MODEL_UNITS_PER_BLOCK;
    private static final float DISPLAY_SCALE = 1.35F;
    private static final float BLADE_WIDTH_SCALE = 2.25F;
    private static final float MODEL_ORIENTATION_ROTATION = 180.0F;
    private static final float SWITCH_PART_OFFSET_X_CM = 0.355F;
    private static final float SWITCH_PART_OFFSET_Y_CM = 2.356F;
    private static final float UPPER_EMITTER_PART_OFFSET_X_CM = 0.300F;
    private static final float UPPER_EMITTER_PART_OFFSET_Y_CM = 8.230F;
    private static final float LOWER_EMITTER_PART_OFFSET_X_CM = 0.335F;
    private static final float LOWER_EMITTER_PART_OFFSET_Y_CM = -8.230F;
    private static final float UPPER_BLADE_BASE_X = -0.018F * OBJ_SCALE;
    private static final float UPPER_BLADE_BASE_Y = 10.178F * OBJ_SCALE;
    private static final float LOWER_BLADE_BASE_X = 0.018F * OBJ_SCALE;
    private static final float LOWER_BLADE_BASE_Y = -10.178F * OBJ_SCALE;
    private static final float NORMAL_POMMEL_OFFSET_Y = -0.96F;
    private static final float BLADE_EDGE_ROLL = 90.0F;
    private static final float SPIN_DIRECTION = 1.0F;
    private static final float MAX_ROTATION_SPEED = 72.0F;
    private static final float ROTATION_ACCELERATION = 8.0F;
    private static final float MAX_ROTATION_DECELERATION = 96.0F;
    private static final float EXTRA_DECELERATION_TURN_SPEED = 48.0F;

    private static final Map<ItemStack, AnimationState> ANIMATION_STATES =
            new IdentityHashMap<>();
    private static ClientLevel animationLevel;

    private SpinningLightsaberObjRenderer() {
    }

    public static float getBladeModelLength() {
        return LightsaberBladeRenderer.getBladeLength(BLADE_LENGTH) * DISPLAY_SCALE;
    }

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        register(event, GRIP_MODEL, "grip");
        register(event, OUTER_MODEL, "outer");
        register(event, UPPER_EMITTER_MODEL, "upper_emitter");
        register(event, LOWER_EMITTER_MODEL, "lower_emitter");
        register(event, SWITCH_MODEL, "switch");
    }

    public static boolean isSupported(LightsaberData data) {
        return data.hasSpinningCore();
    }

    public static boolean hasLowerEmitter(LightsaberData data) {
        return data.canSpinBlades();
    }

    public static void renderHilt(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            int packedOverlay
    ) {
        if (!isSupported(data)) {
            return;
        }

        float rotation = getRotation(Minecraft.getInstance(), stack);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        poseStack.pushPose();
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(GRIP_MODEL, stack, poseStack, collector, packedLight, packedOverlay);
        renderModel(SWITCH_MODEL, stack, poseStack, collector, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(OUTER_MODEL, stack, poseStack, collector, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(
                UPPER_EMITTER_MODEL,
                stack,
                poseStack,
                collector,
                packedLight,
                packedOverlay
        );
        if (hasLowerEmitter(data)) {
            renderModel(
                    LOWER_EMITTER_MODEL,
                    stack,
                    poseStack,
                    collector,
                    packedLight,
                    packedOverlay
            );
        }
        poseStack.popPose();

        if (!hasLowerEmitter(data)) {
            poseStack.pushPose();
            poseStack.translate(0.0F, NORMAL_POMMEL_OFFSET_Y, 0.0F);
            HiltModelRenderer.renderPart(
                    data.get(PartType.POMMEL),
                    PartType.POMMEL,
                    poseStack,
                    collector,
                    packedLight,
                    packedOverlay
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static void renderBlades(
            LightsaberData data,
            ItemStack stack,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            boolean inWorld,
            boolean deferGlow
    ) {
        if (!isSupported(data)) {
            return;
        }

        float rotation = getRotation(Minecraft.getInstance(), stack);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        LightsaberBladeRenderer.bladeRoll = BLADE_EDGE_ROLL;
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(UPPER_BLADE_BASE_X, UPPER_BLADE_BASE_Y, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(BLADE_WIDTH_SCALE, 1.0F, BLADE_WIDTH_SCALE);
        LightsaberBladeRenderer.render(
                data,
                stack,
                poseStack,
                collector,
                inWorld,
                deferGlow,
                BLADE_LENGTH,
                false
        );
        poseStack.popPose();

        if (!hasLowerEmitter(data)) {
            LightsaberBladeRenderer.bladeRoll = 0.0F;
            poseStack.popPose();
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(LOWER_BLADE_BASE_X, LOWER_BLADE_BASE_Y, 0.0F);
        poseStack.scale(BLADE_WIDTH_SCALE, 1.0F, BLADE_WIDTH_SCALE);
        LightsaberBladeRenderer.render(
                data,
                stack,
                poseStack,
                collector,
                inWorld,
                deferGlow,
                BLADE_LENGTH,
                false
        );
        poseStack.popPose();
        LightsaberBladeRenderer.bladeRoll = 0.0F;
        poseStack.popPose();
    }

    public static void renderPart(
            PartType type,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            int packedOverlay
    ) {
        StandaloneModelKey<QuadCollection> model = switch (type) {
            case EMITTER -> UPPER_EMITTER_MODEL;
            case SWITCH_SECTION -> SWITCH_MODEL;
            case BODY -> GRIP_MODEL;
            case POMMEL -> LOWER_EMITTER_MODEL;
        };
        float offsetX = switch (type) {
            case EMITTER -> UPPER_EMITTER_PART_OFFSET_X_CM;
            case SWITCH_SECTION -> SWITCH_PART_OFFSET_X_CM;
            case BODY -> 0.0F;
            case POMMEL -> LOWER_EMITTER_PART_OFFSET_X_CM;
        };
        float offsetY = switch (type) {
            case EMITTER -> UPPER_EMITTER_PART_OFFSET_Y_CM;
            case SWITCH_SECTION -> SWITCH_PART_OFFSET_Y_CM;
            case BODY -> 0.0F;
            case POMMEL -> LOWER_EMITTER_PART_OFFSET_Y_CM;
        };
        poseStack.pushPose();
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        poseStack.translate(-offsetX, -offsetY, 0.0F);
        renderModel(
                model,
                ItemStack.EMPTY,
                poseStack,
                collector,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private static void renderModel(
            StandaloneModelKey<QuadCollection> key,
            ItemStack stack,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            int packedOverlay
    ) {
        QuadCollection model = Minecraft.getInstance()
                .getModelManager()
                .getStandaloneModel(key);
        RenderSubmissionHelper.submitQuads(
                collector,
                poseStack,
                model,
                packedLight,
                packedOverlay,
                stack.hasFoil(),
                0
        );
    }

    private static float getRotation(Minecraft minecraft, ItemStack stack) {
        if (minecraft.level == null || stack.isEmpty()) {
            return 0.0F;
        }

        if (animationLevel != minecraft.level) {
            ANIMATION_STATES.clear();
            animationLevel = minecraft.level;
        }

        boolean active = isBeingUsed(minecraft, stack);
        AnimationState state = ANIMATION_STATES.get(stack);
        if (state == null) {
            if (!active) {
                return 0.0F;
            }
            state = new AnimationState();
            ANIMATION_STATES.put(stack, state);
        }

        double animationTime = minecraft.level.getGameTime()
                + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        state.update(animationTime, active);
        float rotation = state.rotationAngle;
        if (state.isFinished()) {
            ANIMATION_STATES.remove(stack);
        }
        return rotation * SPIN_DIRECTION;
    }

    private static boolean isBeingUsed(Minecraft minecraft, ItemStack stack) {
        if (!ItemLightsaberBase.isActive(stack)
                || !ItemLightsaberBase.isSpinningLightsaber(stack)) {
            return false;
        }
        for (Player player : minecraft.level.players()) {
            if (player.isUsingItem() && player.getUseItem() == stack) {
                return true;
            }
        }
        return false;
    }

    private static StandaloneModelKey<QuadCollection> modelKey(String name) {
        Identifier id = modelId(name);
        return new StandaloneModelKey<>(id::toString);
    }

    private static void register(
            ModelEvent.RegisterStandalone event,
            StandaloneModelKey<QuadCollection> key,
            String name
    ) {
        event.register(key, SimpleUnbakedStandaloneModel.quadCollection(modelId(name)));
    }

    private static Identifier modelId(String name) {
        return Identifier.fromNamespaceAndPath(
                Lightsabers.MODID,
                "item/spinning/" + name
        );
    }

    private static final class AnimationState {
        private float rotationAngle;
        private float rotationSpeed;
        private float decelerationRate;
        private float decelerationRemaining;
        private double lastAnimationTime = Double.NaN;
        private boolean animationActive;

        private void update(double animationTime, boolean active) {
            if (Double.compare(animationTime, lastAnimationTime) == 0) {
                return;
            }

            float delta = Double.isNaN(lastAnimationTime)
                    ? 0.0F
                    : (float) Math.min(
                            Math.max(animationTime - lastAnimationTime, 0.0D),
                            0.25D
                    );
            lastAnimationTime = animationTime;

            if (active) {
                decelerationRemaining = 0.0F;
                rotationSpeed = Math.min(
                        MAX_ROTATION_SPEED,
                        rotationSpeed + ROTATION_ACCELERATION * delta
                );
                rotationAngle = (rotationAngle + rotationSpeed * delta) % 360.0F;
                animationActive = true;
                return;
            }

            if (animationActive) {
                startDeceleration();
                animationActive = false;
            }
            updateDeceleration(delta);
        }

        private void startDeceleration() {
            if (rotationSpeed <= 0.0F) {
                rotationAngle = 0.0F;
                decelerationRemaining = 0.0F;
                return;
            }

            float distanceToAlignment = rotationAngle <= 0.0001F
                    ? 360.0F
                    : 360.0F - rotationAngle;
            float minimumDistance = rotationSpeed * rotationSpeed
                    / (2.0F * MAX_ROTATION_DECELERATION);
            while (distanceToAlignment < minimumDistance) {
                distanceToAlignment += 360.0F;
            }
            if (rotationSpeed >= EXTRA_DECELERATION_TURN_SPEED) {
                distanceToAlignment += 360.0F;
            }
            decelerationRemaining = distanceToAlignment;
            decelerationRate = rotationSpeed * rotationSpeed
                    / (2.0F * decelerationRemaining);
        }

        private void updateDeceleration(float delta) {
            if (decelerationRemaining <= 0.0F
                    || rotationSpeed <= 0.0F
                    || delta <= 0.0F) {
                return;
            }

            float stepDistance = rotationSpeed * delta
                    - 0.5F * decelerationRate * delta * delta;
            if (stepDistance >= decelerationRemaining
                    || rotationSpeed <= decelerationRate * delta) {
                rotationAngle = 0.0F;
                rotationSpeed = 0.0F;
                decelerationRemaining = 0.0F;
                return;
            }

            rotationAngle = (rotationAngle + stepDistance) % 360.0F;
            rotationSpeed = Math.max(
                    0.0F,
                    rotationSpeed - decelerationRate * delta
            );
            decelerationRemaining -= stepDistance;
        }

        private boolean isFinished() {
            return !animationActive
                    && rotationSpeed <= 0.0F
                    && decelerationRemaining <= 0.0F;
        }
    }
}
