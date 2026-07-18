package com.fiskmods.lightsabers.common.container;

import com.fiskmods.lightsabers.common.tileentity.TileEntityHolocron;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerHolocron extends AbstractContainerMenu {
    public final TileEntityHolocron holocron;

    public ContainerHolocron(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        this(containerId, playerInventory, getHolocron(playerInventory, buffer));
    }

    public ContainerHolocron(
            int containerId,
            Inventory playerInventory,
            TileEntityHolocron holocron
    ) {
        super(ModMenus.HOLOCRON.get(), containerId);
        this.holocron = holocron;
        if (holocron.getLevel() != null && !holocron.getLevel().isClientSide) {
            holocron.startUsing();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(holocron, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (holocron.getLevel() != null && !holocron.getLevel().isClientSide) {
            holocron.stopUsing();
        }
    }

    private static TileEntityHolocron getHolocron(
            Inventory playerInventory,
            FriendlyByteBuf buffer
    ) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(
                buffer.readBlockPos()
        );
        if (blockEntity instanceof TileEntityHolocron holocron) {
            return holocron;
        }
        throw new IllegalStateException("Missing holocron block entity");
    }
}
