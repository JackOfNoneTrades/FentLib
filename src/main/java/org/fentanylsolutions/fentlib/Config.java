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

    public static final int DEFAULT_SODIUM_GUI_ACCENT_COLOR = 0xFF94E4D3;
    public static final String DEFAULT_SODIUM_GUI_ACCENT_COLOR_VALUE = "#94E4D3";

    public static boolean debugMode;
    public static boolean logOpenedGuis = false;
    public static boolean printPotions = false;
    public static boolean printMobs = false;
    public static boolean printDimensions = false;
    public static boolean printBiomes = false;

    public static String[] passiveMobsWhichCanInflictDamage = {};
    public static int maxGifFrameCount = 1000;
    public static int gifSizeCap = 2;

    public static boolean enableFishingLootTable = false;

    public static boolean disableEnderCoreInfoButton = true;
    public static boolean milkyPanorama = true;

    public static String publicBaseUrl = "";
    public static String serverIconDirectory = "";

    public static int sodiumGuiAccentColor = DEFAULT_SODIUM_GUI_ACCENT_COLOR;

    public static void loadConfig(File configFile) {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            FentLib.LOG.warn("Failed to create config directory {}", parent);
        }
        config = new Configuration(configFile);

        try {
            FentLib.debug("Loading config");
            config.load();
            config.getCategory(Categories.general)
                .remove("useNativeGifReader");

            // Debug
            debugMode = config.getBoolean("debugMode", Categories.debug, debugMode, "Enable debug mode.");
            logOpenedGuis = config.getBoolean(
                "logOpenedGuis",
                Categories.debug,
                logOpenedGuis,
                "If set to true, log every GUI opened through Minecraft.displayGuiScreen.");

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

            enableFishingLootTable = config.getBoolean(
                "enableFishingLootTable",
                Categories.general,
                enableFishingLootTable,
                "Enable fishing loot table override.");

            serverIconDirectory = normalizeOptionalPath(
                config.getString(
                    "serverIconDirectory",
                    Categories.general,
                    serverIconDirectory,
                    "Directory to search for server icons. Leave empty to use the default server root. "
                        + "Relative paths are resolved from the server root; absolute paths are used directly."));

            // HTTP
            publicBaseUrl = normalizePublicBaseUrl(
                config.getString(
                    "publicBaseUrl",
                    Categories.general,
                    publicBaseUrl,
                    "Public base URL of this server (e.g. myserver.example.com:25565 or https://myserver.example.com). This is the public base only; mods should append their own relative route paths."));

            Property sodiumGuiAccentColorProperty = config.get(
                Categories.general,
                "sodiumGuiAccentColor",
                DEFAULT_SODIUM_GUI_ACCENT_COLOR_VALUE,
                "Default sodiumgui accent color used when callers do not provide their own. "
                    + "Accepts #RRGGBB, #AARRGGBB, 0xRRGGBB, or 0xAARRGGBB.");
            sodiumGuiAccentColor = parseColorProperty(
                sodiumGuiAccentColorProperty,
                DEFAULT_SODIUM_GUI_ACCENT_COLOR,
                DEFAULT_SODIUM_GUI_ACCENT_COLOR_VALUE);

            // Misc tweaks
            disableEnderCoreInfoButton = config.getBoolean(
                "disableEnderCoreInfoButton",
                Categories.miscTweaks,
                disableEnderCoreInfoButton,
                "Disable the EnderCore information button in the modlist screen.");

            milkyPanorama = config.getBoolean(
                "milkyPanorama",
                Categories.miscTweaks,
                milkyPanorama,
                "Apply the vanilla title screen's milky gradient to panorama backgrounds.");

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

    private static String normalizeOptionalPath(String rawPath) {
        return rawPath == null ? "" : rawPath.trim();
    }

    private static int parseColorProperty(Property property, int fallbackColor, String fallbackValue) {
        String value = property.getString();
        try {
            return parseColor(value);
        } catch (IllegalArgumentException e) {
            FentLib.LOG.warn("Invalid color config value '{}'; using {}.", value, fallbackValue);
            property.set(fallbackValue);
            return fallbackColor;
        }
    }

    private static int parseColor(String rawColor) {
        String value = rawColor == null ? "" : rawColor.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
        }

        if (value.length() != 6 && value.length() != 8) {
            throw new IllegalArgumentException("Expected 6 or 8 hex digits");
        }

        try {
            long parsed = Long.parseLong(value, 16);
            if (value.length() == 6) {
                parsed |= 0xFF000000L;
            }
            return (int) parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected a hex color", e);
        }
    }

    private static boolean looksLikeAbsoluteUrl(String value) {
        return value != null && value.contains("://");
    }
}
