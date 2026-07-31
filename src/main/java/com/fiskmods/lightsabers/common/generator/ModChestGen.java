package com.fiskmods.lightsabers.common.generator;

import com.fiskmods.lightsabers.common.hilt.Hilt;
import com.fiskmods.lightsabers.common.item.ItemCrystal;
import com.fiskmods.lightsabers.common.item.ItemFocusingCrystal;
import com.fiskmods.lightsabers.common.item.ItemLightsaberPart;
import com.fiskmods.lightsabers.common.item.ModItems;
import com.fiskmods.lightsabers.common.lightsaber.LightsaberData;
import com.fiskmods.lightsabers.common.lightsaber.PartType;
import net.minecraft.world.Container;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ModChestGen {
    public static final int RANDOM_LOOT_VARIANT = -1;
    public static final String SITH_TOMB_ANNEX = "sithTombAnnex";
    public static final String SITH_TOMB_TREASURY = "sithTombTreasury";
    public static final String SITH_TOMB_COFFIN = "sithTombCoffin";
    public static final String JEDI_TEMPLE = "jediTemple";

    private static final String CHEST_GEN_CATEGORY_TAG = "ChestGenCategory";
    private static final String SITH_TOMB_LOOT_TAG = "SithTombLoot";
    private static final String JEDI_TEMPLE_LOOT_TAG = "JediTempleLoot";

    private static final LootTable ANNEX = new LootTable(3, 7, List.of(
            entry(Items.BONE, 4, 6, 4),
            entry(Items.ROTTEN_FLESH, 3, 7, 3),
            entry(Items.IRON_INGOT, 2, 7, 3),
            entry(() -> ModItems.circuitry, 1, 1, 2),
            randomPart(PartType.EMITTER, 3),
            crystal(SITH_TOMB_ANNEX, 1)
    ));
    private static final LootTable TREASURY = new LootTable(5, 7, List.of(
            entry(Items.BONE, 4, 6, 14),
            entry(Items.ROTTEN_FLESH, 3, 7, 12),
            entry(Items.GOLD_INGOT, 2, 7, 12),
            entry(Items.DIAMOND, 1, 2, 5),
            enchantedBook(5),
            randomPart(PartType.EMITTER, 6),
            randomFocusingCrystal(4),
            crystal(SITH_TOMB_TREASURY, 12)
    ));
    private static final LootTable COFFIN = new LootTable(6, 14, List.of(
            entry(Items.BONE, 4, 6, 14),
            entry(Items.ROTTEN_FLESH, 3, 7, 12),
            entry(Items.GOLD_INGOT, 2, 7, 12),
            entry(Items.DIAMOND, 1, 2, 5),
            enchantedBook(5),
            entry(() -> ModItems.circuitry, 1, 1, 3),
            randomPart(PartType.EMITTER, 3),
            randomFocusingCrystal(4),
            crystal(SITH_TOMB_COFFIN, 3),
            tagged(random -> LightsaberData.createRandom(random), 1, 1, 6, stack ->
                    stack.getOrCreateTag().putBoolean(SITH_TOMB_LOOT_TAG, true))
    ));
    private static final LootTable TEMPLE = new LootTable(4, 8, List.of(
            entry(Items.COOKED_CHICKEN, 1, 4, 4),
            entry(Items.BAKED_POTATO, 2, 6, 5),
            entry(Items.NAME_TAG, 1, 2, 2),
            entry(Items.LEATHER, 1, 8, 4),
            entry(Items.WHITE_WOOL, 1, 9, 10),
            entry(() -> ModItems.circuitry, 1, 2, 3),
            randomPart(PartType.EMITTER, 9),
            tagged(random -> LightsaberData.createRandom(random), 1, 1, 1, stack ->
                    stack.getOrCreateTag().putBoolean(JEDI_TEMPLE_LOOT_TAG, true))
    ));

    private ModChestGen() {
    }

    public static LootTable get(String category) {
        return switch (category) {
            case SITH_TOMB_ANNEX -> ANNEX;
            case SITH_TOMB_TREASURY -> TREASURY;
            case SITH_TOMB_COFFIN -> COFFIN;
            case JEDI_TEMPLE -> TEMPLE;
            default -> throw new IllegalArgumentException("Unknown structure loot category: " + category);
        };
    }

    public static boolean isSithTombCategory(String category) {
        return switch (category) {
            case SITH_TOMB_ANNEX, SITH_TOMB_TREASURY, SITH_TOMB_COFFIN -> true;
            default -> false;
        };
    }

    public static void fill(Container container, String category, Random random) {
        fill(container, category, random, RANDOM_LOOT_VARIANT);
    }

    public static void fill(
            Container container,
            String category,
            Random random,
            int lootVariant
    ) {
        LootTable table = get(category);
        List<LootEntry> availableEntries = new ArrayList<>(table.entries());
        int rolls = Math.min(
                getRollCount(table, random, lootVariant),
                availableEntries.size()
        );
        int totalWeight = availableEntries.stream().mapToInt(LootEntry::weight).sum();
        for (int roll = 0; roll < rolls; roll++) {
            LootEntry selected = select(availableEntries, totalWeight, random);
            availableEntries.remove(selected);
            totalWeight -= selected.weight();
            ItemStack stack = selected.create(random);
            insertRandomly(container, stack, random);
        }
    }

    private static int getRollCount(LootTable table, Random random, int lootVariant) {
        if (lootVariant == RANDOM_LOOT_VARIANT) {
            return nextIntInclusive(random, table.minRolls(), table.maxRolls());
        }
        int range = table.maxRolls() - table.minRolls() + 1;
        return table.minRolls() + Math.floorMod(lootVariant, range);
    }

    private static LootEntry select(List<LootEntry> entries, int totalWeight, Random random) {
        int value = random.nextInt(totalWeight);
        for (LootEntry entry : entries) {
            value -= entry.weight();
            if (value < 0) {
                return entry;
            }
        }
        return entries.get(entries.size() - 1);
    }

    private static void insertRandomly(Container container, ItemStack stack, Random random) {
        int start = random.nextInt(container.getContainerSize());
        for (int offset = 0; offset < container.getContainerSize(); offset++) {
            int slot = (start + offset) % container.getContainerSize();
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)) {
                int maxStackSize = Math.min(
                        existing.getMaxStackSize(),
                        container.getMaxStackSize()
                );
                int transfer = Math.min(stack.getCount(), maxStackSize - existing.getCount());
                if (transfer > 0) {
                    existing.grow(transfer);
                    stack.shrink(transfer);
                    if (stack.isEmpty()) {
                        return;
                    }
                }
            }
        }
        for (int offset = 0; offset < container.getContainerSize(); offset++) {
            int slot = (start + offset) % container.getContainerSize();
            if (container.getItem(slot).isEmpty()) {
                container.setItem(slot, stack);
                return;
            }
        }
    }

    private static LootEntry entry(Item item, int min, int max, int weight) {
        return new LootEntry(
                random -> new ItemStack(item),
                min,
                max,
                weight,
                (stack, random) -> { }
        );
    }

    private static LootEntry entry(ItemSupplier item, int min, int max, int weight) {
        return new LootEntry(
                random -> new ItemStack(item.get()),
                min,
                max,
                weight,
                (stack, random) -> { }
        );
    }

    private static LootEntry stackEntry(
            StackSupplier item,
            int min,
            int max,
            int weight
    ) {
        return new LootEntry(item, min, max, weight, (stack, random) -> { });
    }

    private static LootEntry tagged(
            StackSupplier item,
            int min,
            int max,
            int weight,
            Consumer<ItemStack> tagger
    ) {
        return new LootEntry(
                item,
                min,
                max,
                weight,
                (stack, random) -> tagger.accept(stack)
        );
    }

    private static LootEntry enchantedBook(int weight) {
        return new LootEntry(
                random -> new ItemStack(Items.ENCHANTED_BOOK),
                1,
                1,
                weight,
                ModChestGen::addRandomBookEnchantment
        );
    }

    private static void addRandomBookEnchantment(ItemStack stack, Random random) {
        List<Enchantment> enchantments = DiscoverableEnchantments.VALUES;
        if (enchantments.isEmpty()) {
            return;
        }

        Enchantment enchantment = enchantments.get(random.nextInt(enchantments.size()));
        int level = nextIntInclusive(
                random,
                enchantment.getMinLevel(),
                enchantment.getMaxLevel()
        );
        EnchantedBookItem.addEnchantment(
                stack,
                new EnchantmentInstance(enchantment, level)
        );
    }

    private static LootEntry crystal(String category, int weight) {
        return tagged(
                random -> ItemCrystal.create(ItemCrystal.getRandomGen(random)),
                1,
                1,
                weight,
                stack -> stack.getOrCreateTag().putString(CHEST_GEN_CATEGORY_TAG, category)
        );
    }

    private static LootEntry randomPart(PartType type, int weight) {
        return stackEntry(
                random -> ItemLightsaberPart.create(type, Hilt.REGISTRY.getRandom(random)),
                1,
                1,
                weight
        );
    }

    private static LootEntry randomFocusingCrystal(int weight) {
        return stackEntry(
                random -> ItemFocusingCrystal.create(ItemFocusingCrystal.getRandom(random)),
                1,
                1,
                weight
        );
    }

    private static int nextIntInclusive(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    public record LootTable(int minRolls, int maxRolls, List<LootEntry> entries) {
    }

    public record LootEntry(
            StackSupplier stackSupplier,
            int minCount,
            int maxCount,
            int weight,
            BiConsumer<ItemStack, Random> modifier
    ) {
        ItemStack create(Random random) {
            int count = nextIntInclusive(random, minCount, maxCount);
            ItemStack stack = stackSupplier.get(random);
            stack.setCount(count);
            modifier.accept(stack, random);
            return stack;
        }
    }

    private static final class DiscoverableEnchantments {
        private static final List<Enchantment> VALUES = ForgeRegistries.ENCHANTMENTS
                .getValues()
                .stream()
                .filter(Enchantment::isDiscoverable)
                .toList();

        private DiscoverableEnchantments() {
        }
    }

    @FunctionalInterface
    public interface ItemSupplier {
        Item get();
    }

    @FunctionalInterface
    public interface StackSupplier {
        ItemStack get(Random random);
    }
}
