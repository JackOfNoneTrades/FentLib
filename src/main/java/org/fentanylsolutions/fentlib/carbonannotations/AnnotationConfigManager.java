package org.fentanylsolutions.fentlib.carbonannotations;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fentanylsolutions.fentlib.Fentlib;
import org.fentanylsolutions.fentlib.mixininterfaces.IConfigMixin;
import org.fentanylsolutions.fentlib.util.ClassUtil;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.impl.ReloadMode;
import cpw.mods.fml.common.ModContainer;

public class AnnotationConfigManager {

    private static final List<ConfigContainer> registeredConfigs = new ArrayList<>();
    private static final Map<String, ConfigSection> categorySections = new HashMap<>();

    public static class ConfigContainer {

        public final Object instance;
        public final carbonconfiglib.config.ConfigHandler carbonConfig;
        public final Map<Field, ConfigEntry<?>> fieldEntries;

        public ConfigContainer(Object instance, carbonconfiglib.config.ConfigHandler carbonConfig,
            Map<Field, ConfigEntry<?>> fieldEntries) {
            this.instance = instance;
            this.carbonConfig = carbonConfig;
            this.fieldEntries = fieldEntries;
        }
    }

    /**
     * Scans all loaded classes for @Config annotations and registers them
     */
    public static void scanAndRegisterConfigs() {
        // Create reflections instance that scans all classpath
        Reflections reflections = new Reflections(
            new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader())
                .setScanners(Scanners.TypesAnnotated));

        Set<Class<?>> configClasses = reflections.getTypesAnnotatedWith(CarbonConfigAnnotations.FentConfig.class);

        for (Class<?> configClass : configClasses) {
            try {
                Fentlib.LOG.info("Found class " + configClass.getCanonicalName() + " annotated with @FentConfig");
                registerConfigClass(configClass);
            } catch (Exception e) {
                Fentlib.LOG.error("Failed to register config class: " + configClass.getName(), e);
            }
        }

