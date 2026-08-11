package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.Lightsabers;
import com.fiskmods.lightsabers.client.sound.ALSounds;
import com.fiskmods.lightsabers.common.entity.EntityLightsaber;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.sound.ModSounds;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class ItemLightsaberBase extends Item {
    private static final String ACTIVE_TAG = "active";
    private static final int ENCHANTMENT_VALUE = 10;
    private static final int ATTACK_DAMAGE_MODIFIER = 5;
    private static final float ATTACK_SPEED_MODIFIER = -2.4F;
    private static final float PLAYER_BASE_ATTACK_DAMAGE = 1.0F;
    private static final float SINGLE_ATTACK_DAMAGE = 13.0F;
    private static final float SPINNING_ATTACK_DAMAGE = 20.0F;
    private static final float SPEAR_ATTACK_DAMAGE = 25.0F;
    private static final int TIER_DURABILITY = 0;
    private static final float TIER_MINING_SPEED = 8.0F;
    private static final float TIER_ATTACK_DAMAGE_BONUS = 3.0F;
    private static final ToolMaterial LIGHTSABER_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            TIER_DURABILITY,
            TIER_MINING_SPEED,
            TIER_ATTACK_DAMAGE_BONUS,
            ENCHANTMENT_VALUE,
            ItemTags.DIAMOND_TOOL_MATERIALS
    );

    protected ItemLightsaberBase(Item.Properties properties) {
        // Sword tool/weapon behaviour comes from the material, but the ATTRIBUTE_MODIFIERS
        // component it writes is dropped again so getDefaultAttributeModifiers stays in
        // control of the per-hilt attack damage.
        super(
                properties
                        .sword(LIGHTSABER_MATERIAL, ATTACK_DAMAGE_MODIFIER, ATTACK_SPEED_MODIFIER)
                        .stacksTo(1)
                        .setNoCombineRepair()
                        .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                        .component(DataComponents.ENCHANTABLE, new Enchantable(ENCHANTMENT_VALUE))
                        .attributes(ItemAttributeModifiers.EMPTY)
        );
    }

    public float getAttackDamage(ItemStack stack) {
        if (isSpearLightsaber(stack)) {
            return SPEAR_ATTACK_DAMAGE;
        }
        return isSpinningLightsaber(stack) ? SPINNING_ATTACK_DAMAGE : SINGLE_ATTACK_DAMAGE;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                getAttackDamage(stack) - PLAYER_BASE_ATTACK_DAMAGE,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                ATTACK_SPEED_MODIFIER,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean onEntitySwing(
            ItemStack stack,
            LivingEntity entity,
            InteractionHand hand
    ) {
        if (!isActive(stack)) {
            return false;
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
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Epic Fight has no 26.1.2 build, so its battle mode check is disabled:
        // && (!(attacker instanceof Player player) || !Lightsabers.isEpicFightLoaded
        //         || !EpicFightIntegration.isBattleMode(player))
        if (!attacker.level().isClientSide()) {
            attacker.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    attacker instanceof Player
                            ? ModSounds.PLAYER_LIGHTSABER_HIT.get()
                            : ModSounds.MOB_LIGHTSABER_HIT.get(),
                    attacker.getSoundSource(),
                    1.0F,
                    1.0F
            );
        }
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
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        return !stack.isEmpty() && tag != null && tag.getBooleanOr(ACTIVE_TAG, false);
    }

    public static boolean isSpinningLightsaber(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemLightsaber)) {
            return false;
        }

        return LightsaberData.get(stack).canSpinBlades();
    }

    public static boolean isSpearLightsaber(ItemStack stack) {
        return stack.getItem() instanceof ItemLightsaber
                && LightsaberData.get(stack).isSpear();
    }

    public static boolean isDaggerLightsaber(ItemStack stack) {
        return !isSpinningLightsaber(stack)
                && stack.getItem() instanceof ItemLightsaber
                && LightsaberData.hasFocusingCrystal(stack, FocusingCrystal.DAGGER);
    }

    public static ItemStack setActive(ItemStack stack, boolean active) {
        if (!stack.isEmpty()) {
            ItemDataHelper.updateCustomData(stack, tag -> tag.putBoolean(ACTIVE_TAG, active));
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
        if (entity.level().isClientSide() || stack.isEmpty()) {
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
