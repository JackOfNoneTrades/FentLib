package org.fentanylsolutions.fentlib.util;

import net.minecraft.entity.EntityList;

import org.fentanylsolutions.fentlib.FentLib;

public class MobUtil {

    public static void test() {
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            String name = (String) e;
            System.out.println(name);
        }
    }

    public static String nameToPseudoResource(String name) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            return parts[0] + ":" + parts[1];
        } else {
            return "minecraft:" + name;
        }
    }

    public static String pseudoResourceToName(String resourceName) {
        String[] parts = resourceName.split(":", 2);
        if (parts.length < 2) return resourceName;

        if ("minecraft".equals(parts[0])) {
            return parts[1];
        } else {
            return parts[0] + "." + parts[1];
        }
    }

    public static void printMobNames() {
        FentLib.LOG.info("=========Mob List=========");
        for (String e : EntityList.stringToClassMapping.keySet()) {
            FentLib.LOG.info(
                "{} ({})",
                nameToPseudoResource(e),
                EntityList.stringToClassMapping.get(e)
                    .getName());
        }
        FentLib.LOG.info("=============================");
    }
}
