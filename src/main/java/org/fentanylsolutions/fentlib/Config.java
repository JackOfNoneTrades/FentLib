package org.fentanylsolutions.fentlib;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {

    private static Configuration config;

    public static class Categories {

        public static final String debug = "debug";
        public static final String general = "general";
        public static final String miscTweaks = "misc-tweaks";
    }

    public static boolean debugMode;
    public static boolean printPotions = false;
    public static boolean printMobs = false;
    public static boolean printDimensions = false;
    public static boolean printBiomes = false;

    public static String[] passiveMobsWhichCanInflictDamage = {};
    public static int maxGifFrameCount = 1000;
    public static int gifSizeCap = 2;

    public static boolean useNativeGifReader = true;
    public static boolean enableFishingLootTable = false;

    public static boolean disableEnderCoreInfoButton = true;

    public static String publicBaseUrl = "";

    public static void loadConfig(File configFile) {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            FentLib.LOG.warn("Failed to create config directory {}", parent);
        }
        config = new Configuration(configFile);

        try {
            FentLib.debug("Loading config");
            config.load();

            // Debug
            debugMode = config.getBoolean("debugMode", Categories.debug, debugMode, "Enable debug mode.");

            Property printPotionsProperty = config.get(
                Categories.debug,
                "printPotions",
                printPotions,
                "If set to true, print a list of potions in the logs on game post init.");
            printPotions = printPotionsProperty.getBoolean();

            Property printMobsProperty = config.get(
                Categories.debug,
                "printMobs",
                printMobs,
                "If set to true, print a list of mob names on game post init.");
            printMobs = printMobsProperty.getBoolean();

            Property printDimensionsProperty = config.get(
                Categories.debug,
                "printDimensions",
                printDimensions,
                "If set to true, print a list of dimension names on game post init.");
            printDimensions = printDimensionsProperty.getBoolean();

            Property printBiomesProperty = config.get(
                Categories.debug,
                "printBiomes",
                printBiomes,
                "If set to true, print a list of biome names on game post init.");
            printBiomes = printBiomesProperty.getBoolean();

            // General
            Property passiveMobsWhichCanInflictDamageProperty = config.get(
                Categories.general,
                "passiveMobsWhichCanInflictDamage",
                passiveMobsWhichCanInflictDamage,
                "List of passive mobs that should be able to inflict damage.");
            passiveMobsWhichCanInflictDamage = passiveMobsWhichCanInflictDamageProperty.getStringList();

            maxGifFrameCount = config.getInt(
                "maxGifFrameCount",
                Categories.general,
                maxGifFrameCount,
                1,
                9999,
                "Maximum frames a gif is allowed to have.");
            gifSizeCap = config.getInt(
                "gifSizeCap",
                Categories.general,
                gifSizeCap,
                1,
                10,
                "Gif size cap in megabytes after being stitched into a png spritesheet.");

            useNativeGifReader = config.getBoolean(
                "useNativeGifReader",
                Categories.general,
                useNativeGifReader,
                "Use the pure Java GIF reader instead of scrimage. Disable this to fall back to the scrimage-based reader.");

            enableFishingLootTable = config.getBoolean(
                "enableFishingLootTable",
                Categories.general,
                enableFishingLootTable,
                "Enable fishing loot table override.");

            // HTTP
            publicBaseUrl = normalizePublicBaseUrl(
                config.getString(
                    "publicBaseUrl",
                    Categories.general,
                    publicBaseUrl,
                    "Public base URL of this server (e.g. myserver.example.com:25565 or https://myserver.example.com). This is the public base only; mods should append their own relative route paths."));

            // Misc tweaks
            disableEnderCoreInfoButton = config.getBoolean(
                "disableEnderCoreInfoButton",
                Categories.miscTweaks,
                disableEnderCoreInfoButton,
                "Disable the EnderCore information button in the modlist screen.");

            FentLib.varInstanceCommon.buildPassiveMobList();
        } catch (Exception e) {
            FentLib.LOG.error("Error loading config: ", e);
        } finally {
            config.save();
        }
    }

    public static Configuration getRawConfig() {
        return config;
    }

    /**
     * Normalizes a public-facing base URL used by sibling mods.
     * Missing schemes default to plain HTTP because this is commonly the
     * Minecraft server port rather than a TLS terminator.
     */
    public static String normalizePublicBaseUrl(String rawBaseUrl) {
        String value = rawBaseUrl == null ? "" : rawBaseUrl.trim();
        while (value.endsWith("/") && !value.isEmpty()) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) {
            return "";
        }
        if (!looksLikeAbsoluteUrl(value)) {
            value = "http://" + value;
        }
        return value;
    }

    /**
     * Builds a public-facing URL by appending a relative route path to the
     * configured public base URL.
     *
     * @return the absolute public URL, or null if publicBaseUrl is unset
     */
    public static String buildPublicUrl(String relativePath) {
        String base = normalizePublicBaseUrl(publicBaseUrl);
        if (base.isEmpty()) {
            return null;
        }

        String normalizedPath = normalizeRelativePath(relativePath);
        return normalizedPath.isEmpty() ? base : base + "/" + normalizedPath;
    }

    private static String normalizeRelativePath(String rawPath) {
        String path = rawPath == null ? "" : rawPath.trim();
        while (path.startsWith("./")) {
            path = path.substring(2);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith("/") && !path.isEmpty()) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean looksLikeAbsoluteUrl(String value) {
        return value != null && value.contains("://");
    }
}
