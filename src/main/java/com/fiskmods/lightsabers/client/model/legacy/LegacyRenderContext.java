package com.fiskmods.lightsabers.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;

final class LegacyRenderContext {
    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    private LegacyRenderContext() {
    }

    static State begin(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        State state = STATE.get();
        if (state.active) {
            throw new IllegalStateException("Legacy model render context is already active");
        }
        state.active = true;
        state.poseStack = poseStack;
        state.consumer = consumer;
        state.packedLight = packedLight;
        state.packedOverlay = packedOverlay;
        state.red = red;
        state.green = green;
        state.blue = blue;
        state.alpha = alpha;
        return state;
    }

    static State get() {
        State state = STATE.get();
        if (!state.active) {
            throw new IllegalStateException("Legacy model render context is not active");
        }
        return state;
    }

    static void end() {
        State state = STATE.get();
        state.active = false;
        state.poseStack = null;
        state.consumer = null;
    }

    static final class State {
        private final Quaternionf temporaryRotation = new Quaternionf();

        private boolean active;
        private PoseStack poseStack;
        private VertexConsumer consumer;
        private int packedLight;
        private int packedOverlay;
        private float red;
        private float green;
        private float blue;
        private float alpha;

        PoseStack poseStack() {
            return poseStack;
        }

        VertexConsumer consumer() {
            return consumer;
        }

        int packedLight() {
            return packedLight;
        }

        int packedOverlay() {
            return packedOverlay;
        }

        float red() {
            return red;
        }

        float green() {
            return green;
        }

        float blue() {
            return blue;
        }

        float alpha() {
            return alpha;
        }

        Quaternionf temporaryRotation() {
            return temporaryRotation;
        }
    }
}
