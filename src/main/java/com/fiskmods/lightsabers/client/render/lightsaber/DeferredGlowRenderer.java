package com.fiskmods.lightsabers.client.render.lightsaber;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public enum DeferredGlowRenderer {
    INSTANCE;

    private static final int FALLBACK_BUFFER_SIZE = 256;

    private final MultiBufferSource.BufferSource deferredBuffer = MultiBufferSource.immediateWithBuffers(
            createFixedBuffers(),
            new ByteBufferBuilder(FALLBACK_BUFFER_SIZE)
    );

    private boolean collecting;

    private static SequencedMap<RenderType, ByteBufferBuilder> createFixedBuffers() {
        SequencedMap<RenderType, ByteBufferBuilder> buffers = new LinkedHashMap<>();
        buffers.put(
                LightsaberRenderTypes.BLADE_GLOW,
                new ByteBufferBuilder(LightsaberRenderTypes.BLADE_GLOW.bufferSize())
        );
        buffers.put(
                LightsaberRenderTypes.BLADE_DARK_GLOW,
                new ByteBufferBuilder(LightsaberRenderTypes.BLADE_DARK_GLOW.bufferSize())
        );
        return buffers;
    }

    // Glow layers write no depth, so clouds, water and weather rendered later in the
    // frame would overdraw them. During the level pass the glow is collected here and
    // flushed at AFTER_LEVEL, where the depth buffer is complete and Iris/Oculus has
    // already composited the shader pack output. Fabulous graphics already sorts the
    // glow correctly per pixel via ITEM_ENTITY_TARGET, so it keeps immediate rendering.
    public static VertexConsumer getBuffer(MultiBufferSource fallback, RenderType type) {
        if (INSTANCE.collecting
                && !Minecraft.useShaderTransparency()
                && fallback == Minecraft.getInstance().renderBuffers().bufferSource()) {
            return INSTANCE.deferredBuffer.getBuffer(type);
        }
        return fallback.getBuffer(type);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            deferredBuffer.endBatch();
            collecting = true;
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            collecting = false;
            RenderSystem.getModelViewStack().pushMatrix().mul(event.getModelViewMatrix());
            RenderSystem.applyModelViewMatrix();
            try {
                deferredBuffer.endBatch();
            } finally {
                RenderSystem.getModelViewStack().popMatrix();
                RenderSystem.applyModelViewMatrix();
            }
        }
    }
}
