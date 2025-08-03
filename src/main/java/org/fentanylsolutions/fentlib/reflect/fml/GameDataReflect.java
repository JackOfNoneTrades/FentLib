package org.fentanylsolutions.fentlib.reflect.fml;

import com.google.common.collect.BiMap;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.RegistryDelegate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GameDataReflect {

    private static final Class<?> GAME_DATA_CLASS = GameData.class;

    public static int getMAX_BLOCK_ID() {
        try {
            Field field = GAME_DATA_CLASS.getDeclaredField("MAX_BLOCK_ID");
            field.setAccessible(true);
            return field.getInt(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access MAX_BLOCK_ID", e);
        }
    }

    public static GameData invokeGetMain() {
        try {
            Method method = GAME_DATA_CLASS.getDeclaredMethod("getMain");
            method.setAccessible(true);
            return (GameData) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getMain", e);
        }
    }

    public static int invokeRegister(Object instance, Object obj, String name, int idHint) {
        try {
            Method method = GAME_DATA_CLASS.getDeclaredMethod("register", Object.class, String.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(instance, obj, name, idHint);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke register", e);
        }
    }

    public static <T> RegistryDelegate<T> invokeBuildDelegate(T referant, Class<T> type) {
        try {
            Method method = GAME_DATA_CLASS.getDeclaredMethod("buildDelegate", Object.class, Class.class);
            method.setAccessible(true);
            return (RegistryDelegate<T>) method.invoke(null, referant, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke buildDelegate", e);
        }
    }

    public static <T> BiMap<String, T> invokeGetPersistentSubstitutionMap(Object instance, Class<T> type) {
        try {
            Method method = GAME_DATA_CLASS.getDeclaredMethod("getPersistentSubstitutionMap", Class.class);
            method.setAccessible(true);
            return (BiMap<String, T>) method.invoke(instance, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke getPersistentSubstitutionMap", e);
        }
    }
}
