package com.fiskmods.lightsabers.mixin;

import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@Pseudo
@Mixin(targets = "yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch", remap = false)
public abstract class MixinEpicFightLivingEntityPatch {
    @Shadow
    @Final
    protected EntityDecorations entityDecorations;

    @Shadow
    public abstract CapabilityItem getAdvancedHoldingItemCapability(InteractionHand hand);

    @Inject(method = "getSwingSound", at = @At("HEAD"), cancellable = true)
    private void lightsabers$useInactiveSwingSound(
            InteractionHand hand,
            CallbackInfoReturnable<SoundEvent> callback
    ) {
        LivingEntityPatch<?> entityPatch = (LivingEntityPatch<?>) (Object) this;
        ItemStack stack = entityPatch.getOriginal().getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemLightsaberBase)
                || ItemLightsaberBase.isActive(stack)) {
            return;
        }

        SoundEvent swingSound;
        if (stack.getItem() instanceof ItemDoubleLightsaber
                || ItemLightsaberBase.isSpinningLightsaber(stack)) {
            swingSound = EpicFightSounds.WHOOSH_ROD.get();
        } else if (ItemLightsaberBase.isDaggerLightsaber(stack)) {
            swingSound = EpicFightSounds.WHOOSH_SMALL.get();
        } else {
            swingSound = EpicFightSounds.WHOOSH.get();
        }
        CapabilityItem itemCapability = getAdvancedHoldingItemCapability(hand);
        callback.setReturnValue(
                entityDecorations.getModifiedSwingSound(swingSound, itemCapability)
        );
    }
}
