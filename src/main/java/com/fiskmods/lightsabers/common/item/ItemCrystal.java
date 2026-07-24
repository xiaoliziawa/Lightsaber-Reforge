package com.fiskmods.lightsabers.common.item;

import com.fiskmods.lightsabers.common.block.ModBlocks;
import com.fiskmods.lightsabers.common.lightsaber.CrystalColor;
import com.fiskmods.lightsabers.helper.ItemDataHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemCrystal extends Item implements ILightsaberComponent {
    private static final String CRYSTAL_ID_TAG = "CrystalColorId";
    private static final String LEGACY_CRYSTAL_ID_TAG = "color";
    private static final int[] GENERATION_WEIGHTS = {90, 30, 10, 1};

    public static final Map<CrystalColor, Rarity> rarityMap = new EnumMap<>(CrystalColor.class);
    private static final Map<CrystalColor, Integer> generationWeightMap =
            new EnumMap<>(CrystalColor.class);
    private static int generationTotalWeight;

    public ItemCrystal() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public long getFingerprint(ItemStack stack, int slot) {
        return (long) (getId(stack) & 0xFF) << 24;
    }

    @Override
    public boolean isCompatibleSlot(ItemStack stack, int slot) {
        return slot == 5;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(get(stack).getUnlocalizedName())
                .withStyle(ChatFormatting.GRAY));
    }

    public static boolean isCrystal(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemCrystal;
    }

    public static int getId(ItemStack stack) {
        CompoundTag tag = ItemDataHelper.getCustomData(stack);
        if (tag != null) {
            if (tag.contains(CRYSTAL_ID_TAG)) {
                return normalizeId(tag.getInt(CRYSTAL_ID_TAG));
            }
            if (tag.contains(LEGACY_CRYSTAL_ID_TAG)) {
                int id = normalizeId(tag.getInt(LEGACY_CRYSTAL_ID_TAG));
                tag.remove(LEGACY_CRYSTAL_ID_TAG);
                tag.putInt(CRYSTAL_ID_TAG, id);
                ItemDataHelper.setCustomData(stack, tag);
                return id;
            }
        }
        return normalizeId(stack.getDamageValue());
    }

    public static CrystalColor get(ItemStack stack) {
        return CrystalColor.get(getId(stack));
    }

    public static CrystalColor getRandomGen(Random random) {
        int selectedWeight = random.nextInt(generationTotalWeight);
        for (CrystalColor color : CrystalColor.values()) {
            if (color == CrystalColor.RGB) {
                continue;
            }
            selectedWeight -= generationWeightMap.get(color);
            if (selectedWeight < 0) {
                return color;
            }
        }
        return CrystalColor.DEEP_BLUE;
    }

    public static ItemStack create(CrystalColor color, Item item) {
        ItemStack stack = new ItemStack(item);
        ItemDataHelper.updateCustomData(stack, tag -> tag.putInt(CRYSTAL_ID_TAG, color.id));
        return stack;
    }

    public static ItemStack create(CrystalColor color) {
        return create(color, ModBlocks.LIGHTSABER_CRYSTAL_ITEM.get());
    }

    public static ItemStack createBlock(CrystalColor color) {
        return create(color, ModBlocks.LIGHTSABER_CRYSTAL_BLOCK_ITEM.get());
    }

    private static int normalizeId(int id) {
        return Math.floorMod(id, CrystalColor.values().length);
    }

    private static void registerRarity(CrystalColor color, Rarity rarity) {
        rarityMap.put(color, rarity);
        int generationWeight = GENERATION_WEIGHTS[rarity.ordinal()];
        generationWeightMap.put(color, generationWeight);
        generationTotalWeight += generationWeight;
    }

    static {
        registerRarity(CrystalColor.DEEP_BLUE, Rarity.COMMON);
        registerRarity(CrystalColor.MEDIUM_BLUE, Rarity.COMMON);
        registerRarity(CrystalColor.LIGHT_BLUE, Rarity.COMMON);
        registerRarity(CrystalColor.AMBER, Rarity.COMMON);
        registerRarity(CrystalColor.YELLOW, Rarity.COMMON);
        registerRarity(CrystalColor.GOLD, Rarity.COMMON);
        registerRarity(CrystalColor.LIME_GREEN, Rarity.COMMON);
        registerRarity(CrystalColor.GREEN, Rarity.COMMON);
        registerRarity(CrystalColor.MINT_GREEN, Rarity.COMMON);

        registerRarity(CrystalColor.MAGENTA, Rarity.UNCOMMON);
        registerRarity(CrystalColor.PINK, Rarity.UNCOMMON);
        registerRarity(CrystalColor.RED, Rarity.UNCOMMON);
        registerRarity(CrystalColor.BLOOD_ORANGE, Rarity.UNCOMMON);

        registerRarity(CrystalColor.INDIGO, Rarity.RARE);
        registerRarity(CrystalColor.PURPLE, Rarity.RARE);
        registerRarity(CrystalColor.CYAN, Rarity.RARE);

        registerRarity(CrystalColor.ARCTIC_BLUE, Rarity.EPIC);
        registerRarity(CrystalColor.WHITE, Rarity.EPIC);
        rarityMap.put(CrystalColor.RGB, Rarity.EPIC);
    }
}
