package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.common.tileentity.TileEntityDisassemblyStation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerDisassemblyStation extends AbstractContainerMenu {
    private static final int FIRST_OUTPUT_SLOT = 2;
    private static final int LAST_OUTPUT_SLOT = 16;
    private static final int PLAYER_INVENTORY_START = LAST_OUTPUT_SLOT + 1;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    public final TileEntityDisassemblyStation station;

    private final ContainerData data;

    public ContainerDisassemblyStation(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(containerId, playerInventory, getStation(playerInventory, buffer));
    }

    public ContainerDisassemblyStation(
            int containerId,
            Inventory playerInventory,
            TileEntityDisassemblyStation station
    ) {
        super(ModMenus.DISASSEMBLY_STATION.get(), containerId);
        this.station = station;
        data = station.getDataAccess();
        checkContainerSize(station, 17);
        checkContainerDataCount(data, 3);

        addSlot(new ValidityCheckedSlot(station, TileEntityDisassemblyStation.INPUT, 16, 18));
        addSlot(new ValidityCheckedSlot(station, TileEntityDisassemblyStation.FUEL, 16, 54));
        for (int slot = 0; slot < 15; slot++) {
            addSlot(new ValidityCheckedSlot(
                    station,
                    slot + FIRST_OUTPUT_SLOT,
                    72 + 18 * (slot % 5),
                    18 + 18 * (slot / 5)
            ));
        }
        addPlayerInventory(playerInventory, 2);
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return station.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack originalStack = sourceStack.copy();
        if (slotId >= FIRST_OUTPUT_SLOT && slotId <= LAST_OUTPUT_SLOT) {
            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    true
            )) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(sourceStack, originalStack);
        } else if (slotId >= PLAYER_INVENTORY_START) {
            if (TileEntityDisassemblyStation.canDisassemble(sourceStack)) {
                if (!moveItemStackTo(
                        sourceStack,
                        TileEntityDisassemblyStation.INPUT,
                        TileEntityDisassemblyStation.INPUT + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            } else if (TileEntityDisassemblyStation.isItemFuel(sourceStack)) {
                if (!moveItemStackTo(
                        sourceStack,
                        TileEntityDisassemblyStation.FUEL,
                        TileEntityDisassemblyStation.FUEL + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            } else {
                int inventoryEnd = PLAYER_INVENTORY_START + 27;
                if (slotId < inventoryEnd) {
                    if (!moveItemStackTo(
                            sourceStack,
                            inventoryEnd,
                            PLAYER_INVENTORY_END,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(
                        sourceStack,
                        PLAYER_INVENTORY_START,
                        inventoryEnd,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!moveItemStackTo(
                sourceStack,
                PLAYER_INVENTORY_START,
                PLAYER_INVENTORY_END,
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

    public boolean isBurning() {
        return data.get(1) > 0;
    }

    public int getDisassemblyProgress(int scale) {
        return data.get(0) * scale / TileEntityDisassemblyStation.TICKS_DISASSEMBLY;
    }

    public int getBurnProgress(int scale) {
        int maxFuelTicks = data.get(2);
        if (maxFuelTicks == 0) {
            maxFuelTicks = 200;
        }
        return data.get(1) * scale / maxFuelTicks;
    }

    private void addPlayerInventory(Inventory playerInventory, int yOffset) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        84 + yOffset + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    142 + yOffset
            ));
        }
    }

    private static TileEntityDisassemblyStation getStation(
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(
                buffer.readBlockPos()
        );
        if (blockEntity instanceof TileEntityDisassemblyStation station) {
            return station;
        }
        throw new IllegalStateException("Missing disassembly station block entity");
    }

    private static final class ValidityCheckedSlot extends Slot {
        private ValidityCheckedSlot(
                TileEntityDisassemblyStation station,
                int slot,
                int x,
                int y
        ) {
            super(station, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }
    }
}
