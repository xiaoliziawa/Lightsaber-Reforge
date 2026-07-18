package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.container.ContainerCrystalPouch;
import com.fiskmods.lightsabers.common.container.InventoryCrystalPouch;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.UUID;

public class ItemCrystalPouch extends Item {
    public static final UUID NULL_UUID = new UUID(0, 0);

    public ItemCrystalPouch() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (usedHand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int itemSlot = player.getInventory().selected;
        getOrCreateUUID(stack);
        ServerPlayer serverPlayer = (ServerPlayer) player;
        NetworkHooks.openScreen(
                serverPlayer,
                new SimpleMenuProvider(
                        (containerId, playerInventory, menuPlayer) ->
                                new ContainerCrystalPouch(
                                        containerId,
                                        playerInventory,
                                        new InventoryCrystalPouch(menuPlayer, itemSlot)
                                ),
                        Component.translatable("gui.crystal_pouch")
                ),
                buffer -> buffer.writeVarInt(itemSlot)
        );
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slotId,
            boolean isSelected
    ) {
        if (!level.isClientSide) {
            getOrCreateUUID(stack);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return ItemCrystal.rarityMap.get(ItemCrystal.get(stack));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(ItemCrystal.get(stack).getUnlocalizedName())
                .withStyle(ChatFormatting.GRAY));
    }

    public static boolean isPouch(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemCrystalPouch;
    }

    public static UUID getUUID(ItemStack stack) {
        if (!isPouch(stack) || !stack.hasTag()) {
            return NULL_UUID;
        }

        CompoundTag tag = stack.getTag();
        String value = tag.getString(ALConstants.TAG_POUCH_UUID);
        if (value.isEmpty()) {
            return NULL_UUID;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return NULL_UUID;
        }
    }

    public static UUID getOrCreateUUID(ItemStack stack) {
        UUID uuid = getUUID(stack);
        if (!NULL_UUID.equals(uuid) || !isPouch(stack)) {
            return uuid;
        }

        uuid = UUID.randomUUID();
        stack.getOrCreateTag().putString(ALConstants.TAG_POUCH_UUID, uuid.toString());
        return uuid;
    }
}
