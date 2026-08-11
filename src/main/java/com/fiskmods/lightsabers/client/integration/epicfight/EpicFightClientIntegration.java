package com.fiskmods.lightsabers.client.integration.epicfight;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.render.item.LightsaberItemRenderer;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.entity.ModifyPlayerLivingMotionEvent;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class EpicFightClientIntegration {
    private static final int MAX_BLOCK_LIGHT = 15;
    private static final IdentifierProvider LIGHTSABER_TRAIL_MODIFIER =
            IdentifierProvider.constant(Identifier.fromNamespaceAndPath(
                    Lightsabers.MODID,
                    "lightsaber_trail_modifier"
            ));
    private static final Set<AbstractClientPlayerPatch<?>> TRAIL_MODIFIER_PLAYERS =
            Collections.newSetFromMap(new WeakHashMap<>());

    private EpicFightClientIntegration() {
    }

    public static void register() {
        EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_COMPOSITE
                .registerEvent(EpicFightClientIntegration::onCompositeMotionUpdate);
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

    private static void onCompositeMotionUpdate(
            ModifyPlayerLivingMotionEvent.CompositeLayer event
    ) {
        AbstractClientPlayerPatch<?> playerPatch = event.getPlayerPatch();
        registerLightsaberTrailModifier(playerPatch);
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

    private static void registerLightsaberTrailModifier(
            AbstractClientPlayerPatch<?> playerPatch
    ) {
        if (!TRAIL_MODIFIER_PLAYERS.add(playerPatch)) {
            return;
        }
        playerPatch.getEntityDecorations().addTrailInfoModifier(
                LIGHTSABER_TRAIL_MODIFIER,
                (trailInfo, itemCapability) ->
                        modifyLightsaberTrail(playerPatch, trailInfo)
        );
    }

    private static TrailInfo modifyLightsaberTrail(
            AbstractClientPlayerPatch<?> playerPatch,
            TrailInfo trailInfo
    ) {
        if (trailInfo.hand() == null) {
            return trailInfo;
        }

        ItemStack stack = playerPatch.getOriginal().getItemInHand(trailInfo.hand());
        if (!(stack.getItem() instanceof ItemLightsaberBase)
                || trailInfo.start() == null
                || trailInfo.end() == null) {
            return trailInfo;
        }

        TrailInfo.Builder builder = trailInfo.unpackAsBuilder();
        if (!ItemLightsaberBase.isActive(stack)) {
            return builder.endPos(trailInfo.start()).create();
        }

        LightsaberData data = getTrailingBladeData(stack);
        float[] color = data.getRGB(stack);
        Vec3 direction = trailInfo.end().subtract(trailInfo.start()).normalize();
        Vec3 end = trailInfo.start().add(
                direction.scale(LightsaberItemRenderer.getThirdPersonBladeLength(stack))
        );
        return builder
                .endPos(end)
                .r(color[0])
                .g(color[1])
                .b(color[2])
                .blockLight(MAX_BLOCK_LIGHT)
                .create();
    }

    private static LightsaberData getTrailingBladeData(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemDoubleLightsaber)) {
            return LightsaberData.get(stack);
        }

        LightsaberData[] sabers = ItemDoubleLightsaber.get(stack);
        return sabers[ItemDoubleLightsaber.isFlipped(stack) ? 1 : 0];
    }
}
