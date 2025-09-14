package org.fentanylsolutions.fentlib.util;

import java.lang.reflect.Constructor;

import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;

import org.fentanylsolutions.fentlib.FentLib;

import cpw.mods.fml.common.registry.GameRegistry;

public class MobUtil {

    public static void printMobNames() {
        FentLib.LOG.info("=========Mob List=========");
        FentLib.LOG.info(
            "The printing of this list is for you to know which mob has which name. You can disable this print in the configs.");
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            FentLib.LOG.info(e + " (" + EntityList.stringToClassMapping.get(e) + ")");
        }
        FentLib.LOG.info("=============================");
    }

    public static String getClassByName(String name) {
        Object res = EntityList.stringToClassMapping.get(name);
        if (res != null) {

            return ((Class) res).getCanonicalName();
        }
        return null;
    }

    public static <T extends ItemMonsterPlacer> void registerSpawnEgg(Class<T> eggClass, String name, int primaryColor,
        int secondaryColor, String modid) {
        try {
            // Create instance using a constructor that accepts (String, int, int)
            Constructor<T> constructor = eggClass.getConstructor(String.class, int.class, int.class);
            Item itemSpawnEgg = constructor.newInstance(name, primaryColor, secondaryColor)
                .setUnlocalizedName("spawn_egg_" + name.toLowerCase())
                .setTextureName(modid + ":spawn_egg_" + name.toLowerCase());

            GameRegistry.registerItem(itemSpawnEgg, "spawnEgg_" + name);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to register spawn egg for " + name, e);
        }
    }

}
