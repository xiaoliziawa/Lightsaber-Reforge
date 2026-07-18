package com.fiskmods.lightsabers.mixin;

import com.fiskmods.lightsabers.helper.ALRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void lightsabers$gazeGlow(Entity entity, CallbackInfoReturnable<Boolean> callback) {
        if (ALRenderHelper.shouldGazeGlow(entity)) {
            callback.setReturnValue(true);
        }
    }
}
