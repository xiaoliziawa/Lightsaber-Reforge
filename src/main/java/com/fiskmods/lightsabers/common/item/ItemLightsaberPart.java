package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Random;
import java.util.function.Consumer;

public class ItemLightsaberPart extends Item implements ILightsaberComponent {
    public final PartType partType;

    public ItemLightsaberPart(Item.Properties properties, PartType partType) {
        super(properties.stacksTo(16));
        this.partType = partType;
    }

    @Override
    public long getFingerprint(ItemStack stack, int slot) {
        return (long) Hilt.REGISTRY.getIDForObject(get(stack)) << partType.ordinal() * 6;
    }

    @Override
    public boolean isCompatibleSlot(ItemStack stack, int slot) {
        return slot == partType.ordinal();
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.accept(Component.literal(get(stack).getLocalizedName()));
    }

    public static Hilt get(ItemStack stack) {
        int id = 0;
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag != null) {
            if (tag.getString("lightsaber").isPresent()) {
                Hilt legacyHilt = Hilt.REGISTRY.getObject(
                        Hilt.LEGACY_MAPPINGS.get(tag.getStringOr("lightsaber", ""))
                );
                id = Hilt.REGISTRY.getIDForObject(legacyHilt);
                tag.remove("lightsaber");
                tag.remove("type");
                tag.putByte(ALConstants.TAG_PART, (byte) id);
                ItemDataHelper.setCustomData(stack, tag);
            } else if (tag.getInt(ALConstants.TAG_PART).isPresent()) {
                id = tag.getByteOr(ALConstants.TAG_PART, (byte) 0);
            }
        }
        return Hilt.REGISTRY.getObjectById(id);
    }

    public static Item getItem(PartType type) {
        return switch (type) {
            case EMITTER -> ModItems.EMITTER.get();
            case SWITCH_SECTION -> ModItems.SWITCH_SECTION.get();
            case BODY -> ModItems.GRIP.get();
            case POMMEL -> ModItems.POMMEL.get();
        };
    }

    public static ItemStack create(PartType type, Hilt hilt) {
        ItemStack stack = new ItemStack(getItem(type));
        ItemDataHelper.updateCustomData(
                stack,
                tag -> tag.putByte(
                        ALConstants.TAG_PART,
                        (byte) Hilt.REGISTRY.getIDForObject(hilt)
                )
        );
        return stack;
    }

    public static PartType getType(ItemStack stack) {
        return stack.getItem() instanceof ItemLightsaberPart part ? part.partType : null;
    }

    public static PartType getRandomType(Random random) {
        return PartType.values()[random.nextInt(PartType.values().length)];
    }

    public static PartType getRandomType() {
        return getRandomType(new Random());
    }
}
