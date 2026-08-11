package com.fiskmods.lightsabers.mixin;

import com.fiskmods.lightsabers.client.model.ForcePowerModelAnimator;
import com.fiskmods.lightsabers.common.event.ClientEventHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL")
    )
    private void lightsabers$applyForcePowerPose(
            HumanoidRenderState renderState,
            CallbackInfo callback
    ) {
        LivingEntity entity = ClientEventHandler.getRenderedEntity(renderState);
        if (entity == null) {
            return;
        }
        ForcePowerModelAnimator.apply(
                (HumanoidModel<?>) (Object) this,
                renderState,
                entity
        );
    }
}
