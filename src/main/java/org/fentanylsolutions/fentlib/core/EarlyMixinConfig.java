package org.fentanylsolutions.fentlib.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.launchwrapper.Launch;

public final class EarlyMixinConfig {

    private static final String CONFIG_FILE_NAME = "fentlib-early.properties";
    private static final String CONFIG_DIR_NAME = "fentlib";
    private static final String JAR_DISCOVERER_KEY = "mixin.jarDiscoverer";
    private static final boolean DEFAULT_JAR_DISCOVERER = false;
    private static final boolean ENABLE_JAR_DISCOVERER = loadBoolean(JAR_DISCOVERER_KEY, DEFAULT_JAR_DISCOVERER);

    private EarlyMixinConfig() {}

    public static boolean enableJarDiscovererMixin() {
        return ENABLE_JAR_DISCOVERER;
    }

    private static boolean loadBoolean(String key, boolean defaultValue) {
        Properties properties = new Properties();
        File configFile = new File(getConfigDir(), CONFIG_FILE_NAME);
        if (!configFile.isFile()) {
            properties.setProperty(key, Boolean.toString(defaultValue));
            saveProperties(configFile, properties);
            return defaultValue;
        }

        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            return defaultValue;
        }

        String value = properties.getProperty(key);
        if (value == null) {
            properties.setProperty(key, Boolean.toString(defaultValue));
            saveProperties(configFile, properties);
            return defaultValue;
        }

        return Boolean.parseBoolean(value.trim());
    }

    private static void saveProperties(File configFile, Properties properties) {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            return;
        }

        try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
            properties.store(outputStream, "FentLib early mixin settings");
        } catch (IOException e) {
            // Ignore bootstrap config write failures.
        }
    }

    private static File getConfigDir() {
        File minecraftHome = Launch.minecraftHome;
        if (minecraftHome == null) {
            minecraftHome = new File(".");
        }
        return new File(new File(minecraftHome, "config"), CONFIG_DIR_NAME);
    }
}
