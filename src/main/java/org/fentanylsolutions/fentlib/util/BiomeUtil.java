package org.fentanylsolutions.fentlib.util;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeUtil {

    public static BiomeGenBase getBiomeGenBaseByName(String name) {
        for (int i = 0; i < (BiomeGenBase.getBiomeGenArray()).length; i++) {
            if (BiomeGenBase.getBiomeGenArray()[i] != null) {
                if (BiomeGenBase.getBiomeGenArray()[i].biomeName.equals(name)) {
                    return BiomeGenBase.getBiomeGenArray()[i];
                }
            }
        }
        return null;
    }
}
