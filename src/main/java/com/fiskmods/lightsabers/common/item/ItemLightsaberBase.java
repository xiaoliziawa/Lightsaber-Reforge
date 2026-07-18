package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.client.render.item.LightsaberClientItemExtensions;
import com.fiskmods.lightsabers.common.entity.EntityLightsaber;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public abstract class ItemLightsaberBase extends SwordItem implements IForgeItem {
    private static final String ACTIVE_TAG = "active";
    private static final int ATTACK_DAMAGE_MODIFIER = 5;
    private static final float ATTACK_SPEED_MODIFIER = -2.4F;
    private static final Tier LIGHTSABER_TIER = new Tier() {
        @Override
        public int getUses() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 10;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    protected ItemLightsaberBase() {
        super(
                LIGHTSABER_TIER,
                ATTACK_DAMAGE_MODIFIER,
                ATTACK_SPEED_MODIFIER,
                new Item.Properties().stacksTo(1)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(LightsaberClientItemExtensions.INSTANCE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!isActive(stack)) {
            return false;
        }

        if (Lightsabers.proxy.isClientPlayer(entity)) {
            Lightsabers.proxy.playLocalSound(
                    (Player) entity,
                    ALSounds.player_lightsaber_swing,
                    1.0F,
                    1.0F
            );
        }

        HitResult hitResult = entity.pick(5.0D, 1.0F, true);
        if (hitResult.getType() == HitResult.Type.BLOCK
                && hitResult instanceof BlockHitResult blockHitResult) {
            return onPunchBlock(stack, entity, blockHitResult);
        }
        return false;
    }

    public boolean onPunchBlock(
            ItemStack stack,
            LivingEntity entity,
            BlockHitResult hitResult
    ) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return !isActive(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public boolean mineBlock(
            ItemStack stack,
            Level level,
            BlockState state,
            BlockPos pos,
            LivingEntity entity
    ) {
        return true;
    }

    public static boolean isActive(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }

    public static ItemStack setActive(ItemStack stack, boolean active) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
        }
        return stack;
    }

    public static void ignite(LivingEntity entity, boolean active) {
        ItemStack stack = entity.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemLightsaberBase)
                || isActive(stack) == active) {
            return;
        }

        setActive(stack, active);
        if (entity instanceof Player player && Lightsabers.proxy.isClientPlayer(player)) {
            Lightsabers.proxy.playLocalSound(
                    player,
                    active ? ALSounds.player_lightsaber_on : ALSounds.player_lightsaber_off,
                    1.0F,
                    1.0F
            );
        }
    }

    public static void throwLightsaber(LivingEntity entity, ItemStack stack, int amplifier) {
        if (entity.level().isClientSide || stack.isEmpty()) {
            return;
        }

        EntityLightsaber thrownLightsaber = new EntityLightsaber(
                entity.level(),
                entity,
                stack,
                amplifier
        );
        entity.level().addFreshEntity(thrownLightsaber);
        entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}
