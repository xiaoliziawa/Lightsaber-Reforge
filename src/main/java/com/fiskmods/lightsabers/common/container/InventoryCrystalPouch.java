package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemCrystalPouch;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class InventoryCrystalPouch extends SimpleContainer {
    private static final String SLOTS_TAG = "Slots";
    private static final String SLOT_TAG = "Slot";
    private static final int MAX_STACK_SIZE = 16;

    public final Player player;
    public final UUID uuid;
    public final int itemSlot;

    private boolean loading;

    public InventoryCrystalPouch(Player player, int itemSlot) {
        super(CrystalColor.values().length);
        this.player = player;
        this.itemSlot = itemSlot;

        ItemStack pouchStack = getPouchStack();
        uuid = player.level().isClientSide()
                ? ItemCrystalPouch.getUUID(pouchStack)
                : ItemCrystalPouch.getOrCreateUUID(pouchStack);
        readFromNBT(ItemDataHelper.getCustomData(pouchStack));
    }

    public ItemStack getPouchStack() {
        return itemSlot >= 0 && itemSlot < player.getInventory().getContainerSize()
                ? player.getInventory().getItem(itemSlot)
                : ItemStack.EMPTY;
    }

    public boolean addItemStackToInventory(ItemStack stack) {
        if (!ItemCrystal.isCrystal(stack)) {
            return false;
        }

        int slot = ItemCrystal.getId(stack);
        ItemStack storedStack = getItem(slot);
        if (!storedStack.isEmpty() && !ItemStack.isSameItemSameComponents(storedStack, stack)) {
            return false;
        }

        int storedCount = storedStack.getCount();
        int amount = Math.min(stack.getCount(), MAX_STACK_SIZE - storedCount);
        if (amount <= 0) {
            return false;
        }

        if (storedStack.isEmpty()) {
            ItemStack insertedStack = stack.copy();
            insertedStack.setCount(amount);
            setItem(slot, insertedStack);
        } else {
            storedStack.grow(amount);
            setChanged();
        }
        stack.shrink(amount);
        return true;
    }

    public void readFromNBT(CompoundTag tag) {
        if (tag == null || tag.getList(SLOTS_TAG).isEmpty()) {
            return;
        }

        loading = true;
        try {
            clearContent();
            ListTag slots = tag.getListOrEmpty(SLOTS_TAG);
            for (int i = 0; i < slots.size(); i++) {
                CompoundTag slotTag = slots.getCompoundOrEmpty(i);
                int slot = Byte.toUnsignedInt(slotTag.getByteOr(SLOT_TAG, (byte) 0));
                if (slot < getContainerSize()) {
                    ItemStack stack = slotTag.read(
                            ItemStack.MAP_CODEC,
                            player.registryAccess().createSerializationContext(NbtOps.INSTANCE)
                    ).orElse(ItemStack.EMPTY);
                    if (canPlaceItem(slot, stack)) {
                        setItem(slot, stack);
                    }
                }
            }
        } finally {
            loading = false;
        }
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag slots = new ListTag();
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.store(
                        ItemStack.MAP_CODEC,
                        player.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        stack
                );
                slotTag.putByte(SLOT_TAG, (byte) slot);
                slots.add(slotTag);
            }
        }
        tag.put(SLOTS_TAG, slots);
        return tag;
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK_SIZE;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading && !player.level().isClientSide() && stillValid(player)) {
            ItemDataHelper.updateCustomData(getPouchStack(), this::writeToNBT);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.player == player
                && !ItemCrystalPouch.NULL_UUID.equals(uuid)
                && uuid.equals(ItemCrystalPouch.getUUID(getPouchStack()));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return ItemCrystal.isCrystal(stack) && slot == ItemCrystal.getId(stack);
    }
}
