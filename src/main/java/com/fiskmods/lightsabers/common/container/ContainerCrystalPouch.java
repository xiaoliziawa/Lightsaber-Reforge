package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemCrystalPouch;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCrystalPouch extends AbstractContainerMenu {
    private static final int EXTRA_CRYSTAL_ROW_OFFSET = 18;
    private static final int PLAYER_INVENTORY_START = CrystalColor.values().length;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    public final InventoryCrystalPouch inventory;

    public ContainerCrystalPouch(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, new InventoryCrystalPouch(
                playerInventory.player,
                data.readVarInt()
        ));
    }

    public ContainerCrystalPouch(
            int containerId,
            Inventory playerInventory,
            InventoryCrystalPouch pouchInventory
    ) {
        super(ModMenus.CRYSTAL_POUCH.get(), containerId);
        inventory = pouchInventory;

        for (int slot = 0; slot < pouchInventory.getContainerSize(); slot++) {
            addSlot(new CrystalSlot(
                    pouchInventory,
                    slot,
                    8 + slot % 9 * 18,
                    18 + slot / 9 * 18
            ));
        }
        addPlayerInventory(playerInventory);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + 9;
                addSlot(createPlayerSlot(
                        playerInventory,
                        slot,
                        8 + column * 18,
                        68 + EXTRA_CRYSTAL_ROW_OFFSET + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(createPlayerSlot(
                playerInventory,
                column,
                8 + column * 18,
                126 + EXTRA_CRYSTAL_ROW_OFFSET
            ));
        }
    }

    private Slot createPlayerSlot(Inventory playerInventory, int slot, int x, int y) {
        if (slot == inventory.itemSlot) {
            return new FrozenSlot(playerInventory, slot, x, y);
        }
        return new ExclusiveSlot(playerInventory, slot, x, y);
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack originalStack = sourceStack.copy();
        if (ItemCrystalPouch.isPouch(sourceStack)) {
            return ItemStack.EMPTY;
        }

        if (slotId < inventory.getContainerSize()) {
            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else if (ItemCrystal.isCrystal(sourceStack)) {
            int crystalSlot = ItemCrystal.getId(sourceStack);
            if (!moveItemStackTo(sourceStack, crystalSlot, crystalSlot + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, sourceStack);
        return originalStack;
    }

    private final class CrystalSlot extends Slot {
        private CrystalSlot(InventoryCrystalPouch inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return inventory.canPlaceItem(getContainerSlot(), stack);
        }
    }

    private static final class FrozenSlot extends Slot {
        private FrozenSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    private final class ExclusiveSlot extends Slot {
        private ExclusiveSlot(Inventory inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !inventoryUuidMatches(stack);
        }

        private boolean inventoryUuidMatches(ItemStack stack) {
            return ContainerCrystalPouch.this.inventory.uuid.equals(
                    ItemCrystalPouch.getUUID(stack)
            );
        }
    }
}
