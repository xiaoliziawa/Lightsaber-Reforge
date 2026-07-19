package com.fiskmods.lightsabers.client.integration.epicfight;

import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public final class EpicFightClientIntegration {
    private EpicFightClientIntegration() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(
                EpicFightClientIntegration::useShieldPoseWhileSpinning
        );
    }

    public static boolean isBattleModeHeldStack(ItemStack stack) {
        return findBattleModePlayerHolding(stack) != null;
    }

    public static boolean isBattleModeUsingStack(ItemStack stack) {
        AbstractClientPlayer player = findBattleModePlayerHolding(stack);
        return player != null && player.isUsingItem() && player.getUseItem() == stack;
    }

    private static AbstractClientPlayer findBattleModePlayerHolding(ItemStack stack) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        for (AbstractClientPlayer player : level.players()) {
            if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) {
                continue;
            }
            PlayerPatch<?> playerPatch = EpicFightCapabilities.getPlayerPatch(player);
            if (playerPatch != null && playerPatch.isEpicFightMode()) {
                return player;
            }
        }
        return null;
    }

    private static void useShieldPoseWhileSpinning(
            UpdatePlayerMotionEvent.CompositeLayer event
    ) {
        AbstractClientPlayerPatch<?> playerPatch = event.getPlayerPatch();
        if (!playerPatch.isEpicFightMode()) {
            return;
        }

        AbstractClientPlayer player = playerPatch.getOriginal();
        if (!player.isUsingItem()) {
            return;
        }

        ItemStack stack = player.getUseItem();
        if (ItemLightsaberBase.isActive(stack)
                && ItemLightsaberBase.isSpinningLightsaber(stack)) {
            ClientAnimator animator = playerPatch.getClientAnimator();
            if (animator.getCompositeLivingMotion(LivingMotions.BLOCK_SHIELD)
                    != Animations.BIPED_BLOCK) {
                animator.addLivingAnimation(
                        LivingMotions.BLOCK_SHIELD,
                        Animations.BIPED_BLOCK
                );
            }
            event.setMotion(LivingMotions.BLOCK_SHIELD);
        }
    }
}