        Fentlib.LOG.info("Registered {} config classes", registeredConfigs.size());
    }

    /**
     * Registers a single config class
     */
    private static void registerConfigClass(Class<?> configClass) throws Exception {
        CarbonConfigAnnotations.FentConfig configAnnotation = configClass
            .getAnnotation(CarbonConfigAnnotations.FentConfig.class);
        if (configAnnotation == null) {
            return;
        }

        // Create instance of the config class
        Object configInstance = configClass.getDeclaredConstructor()
            .newInstance();

        // Create carbon config
        carbonconfiglib.config.Config carbonConf = new carbonconfiglib.config.Config(configAnnotation.name());
        ModContainer container = ClassUtil.modContainerByPackageName(
            configClass.getPackage()
                .getName());
        if (container != null) {
            System.out.println("Adding realOwnerId" + container.getModId());
            // ((ConfigHandlerMixin) (Object) config).realOwnerId = container.getModId();
            ((IConfigMixin) carbonConf).setRealOwnerId(container.getModId());
        }

        // Process fields
        Map<Field, ConfigEntry<?>> fieldEntries = new HashMap<>();
        processConfigFields(configClass, carbonConf, fieldEntries, configAnnotation.name());

        // Create and register the config
        ConfigHandler config = CarbonConfig.CONFIGS.createConfig(carbonConf);

        // Add load listener to sync values back to fields
        config.addLoadedListener(() -> {
            Fentlib.LOG.info("Config loaded: {}, syncing values", configAnnotation.name());
            syncConfigToFields(configInstance, fieldEntries);
        });

        config.register();

        // Store reference
        registeredConfigs.add(new ConfigContainer(configInstance, config, fieldEntries));

        Fentlib.LOG.debug("Registered config: {}", configAnnotation.name());
    }

    /**
     * Processes all annotated fields in a config class
     */
    private static void processConfigFields(Class<?> configClass, carbonconfiglib.config.Config carbonConf,
        Map<Field, ConfigEntry<?>> fieldEntries, String configName) {

        for (Field field : configClass.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(CarbonConfigAnnotations.ConfigInt.class)) {
                processIntField(field, carbonConf, fieldEntries, configName);
            } else if (field.isAnnotationPresent(CarbonConfigAnnotations.ConfigBool.class)) {
                processBoolField(field, carbonConf, fieldEntries, configName);
            } else if (field.isAnnotationPresent(CarbonConfigAnnotations.ConfigArray.class)) {
                processArrayField(field, carbonConf, fieldEntries, configName);
            }

        }
    }

    /**
     * Processes a field annotated with @ConfigInt
     */
    private static void processIntField(Field field, carbonconfiglib.config.Config carbonConf,
        Map<Field, ConfigEntry<?>> fieldEntries, String configName) {
        CarbonConfigAnnotations.ConfigInt annotation = field.getAnnotation(CarbonConfigAnnotations.ConfigInt.class);

        // Get or create category section with proper namespacing
        ConfigSection section = getOrCreateSection(carbonConf, annotation.category(), configName);

        // Create config entry
        ConfigEntry.IntValue entry = section.addInt(annotation.name(), annotation.defaultValue());

        // Set properties
        if (!annotation.comment()
            .isEmpty()) {
            entry.setComment(annotation.comment());
        }

        if (annotation.min() != Integer.MIN_VALUE || annotation.max() != Integer.MAX_VALUE) {
            entry.setRange(annotation.min(), annotation.max());
        }

        if (annotation.min() != Integer.MIN_VALUE) {
            entry.setMin(annotation.min());
        }

        if (annotation.max() != Integer.MAX_VALUE) {
            entry.setMax(annotation.max());
        }

        if (annotation.clientSynced()) {
            entry.setClientSynced();
        }

        if (annotation.requiresGameReload()) {
            entry.setRequiredReload(ReloadMode.GAME);
        }

        if (annotation.requiresWorldReload()) {
            entry.setRequiredReload(ReloadMode.WORLD);
        }

        fieldEntries.put(field, entry);
    }

    private static void processBoolField(Field field, carbonconfiglib.config.Config carbonConf,
        Map<Field, ConfigEntry<?>> fieldEntries, String configName) {
        CarbonConfigAnnotations.ConfigBool annotation = field.getAnnotation(CarbonConfigAnnotations.ConfigBool.class);

        // Get or create category section with proper namespacing
        ConfigSection section = getOrCreateSection(carbonConf, annotation.category(), configName);

        // Create config entry
        ConfigEntry.BoolValue entry = section.addBool(annotation.name(), annotation.defaultValue());

        // Set properties
        if (!annotation.comment()
            .isEmpty()) {
            entry.setComment(annotation.comment());
        }

        if (annotation.clientSynced()) {
            entry.setClientSynced();
        }

        if (annotation.requiresGameReload()) {
            entry.setRequiredReload(ReloadMode.GAME);
        }

        if (annotation.requiresWorldReload()) {
            entry.setRequiredReload(ReloadMode.WORLD);
        }

        fieldEntries.put(field, entry);
    }

    private static void processArrayField(Field field, carbonconfiglib.config.Config carbonConf,
        Map<Field, ConfigEntry<?>> fieldEntries, String configName) {

        CarbonConfigAnnotations.ConfigArray annotation = field.getAnnotation(CarbonConfigAnnotations.ConfigArray.class);
        ConfigSection section = getOrCreateSection(carbonConf, annotation.category(), configName);

        Class<? extends IArraySerializer<?>> serializerClass;

        String[] serializedDefaults;

        try {
            field.setAccessible(true);
            Object value = field.get(null);
            Class<?> elementType = field.getType()
                .getComponentType();

            if (elementType == String.class) {
                serializedDefaults = (String[]) value;
            } else {
                if (elementType == int.class) {
                    serializerClass = Serializers.IntSerializer.class;
                } else if (elementType == double.class) {
                    serializerClass = Serializers.DoubleSerializer.class;
                } else if (elementType == float.class) {
                    serializerClass = Serializers.FloatSerializer.class;
                } else if (elementType == boolean.class) {
                    serializerClass = Serializers.BooleanSerializer.class;
                } else {
                    serializerClass = annotation.serializer();
                }
                IArraySerializer<Object> serializer = (IArraySerializer<Object>) serializerClass
                    .getDeclaredConstructor()
                    .newInstance();

                int length = Array.getLength(value);
                serializedDefaults = new String[length];

                for (int i = 0; i < length; i++) {
                    Object elem = Array.get(value, i);
                    serializedDefaults[i] = serializer.serialize(elem);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read default value from field: " + field.getName(), e);
        }

        ConfigEntry.ArrayValue entry = section.addArray(annotation.name(), serializedDefaults, annotation.comment());

        if (annotation.clientSynced()) {
            entry.setClientSynced();
        }

        if (annotation.requiresGameReload()) {
            entry.setRequiredReload(ReloadMode.GAME);
        }

        if (annotation.requiresWorldReload()) {
            entry.setRequiredReload(ReloadMode.WORLD);
        }

        fieldEntries.put(field, entry);
    }

    /**
     * Gets or creates a config section for a category
     */
    private static ConfigSection getOrCreateSection(carbonconfiglib.config.Config carbonConf, String category,
        String configName) {
        String key = configName + ":" + category;
        return categorySections.computeIfAbsent(key, k -> carbonConf.add(category));
    }

    /**
     * Syncs config values back to the annotated fields
     */
    private static void syncConfigToFields(Object configInstance, Map<Field, ConfigEntry<?>> fieldEntries) {
        for (Map.Entry<Field, ConfigEntry<?>> entry : fieldEntries.entrySet()) {
            Field field = entry.getKey();
            field.setAccessible(true);

            ConfigEntry<?> configEntry = entry.getValue();

            try {
                if (configEntry instanceof ConfigEntry.IntValue) {
                    field.setInt(configInstance, ((ConfigEntry.IntValue) configEntry).get());
                } else if (configEntry instanceof ConfigEntry.BoolValue) {
                    field.setBoolean(configInstance, ((ConfigEntry.BoolValue) configEntry).get());
                } else if (configEntry instanceof ConfigEntry.ArrayValue) {
                    // Handle array field
                    CarbonConfigAnnotations.ConfigArray annotation = field
                        .getAnnotation(CarbonConfigAnnotations.ConfigArray.class);
                    if (annotation == null) continue;

                    String[] rawValues = ((ConfigEntry.ArrayValue) configEntry).get();
                    Class<?> elementType = field.getType()
                        .getComponentType();
                    Class<? extends IArraySerializer<?>> serializerClass = annotation.serializer();

                    Object parsedArray;

                    if (elementType == String.class) {
                        parsedArray = rawValues;
                    } else {
                        IArraySerializer<Object> serializer = (IArraySerializer<Object>) serializerClass
                            .getDeclaredConstructor()
                            .newInstance();
                        Object array = Array.newInstance(elementType, rawValues.length);
                        for (int i = 0; i < rawValues.length; i++) {
                            Object deserialized = serializer.deserialize(rawValues[i]);
                            Class<?> componentType = array.getClass()
                                .getComponentType();
                            if (deserialized instanceof Integer) {
                                Array.set(array, i, ((Integer) deserialized).intValue());
                            } else if (deserialized instanceof Double) {
                                Array.set(array, i, ((Double) deserialized).doubleValue());
                            } else if (deserialized instanceof Float) {
                                Array.set(array, i, ((Float) deserialized).floatValue());
                            } else if (deserialized instanceof Boolean) {
                                Array.set(array, i, ((Boolean) deserialized).booleanValue());
                            } else {
                                Array.set(array, i, componentType.cast(deserialized));
                            }
                        }
                        parsedArray = array;
                    }

                    field.set(configInstance, parsedArray);
                }
            } catch (IllegalAccessException e) {
                Fentlib.LOG.error("Failed to sync field: " + field.getName(), e);
            } catch (InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Gets all registered config containers
     */
    public static List<ConfigContainer> getRegisteredConfigs() {
        return Collections.unmodifiableList(registeredConfigs);
    }

    /**
     * Gets a specific config instance by name
     */
    public static <T> T getConfigInstance(String configName, Class<T> configClass) {
        return registeredConfigs.stream()
            .filter(
                container -> container.instance.getClass()
                    .equals(configClass))
            .map(container -> configClass.cast(container.instance))
            .findFirst()
            .orElse(null);
    }
}
