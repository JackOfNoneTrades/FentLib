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

    public static boolean disableEnderCoreInfoButton = true;

    public static void loadConfig(File configFile) {
        config = new Configuration(configFile);

        try {
            config.load();

            // Debug
            debugMode = config.getBoolean("debugMode", Categories.debug, debugMode, "Enable debug mode.");
            Property passiveMobsWhichCanInflictDamageProperty = config.get(
                Categories.general,
                "passiveMobsWhichCanInflictDamage",
                passiveMobsWhichCanInflictDamage,
                "List of passive mobs that should be able to inflict damage.");
            passiveMobsWhichCanInflictDamage = passiveMobsWhichCanInflictDamageProperty.getStringList();

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

            // Misc tweaks
            disableEnderCoreInfoButton = config.getBoolean(
                "disableEnderCoreInfoButton",
                Categories.miscTweaks,
                disableEnderCoreInfoButton,
                "Disable the EnderCore information button in the modlist screen.");

            FentLib.varInstanceCommon.buildPassiveMobList();
        } catch (Exception e) {
            System.err.println("Error loading config: " + e.getMessage());
        } finally {
            config.save();
        }
    }

    public static Configuration getRawConfig() {
        return config;
    }
}
