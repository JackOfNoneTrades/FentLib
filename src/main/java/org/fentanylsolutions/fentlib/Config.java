package org.fentanylsolutions.fentlib;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean carbonConfigFixes = true;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        carbonConfigFixes = configuration.getBoolean(
            "carbonConfigFixes",
            "general",
            carbonConfigFixes,
            "Whether to fix Carbon Config list serialization and the display of list tooltips. It also disables carbon config hijacking regular forge config GUIs by default.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
