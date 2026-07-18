package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.client.render.item.HolocronClientItemExtensions;
import com.fiskmods.lightsabers.common.block.BlockHolocron;
import com.fiskmods.lightsabers.common.block.HolocronType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class ItemHolocron extends BlockItem {
    private final HolocronType type;

    public ItemHolocron(BlockHolocron block) {
        super(block, new Item.Properties().stacksTo(1));
        type = block.getType();
    }

    public HolocronType getType() {
        return type;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(HolocronClientItemExtensions.INSTANCE);
    }
}
