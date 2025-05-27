package org.fentanylsolutions.fentlib.util;

import net.minecraft.entity.EntityList;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class ClassUtil {

    public static ModContainer modContainerByPackageName(String pkg) {
        for (ModContainer container : Loader.instance()
            .getModList()) {
            for (String ownedPkg : container.getOwnedPackages()) {
                // System.out.println("Testing " + pkg + " and " + configClass.getPackage().getName());
                if (pkg.equals(ownedPkg)) {
                    return container;
                }
            }
        }
        return null;
    }

    public static String getEntityClassByName(String name) {
        Object res = EntityList.stringToClassMapping.get(name);
        if (res != null) {
            return ((Class) res).getCanonicalName();
        }
        return null;
    }
}
