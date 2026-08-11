package com.fiskmods.lightsabers.client.integration.iris;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.neoforged.fml.ModList;

public final class IrisCompat {
    private static final String MOD_ID = "iris";
    private static final Provider PROVIDER = createProvider();

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        return PROVIDER.isShaderPackInUse();
    }

    public static boolean isRenderingShadowPass() {
        return PROVIDER.isRenderingShadowPass();
    }

    public static void registerBasicPipelines(RenderPipeline... pipelines) {
        PROVIDER.registerBasicPipelines(pipelines);
    }

    private static Provider createProvider() {
        return ModList.get().isLoaded(MOD_ID) ? new IrisProvider() : new Provider() {
        };
    }

    private interface Provider {
        default boolean isShaderPackInUse() {
            return false;
        }

        default boolean isRenderingShadowPass() {
            return false;
        }

        default void registerBasicPipelines(RenderPipeline[] pipelines) {
        }
    }

    private static final class IrisProvider implements Provider {
        private final IrisApi api = IrisApi.getInstance();

        @Override
        public boolean isShaderPackInUse() {
            return api.isShaderPackInUse();
        }

        @Override
        public boolean isRenderingShadowPass() {
            return api.isRenderingShadowPass();
        }

        @Override
        public void registerBasicPipelines(RenderPipeline[] pipelines) {
            for (RenderPipeline pipeline : pipelines) {
                api.assignPipeline(pipeline, IrisProgram.BASIC);
            }
        }
    }
}
