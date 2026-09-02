package org.fentanylsolutions.fentlib.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.launchwrapper.Launch;

public final class EarlyMixinConfig {

    private static final String CONFIG_FILE_NAME = "fentlib-early.properties";
    private static final String MAIN_CONFIG_FILE_NAME = "fentlib.cfg";
    private static final String CONFIG_DIR_NAME = "fentlib";
    private static final String JAR_DISCOVERER_KEY = "mixin.jarDiscoverer";
    private static final String MISC_TWEAKS_CATEGORY = "misc-tweaks";
    private static final String TERMINAL_DEPLOADER_PROGRESS_KEY = "terminalDepLoaderProgress";
    private static final boolean DEFAULT_JAR_DISCOVERER = false;
    private static final boolean DEFAULT_TERMINAL_DEPLOADER_PROGRESS = true;

    private EarlyMixinConfig() {}

    public static boolean enableJarDiscovererMixin() {
        return JarDiscovererSetting.ENABLED;
    }

    public static boolean terminalDepLoaderProgress() {
        return terminalDepLoaderProgress(getMinecraftHome());
    }

    public static boolean terminalDepLoaderProgress(File minecraftHome) {
        File configDir = new File(new File(minecraftHome, "config"), CONFIG_DIR_NAME);
        File configFile = new File(configDir, MAIN_CONFIG_FILE_NAME);
        if (!configFile.isFile()) {
            return DEFAULT_TERMINAL_DEPLOADER_PROGRESS;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            int depth = 0;
            int miscTweaksDepth = -1;
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (miscTweaksDepth < 0 && isCategoryStart(value, MISC_TWEAKS_CATEGORY)) {
                    miscTweaksDepth = depth;
                } else if (miscTweaksDepth >= 0 && value.startsWith("B:" + TERMINAL_DEPLOADER_PROGRESS_KEY)) {
                    int equals = value.indexOf('=');
                    if (equals >= 0) {
                        String configuredValue = value.substring(equals + 1)
                            .trim();
                        if ("true".equalsIgnoreCase(configuredValue)) {
                            return true;
                        }
                        if ("false".equalsIgnoreCase(configuredValue)) {
                            return false;
                        }
                        return DEFAULT_TERMINAL_DEPLOADER_PROGRESS;
                    }
                }

                depth += count(value, '{') - count(value, '}');
                if (miscTweaksDepth >= 0 && depth <= miscTweaksDepth) {
                    return DEFAULT_TERMINAL_DEPLOADER_PROGRESS;
                }
            }
        } catch (IOException ignored) {
            // Keep the default if the config is not readable during bootstrap.
        }
        return DEFAULT_TERMINAL_DEPLOADER_PROGRESS;
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
        return new File(new File(getMinecraftHome(), "config"), CONFIG_DIR_NAME);
    }

    private static File getMinecraftHome() {
        return Launch.minecraftHome == null ? new File(".") : Launch.minecraftHome;
    }

    private static boolean isCategoryStart(String line, String category) {
        String unquoted = line.replace("\"", "");
        return unquoted.startsWith(category) && unquoted.substring(category.length())
            .trim()
            .equals("{");
    }

    private static int count(String value, char character) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }

    private static final class JarDiscovererSetting {

        private static final boolean ENABLED = loadBoolean(JAR_DISCOVERER_KEY, DEFAULT_JAR_DISCOVERER);
    }
}
