package org.fentanylsolutions.fentlib.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class ClassUtil {

    public static ModContainer modContainerByPackageName(String pkg) {
        for (ModContainer container : Loader.instance()
            .getModList()) {
            for (String ownedPkg : container.getOwnedPackages()) {
                if (pkg.equals(ownedPkg)) {
                    return container;
                }
            }
        }
        return null;
    }
}
