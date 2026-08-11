package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class LightsaberRenderTypes {
    private static final RenderPipeline CORE_PIPELINE = createPipeline(
            "core",
            ColorTargetState.DEFAULT,
            DepthStencilState.DEFAULT
    );
    private static final RenderPipeline GLOW_PIPELINE = createPipeline(
            "glow",
            new ColorTargetState(BlendFunction.LIGHTNING),
            new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)
    );
    private static final RenderPipeline DARK_GLOW_PIPELINE = createPipeline(
            "dark_glow",
            new ColorTargetState(BlendFunction.TRANSLUCENT),
            new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false)
    );
    private static final OutputTarget PARTICLES_TARGET = new OutputTarget(
            "lightsabers_particles_target",
            () -> Minecraft.getInstance().levelRenderer.getParticlesTarget()
    );

    public static final RenderType BLADE_CORE = createRenderType(
            "lightsabers_blade_core",
            CORE_PIPELINE,
            OutputTarget.MAIN_TARGET,
            2048,
            false
    );
    public static final RenderType BLADE_GLOW = createRenderType(
            "lightsabers_blade_glow",
            GLOW_PIPELINE,
            OutputTarget.ITEM_ENTITY_TARGET,
            8192,
            true
    );
    public static final RenderType BLADE_GLOW_DIRECT = createRenderType(
            "lightsabers_blade_glow_direct",
            GLOW_PIPELINE,
            OutputTarget.MAIN_TARGET,
            8192,
            true
    );
    public static final RenderType BLADE_DARK_GLOW = createRenderType(
            "lightsabers_blade_dark_glow",
            DARK_GLOW_PIPELINE,
            OutputTarget.ITEM_ENTITY_TARGET,
            8192,
            true
    );
    public static final RenderType BLADE_DARK_GLOW_DIRECT = createRenderType(
            "lightsabers_blade_dark_glow_direct",
            DARK_GLOW_PIPELINE,
            OutputTarget.MAIN_TARGET,
            8192,
            true
    );
    public static final RenderType LIGHTNING_GLOW = createRenderType(
            "lightsabers_lightning_glow",
            GLOW_PIPELINE,
            OutputTarget.ITEM_ENTITY_TARGET,
            2048,
            true
    );
    public static final RenderType LIGHTNING_CORE = createRenderType(
            "lightsabers_lightning_core",
            CORE_PIPELINE,
            OutputTarget.MAIN_TARGET,
            1024,
            false
    );
    public static final RenderType FORCE_EFFECT_GLOW = createRenderType(
            "lightsabers_force_effect_glow",
            GLOW_PIPELINE,
            PARTICLES_TARGET,
            16384,
            true
    );
    public static final RenderType FORCE_EFFECT_CORE = createRenderType(
            "lightsabers_force_effect_core",
            CORE_PIPELINE,
            OutputTarget.MAIN_TARGET,
            4096,
            false
    );
    private LightsaberRenderTypes() {
    }

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(CORE_PIPELINE);
        event.registerPipeline(GLOW_PIPELINE);
        event.registerPipeline(DARK_GLOW_PIPELINE);
    }

    private static RenderPipeline createPipeline(
            String name,
            ColorTargetState colorTargetState,
            DepthStencilState depthStencilState
    ) {
        return RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(
                        Lightsabers.MODID,
                        "pipeline/" + name
                ))
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withColorTargetState(colorTargetState)
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .withDepthStencilState(depthStencilState)
                .build();
    }

    private static RenderType createRenderType(
            String name,
            RenderPipeline pipeline,
            OutputTarget outputTarget,
            int bufferSize,
            boolean sortOnUpload
    ) {
        RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline)
                .setOutputTarget(outputTarget)
                .bufferSize(bufferSize);
        if (sortOnUpload) {
            setup.sortOnUpload();
        }
        return RenderType.create(name, setup.createRenderSetup());
    }
}
