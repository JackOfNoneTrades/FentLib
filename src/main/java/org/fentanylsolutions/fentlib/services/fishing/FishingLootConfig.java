package org.fentanylsolutions.fentlib.services.fishing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.FishingHooks;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.BiomeUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class FishingLootConfig {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private static final String CONFIG_FILE_NAME = "fishing-loot.json";
    private static final int SCHEMA_VERSION = 2;
    private static final Set<String> warnedMissingItems = Collections.synchronizedSet(new HashSet<String>());
    private static final Set<Integer> warnedMissingEnchantments = Collections.synchronizedSet(new HashSet<Integer>());
    private static volatile FishingLootConfig cachedConfig;
    private static volatile long cachedLastModified = Long.MIN_VALUE;

    public int schemaVersion = SCHEMA_VERSION;
    public List<LootEntry> fish = new ArrayList<>();
    public List<LootEntry> junk = new ArrayList<>();
    public List<LootEntry> treasure = new ArrayList<>();

    public static final class IntRange {

        public int min;
        public int max;

        public static IntRange exact(int value) {
            IntRange range = new IntRange();
            range.min = value;
            range.max = value;
            return range;
        }

        public int randomValue(Random rand) {
            if (max <= min) {
                return min;
            }
            return min + rand.nextInt(max - min + 1);
        }
    }

    public static final class EnchantmentSpec {

        public int id = -1;
        public IntRange level = IntRange.exact(1);

        public boolean isValid() {
            return id >= 0;
        }
    }

    public static final class LootEntry {

        public String item = "";
        public IntRange count = IntRange.exact(1);
        public IntRange meta = IntRange.exact(0);
        public int weight = 1;
        public Float maxDamagePercent;
        public boolean useVanillaEnchantingRules = false;
        public List<EnchantmentSpec> enchantments = new ArrayList<>();
        public List<String> biomes = new ArrayList<>();
        public boolean invertBiomes = false;

        public boolean isValid() {
            return item != null && !item.trim()
                .isEmpty() && weight > 0;
        }

        public boolean appliesToBiome(BiomeGenBase biome) {
            if (biomes == null || biomes.isEmpty()) {
                return true;
            }

            boolean matched = false;
            if (biome != null) {
                for (String selector : biomes) {
                    if (BiomeUtil.matchesBiomeSelector(biome, selector)) {
                        matched = true;
                        break;
                    }
                }
            }

            return invertBiomes ? !matched : matched;
        }

        public ItemStack createItemStack(Random rand, Item resolvedItem) {
            IntRange countRange = sanitizeRange(count, 1);
            IntRange metaRange = sanitizeRange(meta, 0);
            ItemStack stack = new ItemStack(resolvedItem, countRange.randomValue(rand), metaRange.randomValue(rand));

            if (maxDamagePercent != null && maxDamagePercent.floatValue() > 0.0F && stack.getMaxDamage() > 0) {
                float clampedPercent = Math.max(0.0F, Math.min(maxDamagePercent.floatValue(), 1.0F));
                int maxDamage = (int) (clampedPercent * stack.getMaxDamage());

                if (maxDamage > 0) {
                    int randomizedDamage = stack.getMaxDamage() - rand.nextInt(rand.nextInt(maxDamage) + 1);

                    if (randomizedDamage > maxDamage) {
                        randomizedDamage = maxDamage;
                    }

                    if (randomizedDamage < 1) {
                        randomizedDamage = 1;
                    }

                    stack.setItemDamage(randomizedDamage);
                }
            }

            if (useVanillaEnchantingRules) {
                stack = EnchantmentHelper.addRandomEnchantment(rand, stack, 30);
            }

            applyExplicitEnchantments(stack, enchantments, rand);
            return stack;
        }
    }

    private static final class WeightedLootEntry extends WeightedRandom.Item {

        private final LootEntry entry;
        private final Item item;

        private WeightedLootEntry(LootEntry entry, Item item) {
            super(entry.weight);
            this.entry = entry;
            this.item = item;
        }
    }

    private FishingLootConfig sanitized() {
        FishingLootConfig sanitized = new FishingLootConfig();
        sanitized.schemaVersion = SCHEMA_VERSION;
        sanitized.fish = sanitizeEntries(fish);
        sanitized.junk = sanitizeEntries(junk);
        sanitized.treasure = sanitizeEntries(treasure);
        return sanitized;
    }

    public static void ensureExists() {
        get();
    }

    public static FishingLootConfig get() {
        File file = getConfigFile();
        long lastModified = file.isFile() ? file.lastModified() : Long.MIN_VALUE;
        FishingLootConfig config = cachedConfig;
        if (config != null && cachedLastModified == lastModified) {
            return config;
        }
        synchronized (FishingLootConfig.class) {
            lastModified = file.isFile() ? file.lastModified() : Long.MIN_VALUE;
            if (cachedConfig != null && cachedLastModified == lastModified) {
                return cachedConfig;
            }
            FishingLootConfig loaded = load(file);
            cachedConfig = loaded;
            cachedLastModified = file.isFile() ? file.lastModified() : Long.MIN_VALUE;
            return loaded;
        }
    }

    public static ItemStack getRandomLoot(BiomeGenBase biome, FishingHooks.FishableCategory category, Random rand) {
        List<LootEntry> entries = get().getEntriesFor(biome, category);
        if (entries.isEmpty()) {
            return null;
        }

        List<WeightedLootEntry> weightedEntries = new ArrayList<>();
        for (LootEntry entry : entries) {
            Item item = resolveItem(entry.item);
            if (item == null) {
                warnMissingItem(entry.item);
                continue;
            }
            weightedEntries.add(new WeightedLootEntry(entry, item));
        }

        if (weightedEntries.isEmpty()) {
            return null;
        }

        WeightedLootEntry selected = (WeightedLootEntry) WeightedRandom.getRandomItem(rand, weightedEntries);
        if (selected == null) {
            return null;
        }

        return selected.entry.createItemStack(rand, selected.item);
    }

    private List<LootEntry> getEntriesFor(BiomeGenBase biome, FishingHooks.FishableCategory category) {
        List<LootEntry> resolved = new ArrayList<>();
        for (LootEntry entry : getEntries(category)) {
            if (entry.appliesToBiome(biome)) {
                resolved.add(entry);
            }
        }
        return resolved;
    }

    private List<LootEntry> getEntries(FishingHooks.FishableCategory category) {
        switch (category) {
            case JUNK:
                return junk == null ? Collections.<LootEntry>emptyList() : junk;
            case TREASURE:
                return treasure == null ? Collections.<LootEntry>emptyList() : treasure;
            case FISH:
            default:
                return fish == null ? Collections.<LootEntry>emptyList() : fish;
        }
    }

    private static FishingLootConfig load(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            FentLib.LOG.warn("Could not create fishing loot config directory at {}", parent);
        }
        if (!file.isFile()) {
            FishingLootConfig defaults = defaultConfig();
            save(file, defaults);
            return defaults.sanitized();
        }
        try (FileReader reader = new FileReader(file)) {
            FishingLootConfig parsed = GSON.fromJson(reader, FishingLootConfig.class);
            if (parsed == null) {
                FishingLootConfig defaults = defaultConfig();
                save(file, defaults);
                return defaults.sanitized();
            }
            return parsed.sanitized();
        } catch (Exception e) {
            FentLib.LOG.error("Failed to read {}", file, e);
            return defaultConfig().sanitized();
        }
    }

    private static void save(File file, FishingLootConfig config) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            FentLib.LOG.error("Failed to write {}", file, e);
        }
    }

    private static File getConfigFile() {
        return new File(FentLib.getConfigDir(), CONFIG_FILE_NAME);
    }

    private static List<LootEntry> sanitizeEntries(List<LootEntry> source) {
        List<LootEntry> sanitized = new ArrayList<>();
        for (LootEntry entry : source == null ? Collections.<LootEntry>emptyList() : source) {
            if (entry == null || !entry.isValid()) {
                continue;
            }

            LootEntry sanitizedEntry = new LootEntry();
            sanitizedEntry.item = entry.item.trim();
            sanitizedEntry.count = sanitizeRange(entry.count, 1);
            sanitizedEntry.meta = sanitizeRange(entry.meta, 0);
            sanitizedEntry.weight = Math.max(1, entry.weight);
            sanitizedEntry.maxDamagePercent = entry.maxDamagePercent;
            sanitizedEntry.useVanillaEnchantingRules = entry.useVanillaEnchantingRules;
            sanitizedEntry.enchantments = sanitizeEnchantmentSpecs(entry.enchantments);
            sanitizedEntry.biomes = sanitizeBiomeSelectors(entry.biomes);
            sanitizedEntry.invertBiomes = entry.invertBiomes;
            sanitized.add(sanitizedEntry);
        }
        return sanitized;
    }

    private static List<EnchantmentSpec> sanitizeEnchantmentSpecs(List<EnchantmentSpec> source) {
        List<EnchantmentSpec> sanitized = new ArrayList<>();
        for (EnchantmentSpec spec : source == null ? Collections.<EnchantmentSpec>emptyList() : source) {
            if (spec == null || !spec.isValid()) {
                continue;
            }
            EnchantmentSpec sanitizedSpec = new EnchantmentSpec();
            sanitizedSpec.id = spec.id;
            sanitizedSpec.level = sanitizeRange(spec.level, 1);
            sanitized.add(sanitizedSpec);
        }
        return sanitized;
    }

    private static List<String> sanitizeBiomeSelectors(List<String> selectors) {
        List<String> sanitized = new ArrayList<>();
        for (String selector : selectors == null ? Collections.<String>emptyList() : selectors) {
            if (selector == null) {
                continue;
            }
            String trimmed = selector.trim();
            if (!trimmed.isEmpty()) {
                sanitized.add(trimmed);
            }
        }
        return sanitized;
    }

    private static IntRange sanitizeRange(IntRange range, int floor) {
        IntRange sanitized = new IntRange();
        if (range == null) {
            sanitized.min = floor;
            sanitized.max = floor;
            return sanitized;
        }
        sanitized.min = Math.max(floor, range.min);
        sanitized.max = Math.max(sanitized.min, Math.max(floor, range.max));
        return sanitized;
    }

    private static FishingLootConfig defaultConfig() {
        FishingLootConfig config = new FishingLootConfig();
        config.schemaVersion = SCHEMA_VERSION;

        config.junk.add(entry("minecraft:leather_boots", 1, 0, 10, 0.9F, false));
        config.junk.add(entry("minecraft:leather", 1, 0, 10, null, false));
        config.junk.add(entry("minecraft:bone", 1, 0, 10, null, false));
        config.junk.add(entry("minecraft:potionitem", 1, 0, 10, null, false));
        config.junk.add(entry("minecraft:string", 1, 0, 5, null, false));
        config.junk.add(entry("minecraft:fishing_rod", 1, 0, 2, 0.9F, false));
        config.junk.add(entry("minecraft:bowl", 1, 0, 10, null, false));
        config.junk.add(entry("minecraft:stick", 1, 0, 5, null, false));
        config.junk.add(entry("minecraft:dye", 10, 0, 1, null, false));
        config.junk.add(entry("minecraft:tripwire_hook", 1, 0, 10, null, false));
        config.junk.add(entry("minecraft:rotten_flesh", 1, 0, 10, null, false));

        config.treasure.add(entry("minecraft:waterlily", 1, 0, 1, null, false));
        config.treasure.add(entry("minecraft:name_tag", 1, 0, 1, null, false));
        config.treasure.add(entry("minecraft:saddle", 1, 0, 1, null, false));
        config.treasure.add(entry("minecraft:bow", 1, 0, 1, 0.25F, true));
        config.treasure.add(entry("minecraft:fishing_rod", 1, 0, 1, 0.25F, true));
        config.treasure.add(entry("minecraft:book", 1, 0, 1, null, true));

        config.fish.add(entry("minecraft:fish", 1, 0, 60, null, false));
        config.fish.add(entry("minecraft:fish", 1, 1, 25, null, false));
        config.fish.add(entry("minecraft:fish", 1, 2, 2, null, false));
        config.fish.add(entry("minecraft:fish", 1, 3, 13, null, false));

        return config;
    }

    private static LootEntry entry(String item, int count, int meta, int weight, Float maxDamagePercent,
        boolean useVanillaEnchantingRules) {
        LootEntry entry = new LootEntry();
        entry.item = item;
        entry.count = IntRange.exact(count);
        entry.meta = IntRange.exact(meta);
        entry.weight = weight;
        entry.maxDamagePercent = maxDamagePercent;
        entry.useVanillaEnchantingRules = useVanillaEnchantingRules;
        return entry;
    }

    private static Item resolveItem(String registryName) {
        if (registryName == null) {
            return null;
        }
        Item item = getItemByRegistryName(registryName);
        if (item != null) {
            return item;
        }

        Block block = getBlockByRegistryName(registryName);
        if (block != null) {
            return Item.getItemFromBlock(block);
        }

        return null;
    }

    private static void applyExplicitEnchantments(ItemStack stack, List<EnchantmentSpec> enchantments, Random rand) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }

        if (stack.getItem() == Items.book) {
            stack.func_150996_a(Items.enchanted_book);
        }

        Map<Integer, Integer> appliedEnchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));

        for (EnchantmentSpec spec : enchantments) {
            Enchantment enchantment = resolveEnchantment(spec.id);
            if (enchantment == null) {
                warnMissingEnchantment(spec.id);
                continue;
            }

            IntRange levelRange = sanitizeRange(spec.level, 1);
            appliedEnchantments
                .put(Integer.valueOf(enchantment.effectId), Integer.valueOf(levelRange.randomValue(rand)));
        }

        if (!appliedEnchantments.isEmpty()) {
            EnchantmentHelper.setEnchantments(appliedEnchantments, stack);
        }
    }

    private static Enchantment resolveEnchantment(int id) {
        if (id < 0 || id >= Enchantment.enchantmentsList.length) {
            return null;
        }
        return Enchantment.enchantmentsList[id];
    }

    private static void warnMissingItem(String registryName) {
        if (registryName == null || !warnedMissingItems.add(registryName)) {
            return;
        }
        FentLib.LOG.warn("Skipping unknown fishing loot item '{}'", registryName);
    }

    private static void warnMissingEnchantment(int enchantmentId) {
        if (!warnedMissingEnchantments.add(Integer.valueOf(enchantmentId))) {
            return;
        }
        FentLib.LOG.warn("Skipping unknown fishing loot enchantment id '{}'", enchantmentId);
    }

    private static Item getItemByRegistryName(String registryName) {
        Item item = (Item) Item.itemRegistry.getObject(registryName);
        if (item != null) {
            return item;
        }

        int separator = registryName.indexOf(':');
        if (separator >= 0 && separator < registryName.length() - 1) {
            return (Item) Item.itemRegistry.getObject(registryName.substring(separator + 1));
        }

        return null;
    }

    private static Block getBlockByRegistryName(String registryName) {
        Block block = (Block) Block.blockRegistry.getObject(registryName);
        if (block != null) {
            return block;
        }

        int separator = registryName.indexOf(':');
        if (separator >= 0 && separator < registryName.length() - 1) {
            return (Block) Block.blockRegistry.getObject(registryName.substring(separator + 1));
        }

        return null;
    }
}
