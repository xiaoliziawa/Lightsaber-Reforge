package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.common.tileentity.TileEntitySithCoffin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerSithCoffin extends AbstractContainerMenu {
    public static final int VISIBLE_ROWS = 3;

    private static final int VISIBLE_CONTAINER_SLOTS = VISIBLE_ROWS * 9;
    private static final int PLAYER_INVENTORY_START = VISIBLE_CONTAINER_SLOTS;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    public final TileEntitySithCoffin coffin;

    public ContainerSithCoffin(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(containerId, playerInventory, getCoffin(playerInventory, buffer));
    }

    public ContainerSithCoffin(
            int containerId,
            Inventory playerInventory,
            TileEntitySithCoffin coffin
    ) {
        super(ModMenus.SITH_COFFIN.get(), containerId);
        this.coffin = coffin;
        checkContainerSize(coffin, 28);

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        coffin,
                        column + row * 9,
                        8 + column * 18,
                        18 + row * 18
                ));
            }
        }
        addPlayerInventory(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return coffin.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack originalStack = sourceStack.copy();
        if (slotId < VISIBLE_CONTAINER_SLOTS) {
            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(
                sourceStack,
                0,
                VISIBLE_CONTAINER_SLOTS,
                false
        )) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, sourceStack);
        return originalStack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int inventoryOffset = 2 + (VISIBLE_ROWS - 3) * 18;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        84 + inventoryOffset + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    142 + inventoryOffset
            ));
        }
    }

    private static TileEntitySithCoffin getCoffin(
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(
                buffer.readBlockPos()
        );
        if (blockEntity instanceof TileEntitySithCoffin coffin) {
            return coffin;
        }
        throw new IllegalStateException("Missing Sith coffin block entity");
    }
}
