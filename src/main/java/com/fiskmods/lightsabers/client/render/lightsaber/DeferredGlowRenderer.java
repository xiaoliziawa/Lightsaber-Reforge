package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.client.render.RenderSubmissionHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;

public enum DeferredGlowRenderer {
    INSTANCE;

    private static final int FALLBACK_BUFFER_SIZE = 256;

    private final MultiBufferSource.BufferSource deferredBuffer =
            MultiBufferSource.immediateWithBuffers(
                    createFixedBuffers(),
                    new ByteBufferBuilder(FALLBACK_BUFFER_SIZE)
            );
    private boolean hasDeferredGeometry;

    private static SequencedMap<RenderType, ByteBufferBuilder> createFixedBuffers() {
        SequencedMap<RenderType, ByteBufferBuilder> buffers = new LinkedHashMap<>();
        addBuffer(buffers, LightsaberRenderTypes.BLADE_GLOW);
        addBuffer(buffers, LightsaberRenderTypes.BLADE_DARK_GLOW);
        addBuffer(buffers, LightsaberRenderTypes.FORCE_EFFECT_GLOW);
        return buffers;
    }

    private static void addBuffer(
            SequencedMap<RenderType, ByteBufferBuilder> buffers,
            RenderType renderType
    ) {
        buffers.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
    }

    public static void submitGeometry(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            RenderType worldRenderType,
            RenderType directRenderType,
            boolean deferInWorld,
            RenderSubmissionHelper.GeometryRenderer renderer
    ) {
        if (!deferInWorld) {
            RenderSubmissionHelper.submitGeometry(
                    collector,
                    poseStack,
                    directRenderType,
                    renderer
            );
            return;
        }
        if (Minecraft.useShaderTransparency()) {
            RenderSubmissionHelper.submitGeometry(
                    collector,
                    poseStack,
                    worldRenderType,
                    renderer
            );
            return;
        }
        PoseStack renderPose = new PoseStack();
        renderPose.last().set(poseStack.last());
        renderer.render(
                renderPose,
                INSTANCE.deferredBuffer.getBuffer(worldRenderType)
        );
        INSTANCE.hasDeferredGeometry = true;
    }

    @SubscribeEvent
    public void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        if (!hasDeferredGeometry) {
            return;
        }

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix().mul(event.getModelViewMatrix());
        try {
            deferredBuffer.endBatch();
        } finally {
            hasDeferredGeometry = false;
            modelViewStack.popMatrix();
        }
    }
}
