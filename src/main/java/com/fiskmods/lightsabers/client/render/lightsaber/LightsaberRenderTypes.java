package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.integration.iris.IrisCompat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class LightsaberRenderTypes {
    private static final RenderPipeline CORE_PIPELINE = createPipeline(
            "core", ColorTargetState.DEFAULT, DepthStencilState.DEFAULT
    );
    private static final RenderPipeline GLOW_PIPELINE = createPipeline(
            "glow",
            new ColorTargetState(BlendFunction.LIGHTNING),
            new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)
    );
    private static final RenderPipeline DARK_GLOW_PIPELINE = createPipeline(
            "dark_glow",
            new ColorTargetState(BlendFunction.TRANSLUCENT),
            new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)
    );
    private static final OitPipelineSet GLOW_OIT = OitPipelineSet.builder(
            Identifier.fromNamespaceAndPath(Lightsabers.MODID, "glow"),
            createPipelineBuilder().withShaderDefine("OIT_ADDITIVE")
    ).build();
    private static final OitPipelineSet DARK_GLOW_OIT = OitPipelineSet.builder(
            Identifier.fromNamespaceAndPath(Lightsabers.MODID, "dark_glow"),
            createPipelineBuilder()
    ).build();

    public static final RenderType BLADE_CORE = createRenderType("lightsabers_blade_core", CORE_PIPELINE, null);
    public static final RenderType BLADE_GLOW = createRenderType("lightsabers_blade_glow", GLOW_PIPELINE, GLOW_OIT);
    public static final RenderType BLADE_GLOW_DIRECT = BLADE_GLOW;
    public static final RenderType BLADE_DARK_GLOW = createRenderType("lightsabers_blade_dark_glow", DARK_GLOW_PIPELINE, DARK_GLOW_OIT);
    public static final RenderType BLADE_DARK_GLOW_DIRECT = BLADE_DARK_GLOW;
    public static final RenderType LIGHTNING_GLOW = createRenderType("lightsabers_lightning_glow", GLOW_PIPELINE, GLOW_OIT);
    public static final RenderType LIGHTNING_CORE = createRenderType("lightsabers_lightning_core", CORE_PIPELINE, null);
    public static final RenderType FORCE_EFFECT_GLOW = createRenderType("lightsabers_force_effect_glow", GLOW_PIPELINE, GLOW_OIT);
    public static final RenderType FORCE_EFFECT_CORE = createRenderType("lightsabers_force_effect_core", CORE_PIPELINE, null);

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(CORE_PIPELINE);
        event.registerPipeline(GLOW_PIPELINE);
        event.registerPipeline(DARK_GLOW_PIPELINE);
        event.registerOitPipelineSet(GLOW_OIT);
        event.registerOitPipelineSet(DARK_GLOW_OIT);
        IrisCompat.registerBasicPipelines(
                CORE_PIPELINE,
                GLOW_PIPELINE,
                DARK_GLOW_PIPELINE,
                GLOW_OIT.depthBoundsPipeline(),
                GLOW_OIT.transmittancePipeline(),
                GLOW_OIT.accumulatePipeline(),
                DARK_GLOW_OIT.depthBoundsPipeline(),
                DARK_GLOW_OIT.transmittancePipeline(),
                DARK_GLOW_OIT.accumulatePipeline()
        );
    }

    private static RenderPipeline.Builder createPipelineBuilder() {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withCull(false)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
    }

    private static RenderPipeline createPipeline(
            String name,
            ColorTargetState colorTargetState,
            DepthStencilState depthStencilState
    ) {
        return createPipelineBuilder()
                .withLocation(Identifier.fromNamespaceAndPath(Lightsabers.MODID, "pipeline/" + name))
                .withColorTargetState(colorTargetState)
                .withDepthStencilState(depthStencilState)
                .build();
    }

    private static RenderType createRenderType(String name, RenderPipeline pipeline, OitPipelineSet oitPipelines) {
        RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline);
        if (oitPipelines != null) {
            setup.setOitPipelines(oitPipelines).sortOnUpload();
        }
        return RenderType.create(name, setup.createRenderSetup());
    }
}
