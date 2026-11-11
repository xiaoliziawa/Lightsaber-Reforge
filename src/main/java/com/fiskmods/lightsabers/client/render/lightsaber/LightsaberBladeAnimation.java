package com.fiskmods.lightsabers.client.render.lightsaber;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.common.config.ModConfig;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Lightsabers.MODID, value = Dist.CLIENT)
public final class LightsaberBladeAnimation {
    private static final Map<ItemStack, AnimationState> STACK_STATES = new WeakHashMap<>();
    private static final Map<LivingEntity, HeldAnimations> HELD_STATES = new WeakHashMap<>();
    private static ClientLevel animationLevel;
    private static long animationTicks;

    public static float getExtension(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        updateLevel(minecraft.level);
        if (animationLevel == null || stack.isEmpty()) {
            return ItemLightsaberBase.isActive(stack) ? 1.0F : 0.0F;
        }

        double time = animationTicks + (double) minecraft.getPartialTick();
        return getState(stack, time).getExtension(time);
    }

    public static void track(LivingEntity entity) {
        track(entity, 0.0F);
    }

    private static void track(LivingEntity entity, float partialTick) {
        if (!entity.level().isClientSide) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        updateLevel(minecraft.level);
        if (entity.level() != animationLevel) {
            return;
        }

        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();
        if (!(mainHand.getItem() instanceof ItemLightsaberBase)
                && !(offHand.getItem() instanceof ItemLightsaberBase)) {
            HELD_STATES.remove(entity);
            return;
        }

        HeldAnimations held = HELD_STATES.get(entity);
        if (held == null) {
            held = new HeldAnimations();
            HELD_STATES.put(entity, held);
        }
        int selectedSlot = entity instanceof LocalPlayer player
                ? player.getInventory().selected
                : -1;
        double time = animationTicks + (double) partialTick;
        held.mainHand.track(mainHand, selectedSlot, time);
        held.offHand.track(offHand, -1, time);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        track(event.getEntity());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        updateLevel(minecraft.level);
        if (animationLevel != null && !minecraft.isPaused()) {
            animationTicks++;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        track(event.getEntity(), event.getPartialTick());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            track(player, event.getPartialTick());
        }
    }

    private static void updateLevel(ClientLevel level) {
        if (animationLevel != level) {
            STACK_STATES.clear();
            HELD_STATES.clear();
            animationLevel = level;
            animationTicks = 0L;
        }
    }

    private static AnimationState getState(ItemStack stack, double time) {
        AnimationState state = STACK_STATES.get(stack);
        if (state == null) {
            state = new AnimationState(stack, time);
            STACK_STATES.put(stack, state);
        }
        return state;
    }

    private static final class HeldAnimations {
        private final HandAnimation mainHand = new HandAnimation();
        private final HandAnimation offHand = new HandAnimation();
    }

    private static final class HandAnimation {
        private ItemStack stack = ItemStack.EMPTY;
        private int slot = -1;
        private AnimationState state;

        private void track(ItemStack currentStack, int currentSlot, double time) {
            if (!(currentStack.getItem() instanceof ItemLightsaberBase)) {
                stack = ItemStack.EMPTY;
                state = null;
                slot = currentSlot;
                return;
            }

            if (stack != currentStack) {
                AnimationState existing = STACK_STATES.get(currentStack);
                if (existing != null && existing.trackedInHand) {
                    state = existing;
                } else if (state != null
                        && slot == currentSlot
                        && ItemLightsaberBase.matchesIgnoringActive(stack, currentStack)) {
                    STACK_STATES.put(currentStack, state);
                } else {
                    state = getState(currentStack, time);
                }
                stack = currentStack;
            }
            slot = currentSlot;
            state.bind(currentStack, time);
        }
    }

    private static final class AnimationState {
        private WeakReference<ItemStack> source;
        private boolean active;
        private boolean trackedInHand;
        private float progress;
        private double lastTime;

        private AnimationState(ItemStack stack, double time) {
            source = new WeakReference<>(stack);
            active = ItemLightsaberBase.isActive(stack);
            progress = active ? 1.0F : 0.0F;
            lastTime = time;
        }

        private void bind(ItemStack stack, double time) {
            update(time, ItemLightsaberBase.isActive(stack));
            trackedInHand = true;
            if (source.get() != stack) {
                source = new WeakReference<>(stack);
            }
        }

        private float getExtension(double time) {
            ItemStack stack = source.get();
            update(time, stack == null ? active : ItemLightsaberBase.isActive(stack));
            return progress * progress * (3.0F - 2.0F * progress);
        }

        private void update(double time, boolean targetActive) {
            int duration = ModConfig.bladeAnimationDurationTicks;
            if (duration <= 0) {
                progress = targetActive ? 1.0F : 0.0F;
            } else {
                float step = (float) (Math.max(time - lastTime, 0.0D) / duration);
                progress = Mth.clamp(progress + (active ? step : -step), 0.0F, 1.0F);
            }
            active = targetActive;
            lastTime = Math.max(time, lastTime);
        }
    }
}
