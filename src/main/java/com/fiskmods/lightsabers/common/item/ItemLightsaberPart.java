package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.ALConstants;
import com.fiskmods.lightsabers.client.render.item.LightsaberClientItemExtensions;
import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class ItemLightsaberPart extends Item implements ILightsaberComponent {
    public final PartType partType;

    public ItemLightsaberPart(PartType partType) {
        super(new Item.Properties().stacksTo(16));
        this.partType = partType;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(LightsaberClientItemExtensions.INSTANCE);
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
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal(get(stack).getLocalizedName()));
    }

    public static Hilt get(ItemStack stack) {
        int id = 0;
        if (stack.hasTag()) {
            if (stack.getTag().contains("lightsaber", Tag.TAG_STRING)) {
                Hilt legacyHilt = Hilt.REGISTRY.getObject(
                        Hilt.LEGACY_MAPPINGS.get(stack.getTag().getString("lightsaber"))
                );
                id = Hilt.REGISTRY.getIDForObject(legacyHilt);
                stack.getTag().remove("lightsaber");
                stack.getTag().remove("type");
                stack.getTag().putByte(ALConstants.TAG_PART, (byte) id);
            } else if (stack.getTag().contains(ALConstants.TAG_PART, Tag.TAG_ANY_NUMERIC)) {
                id = stack.getTag().getByte(ALConstants.TAG_PART);
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
        stack.getOrCreateTag().putByte(
                ALConstants.TAG_PART,
                (byte) Hilt.REGISTRY.getIDForObject(hilt)
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
