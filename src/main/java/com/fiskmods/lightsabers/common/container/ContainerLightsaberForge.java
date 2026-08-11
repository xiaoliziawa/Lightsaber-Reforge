package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.item.ILightsaberComponent;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Objects;

public class ContainerLightsaberForge extends AbstractContainerMenu {
    public static final int[][] SLOTS = {
            {20, 17}, {20, 35}, {20, 53}, {20, 71},
            {43, 71}, {66, 71}, {89, 71}, {107, 71}
    };

    private static final int INPUT_SLOT_COUNT = SLOTS.length;
    private static final int OUTPUT_SLOT = INPUT_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = OUTPUT_SLOT + 1;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    public final InventoryLightsaberForge craftMatrix = new InventoryLightsaberForge();
    public final ResultContainer craftResult = new ResultContainer();

    private final ContainerLevelAccess access;
    private final Level level;

    public ContainerLightsaberForge(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf data
    ) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public ContainerLightsaberForge(
            int containerId,
            Inventory playerInventory,
            BlockPos blockPos
    ) {
        super(ModMenus.LIGHTSABER_FORGE.get(), containerId);
        level = playerInventory.player.level();
        access = ContainerLevelAccess.create(level, blockPos);

        craftMatrix.setChangeListener(() -> slotsChanged(craftMatrix));
        for (int slot = 0; slot < SLOTS.length; slot++) {
            addSlot(new InputSlot(craftMatrix, slot, SLOTS[slot][0], SLOTS[slot][1]));
        }
        addSlot(new OutputSlot(craftResult, 0, 136, 87));
        addPlayerInventory(playerInventory, 30);
        slotsChanged(craftMatrix);
    }

    @Override
    public void slotsChanged(Container container) {
        LightsaberData resultData = craftMatrix.updateResult();
        ItemStack resultStack = ItemStack.EMPTY;
        if (resultData != null) {
            resultStack = ItemLightsaberBase.setActive(resultData.create(), true);
            ItemStack specialStack = craftMatrix.getItem(5);
            if (!specialStack.isEmpty() && specialStack.is(ItemTags.FISHES)) {
                String specialItem = Objects.requireNonNull(
                        BuiltInRegistries.ITEM.getKey(specialStack.getItem())
                ).toString();
                ItemDataHelper.updateCustomData(
                        resultStack,
                        tag -> tag.putString(ALConstants.TAG_LIGHTSABER_SPECIAL, specialItem)
                );
            }
        }
        craftResult.setItem(0, resultStack);
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((actualLevel, pos) -> {
            BlockState blockState = actualLevel.getBlockState(pos);
            boolean validBlock = blockState.is(ModBlocks.LIGHTSABER_FORGE.get());
            return validBlock && player.distanceToSqr(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D
            ) <= 64.0D;
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        ItemStack originalStack = sourceStack.copy();
        if (slotId == OUTPUT_SLOT) {
            if (!slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            ItemLightsaberBase.setActive(sourceStack, false);
            ItemLightsaberBase.setActive(originalStack, false);
            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    true
            )) {
                ItemLightsaberBase.setActive(sourceStack, true);
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(sourceStack, originalStack);
        } else if (slotId < INPUT_SLOT_COUNT) {
            if (!moveItemStackTo(
                    sourceStack,
                    PLAYER_INVENTORY_START,
                    PLAYER_INVENTORY_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        } else if (!moveIntoInputSlot(sourceStack)) {
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

        if (sourceStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotId == OUTPUT_SLOT ? originalStack : sourceStack);
        return originalStack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftResult && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!level.isClientSide()) {
            for (int slot = 0; slot < craftMatrix.getContainerSize(); slot++) {
                ItemStack stack = craftMatrix.removeItemNoUpdate(slot);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }

    private boolean moveIntoInputSlot(ItemStack stack) {
        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
            Slot target = slots.get(slot);
            if (!target.hasItem()
                    && target.mayPlace(stack)
                    && moveItemStackTo(stack, slot, slot + 1, false)) {
                return true;
            }
        }
        return false;
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

    private final class InputSlot extends Slot {
        private InputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            int slot = getContainerSlot();
            boolean compatible = stack.getItem() instanceof ILightsaberComponent component
                    && component.isCompatibleSlot(stack, slot);
            if (!compatible && (slot != 5 || !stack.is(ItemTags.FISHES))) {
                return false;
            }

            for (int otherSlot = 0; otherSlot < craftMatrix.getContainerSize(); otherSlot++) {
                ItemStack existingStack = craftMatrix.getItem(otherSlot);
                if (!existingStack.isEmpty()
                        && ItemStack.isSameItemSameComponents(existingStack, stack)) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            LightsaberData resultData = craftMatrix.getResult();
            return resultData != null && !resultData.isTooShort();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            ItemLightsaberBase.setActive(stack, false);
            stack.onCraftedBy(player, 1);
            EventHooks.firePlayerCraftingEvent(player, stack, craftMatrix);
            for (int slot = 0; slot < craftMatrix.getContainerSize(); slot++) {
                if (!craftMatrix.getItem(slot).isEmpty()) {
                    craftMatrix.removeItem(slot, 1);
                }
            }
            super.onTake(player, stack);
        }
    }
}
