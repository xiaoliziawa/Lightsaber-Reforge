package com.fiskmods.lightsabers.common.tileentity;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemDoubleLightsaber;
import com.fiskmods.lightsabers.common.item.ItemFocusingCrystal;
import com.fiskmods.lightsabers.common.item.ItemLightsaberBase;
import com.fiskmods.lightsabers.common.item.ItemLightsaberPart;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.common.lightsaber.FocusingCrystal;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TileEntityDisassemblyStation extends BlockEntity implements WorldlyContainer {
    public static final int TICKS_DISASSEMBLY = 2400;
    public static final int INPUT = 0;
    public static final int FUEL = 1;

    private static final int INVENTORY_SIZE = 17;
    private static final int FIRST_OUTPUT_SLOT = 2;
    private static final int[] SLOTS_TOP = {INPUT};
    private static final int[] SLOTS_BOTTOM = {
            2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, FUEL
    };
    private static final int[] SLOTS_SIDES = {INPUT, FUEL};
    private static final String BURN_TIME_TAG = "BurnTime";
    private static final String DISASSEMBLY_TIME_TAG = "DisassemblyTime";

    private static final Map<ItemStack, Integer> FUELS = new LinkedHashMap<>();

    private final NonNullList<ItemStack> items = NonNullList.withSize(
            INVENTORY_SIZE,
            ItemStack.EMPTY
    );
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> fuelTicks;
                case 2 -> maxFuelTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> fuelTicks = value;
                case 2 -> maxFuelTicks = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private int fuelTicks;
    private int maxFuelTicks;
    private int progress;

    public TileEntityDisassemblyStation(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISASSEMBLY_STATION.get(), pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            TileEntityDisassemblyStation station
    ) {
        boolean wasBurning = station.isBurning();
        boolean changed = false;

        if (station.fuelTicks > 0) {
            station.fuelTicks--;
        }

        ItemStack fuelStack = station.items.get(FUEL);
        ItemStack inputStack = station.items.get(INPUT);
        if (station.fuelTicks != 0 || !fuelStack.isEmpty() && !inputStack.isEmpty()) {
            if (station.fuelTicks == 0 && canDisassemble(inputStack)) {
                int burnTime = getItemBurnTime(fuelStack);
                if (burnTime > 0) {
                    station.maxFuelTicks = burnTime;
                    station.fuelTicks = burnTime;
                    fuelStack.shrink(1);
                    if (fuelStack.isEmpty()) {
                        station.items.set(FUEL, ItemStack.EMPTY);
                    }
                    changed = true;
                }
            }

            if (station.isBurning() && canDisassemble(inputStack)) {
                station.progress++;
                if (station.progress >= TICKS_DISASSEMBLY) {
                    station.progress = 0;
                    station.disassembleItem((ServerLevel) level);
                    changed = true;
                }
            } else if (station.progress != 0) {
                station.progress = 0;
                changed = true;
            }
        } else if (station.progress != 0) {
            station.progress = 0;
            changed = true;
        }

        if (wasBurning != station.isBurning()) {
            changed = true;
        }
        if (changed) {
            station.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public boolean isBurning() {
        return fuelTicks > 0;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public static boolean canDisassemble(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemLightsaberBase;
    }

    private void disassembleItem(ServerLevel level) {
        ItemStack inputStack = items.get(INPUT);
        if (!canDisassemble(inputStack)) {
            return;
        }

        for (Map.Entry<ItemStack, Float> entry : getOutput(inputStack).entrySet()) {
            if (entry.getValue() > level.getRandom().nextFloat()) {
                addOutputItem(level, entry.getKey());
            }
        }
        inputStack.shrink(1);
        if (inputStack.isEmpty()) {
            items.set(INPUT, ItemStack.EMPTY);
        }
    }

    private void addOutputItem(ServerLevel level, ItemStack outputStack) {
        ItemStack remainingStack = outputStack.copy();
        for (int slot = FIRST_OUTPUT_SLOT; slot < getContainerSize(); slot++) {
            ItemStack existingStack = items.get(slot);
            if (existingStack.isEmpty()) {
                items.set(slot, remainingStack.copy());
                return;
            }
            if (ItemStack.matches(remainingStack, existingStack)) {
                int moved = Math.min(
                        remainingStack.getCount(),
                        existingStack.getMaxStackSize() - existingStack.getCount()
                );
                existingStack.grow(moved);
                remainingStack.shrink(moved);
                if (remainingStack.isEmpty()) {
                    return;
                }
            }
        }
        Block.popResource(level, worldPosition, remainingStack);
    }

    public static Map<ItemStack, Float> getOutput(ItemStack stack) {
        if (stack.getItem() instanceof ItemDoubleLightsaber) {
            LightsaberData[] data = ItemDoubleLightsaber.get(stack);
            Map<ItemStack, Float> drops = getOutput(data[0], true);
            drops.putAll(getOutput(data[1], true));
            return drops;
        }
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        return getOutput(
                LightsaberData.get(stack),
                tag == null || !tag.contains(ALConstants.TAG_LIGHTSABER_SPECIAL)
        );
    }

    public static Map<ItemStack, Float> getOutput(
            LightsaberData data,
            boolean salvageColor
    ) {
        Map<ItemStack, Float> drops = new LinkedHashMap<>();
        drops.put(new ItemStack(ModItems.CIRCUITRY.get()), 0.25F);

        if (data.isHiltUniform()) {
            for (PartType type : PartType.values()) {
                drops.put(ItemLightsaberPart.create(type, data.get(type)), 1.0F);
            }
        } else {
            Map<Hilt, Integer> hilts = new HashMap<>();
            for (Hilt hilt : data.getHilt()) {
                hilts.put(hilt, hilts.getOrDefault(hilt, -1) + 1);
            }
            for (PartType type : PartType.values()) {
                Hilt hilt = data.get(type);
                drops.put(
                        ItemLightsaberPart.create(type, hilt),
                        0.66F + 0.05F * hilts.get(hilt)
                );
            }
        }

        for (FocusingCrystal crystal : data.getFocusingCrystals()) {
            drops.put(ItemFocusingCrystal.create(crystal), 0.725F);
        }
        if (salvageColor) {
            CrystalColor color = data.getColor();
            drops.put(
                    ItemCrystal.create(color),
                    0.35F + 0.125F * ItemCrystal.rarityMap.get(color).ordinal()
            );
        }
        return drops;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT && canDisassemble(stack)
                || slot == FUEL && isItemFuel(stack);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN
                ? SLOTS_BOTTOM
                : side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(
            int slot,
            ItemStack stack,
            @Nullable Direction direction
    ) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(
            int slot,
            ItemStack stack,
            Direction direction
    ) {
        return direction != Direction.DOWN || slot != FUEL;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        fuelTicks = input.getShortOr(BURN_TIME_TAG, (short) 0);
        progress = input.getShortOr(DISASSEMBLY_TIME_TAG, (short) 0);
        maxFuelTicks = getItemBurnTime(items.get(FUEL));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort(BURN_TIME_TAG, (short) fuelTicks);
        output.putShort(DISASSEMBLY_TIME_TAG, (short) progress);
        ContainerHelper.saveAllItems(output, items);
    }

    public static int getItemBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (Map.Entry<ItemStack, Integer> entry : FUELS.entrySet()) {
            ItemStack fuelStack = entry.getKey();
            if (fuelStack.is(stack.getItem())
                    && fuelStack.getDamageValue() == stack.getDamageValue()) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public static boolean isItemFuel(ItemStack stack) {
        return getItemBurnTime(stack) > 0;
    }

    public static void registerFuel(ItemStack stack, int ticks) {
        ItemStack fuelStack = stack.copy();
        fuelStack.setCount(1);
        FUELS.put(fuelStack, ticks);
    }

    public static ImmutableMap<ItemStack, Integer> getFuels() {
        return ImmutableMap.copyOf(FUELS);
    }

    static {
        registerFuel(new ItemStack(Items.REDSTONE), 300);
        registerFuel(new ItemStack(Blocks.REDSTONE_BLOCK), 2700);
    }
}
