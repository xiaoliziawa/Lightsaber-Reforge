package com.fiskmods.lightsabers.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Map;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostChain.class)
public interface PostChainAccessor {
    @Accessor("persistentTargets")
    Map<Identifier, RenderTarget> lightsabers$getPersistentTargets();
}
