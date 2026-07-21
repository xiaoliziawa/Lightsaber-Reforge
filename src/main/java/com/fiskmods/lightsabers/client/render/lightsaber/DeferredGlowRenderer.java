package com.fiskmods.lightsabers.client.render.lightsaber;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public enum DeferredGlowRenderer {
    INSTANCE;

    private static final int FALLBACK_BUFFER_SIZE = 256;

    private final MultiBufferSource.BufferSource deferredBuffer = MultiBufferSource.immediateWithBuffers(
            Map.of(
                    LightsaberRenderTypes.BLADE_GLOW,
                    new BufferBuilder(LightsaberRenderTypes.BLADE_GLOW.bufferSize()),
                    LightsaberRenderTypes.BLADE_DARK_GLOW,
                    new BufferBuilder(LightsaberRenderTypes.BLADE_DARK_GLOW.bufferSize())
            ),
            new BufferBuilder(FALLBACK_BUFFER_SIZE)
    );

    private boolean collecting;

    // Glow layers write no depth, so clouds, water and weather rendered later in the
    // frame would overdraw them. During the level pass the glow is collected here and
    // flushed at AFTER_LEVEL, where the depth buffer is complete, the model view
    // matrix matches the entity pass (AFTER_WEATHER fires inside the clouds/weather
    // model view push and would double-apply the camera rotation), and Iris/Oculus has
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
            deferredBuffer.endBatch();
        }
    }
}
