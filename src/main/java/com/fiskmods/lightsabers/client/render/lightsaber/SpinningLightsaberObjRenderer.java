package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.render.hilt.HiltModelRenderer;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;

import java.util.IdentityHashMap;
import java.util.Map;

public final class SpinningLightsaberObjRenderer {
    public static final float MODEL_UNITS_PER_BLOCK = 8.0F;
    public static final int BLADE_LENGTH = 80;

    private static final ResourceLocation GRIP_MODEL = model("grip");
    private static final ResourceLocation OUTER_MODEL = model("outer");
    private static final ResourceLocation UPPER_EMITTER_MODEL = model("upper_emitter");
    private static final ResourceLocation LOWER_EMITTER_MODEL = model("lower_emitter");
    private static final ResourceLocation SWITCH_MODEL = model("switch");

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
    private static final float MAX_ROTATION_SPEED = 72.0F;
    private static final float ROTATION_ACCELERATION = 8.0F;
    private static final float MAX_ROTATION_DECELERATION = 96.0F;
    private static final float EXTRA_DECELERATION_TURN_SPEED = 48.0F;
    private static final RenderType MODEL_RENDER_TYPE = RenderType.entityCutoutNoCull(
            TextureAtlas.LOCATION_BLOCKS
    );

    private static final Map<ItemStack, AnimationState> ANIMATION_STATES = new IdentityHashMap<>();
    private static ClientLevel animationLevel;

    private SpinningLightsaberObjRenderer() {
    }

    public static float getBladeModelLength() {
        return LightsaberBladeRenderer.getBladeLength(BLADE_LENGTH) * DISPLAY_SCALE;
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(GRIP_MODEL);
        event.register(OUTER_MODEL);
        event.register(UPPER_EMITTER_MODEL);
        event.register(LOWER_EMITTER_MODEL);
        event.register(SWITCH_MODEL);
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
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        if (!isSupported(data)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        float rotation = getRotation(minecraft, stack);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        poseStack.pushPose();
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(itemRenderer, modelManager.getModel(GRIP_MODEL), poseStack, buffer, packedLight, packedOverlay);
        renderModel(itemRenderer, modelManager.getModel(SWITCH_MODEL), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(itemRenderer, modelManager.getModel(OUTER_MODEL), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.scale(OBJ_SCALE, OBJ_SCALE, OBJ_SCALE);
        renderModel(itemRenderer, modelManager.getModel(UPPER_EMITTER_MODEL), poseStack, buffer, packedLight, packedOverlay);
        if (hasLowerEmitter(data)) {
            renderModel(itemRenderer, modelManager.getModel(LOWER_EMITTER_MODEL), poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();

        if (!hasLowerEmitter(data)) {
            poseStack.pushPose();
            poseStack.translate(0.0F, NORMAL_POMMEL_OFFSET_Y, 0.0F);
            HiltModelRenderer.renderPart(
                    data.get(PartType.POMMEL),
                    PartType.POMMEL,
                    poseStack,
                    buffer,
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
            MultiBufferSource buffer,
            boolean inWorld
    ) {
        if (!isSupported(data)) {
            return;
        }

        float rotation = getRotation(Minecraft.getInstance(), stack);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(MODEL_ORIENTATION_ROTATION));
        poseStack.scale(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
        poseStack.translate(UPPER_BLADE_BASE_X, UPPER_BLADE_BASE_Y, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.scale(BLADE_WIDTH_SCALE, 1.0F, BLADE_WIDTH_SCALE);
        LightsaberBladeRenderer.render(
                data,
                stack,
                poseStack,
                buffer,
                inWorld,
                BLADE_LENGTH,
                false
        );
        poseStack.popPose();

        if (!hasLowerEmitter(data)) {
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
                buffer,
                inWorld,
                BLADE_LENGTH,
                false
        );
        poseStack.popPose();
        poseStack.popPose();
    }

    public static void renderPart(
            PartType type,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        ResourceLocation model = switch (type) {
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
        renderModel(itemRenderer, modelManager.getModel(model), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderModel(
            ItemRenderer itemRenderer,
            BakedModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        itemRenderer.renderModelLists(
                model,
                ItemStack.EMPTY,
                packedLight,
                packedOverlay,
                poseStack,
                buffer.getBuffer(MODEL_RENDER_TYPE)
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

        double animationTime = minecraft.level.getGameTime() + minecraft.getFrameTime();
        state.update(animationTime, active);
        float rotation = state.rotationAngle;
        if (state.isFinished()) {
            ANIMATION_STATES.remove(stack);
        }
        return rotation;
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
                    : (float) Math.min(Math.max(animationTime - lastAnimationTime, 0.0D), 0.25D);
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
            if (decelerationRemaining <= 0.0F || rotationSpeed <= 0.0F || delta <= 0.0F) {
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

    private static ResourceLocation model(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                Lightsabers.MODID,
                "item/spinning/" + name
        );
    }
}
