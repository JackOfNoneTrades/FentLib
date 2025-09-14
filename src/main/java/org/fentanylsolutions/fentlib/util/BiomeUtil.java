package org.fentanylsolutions.fentlib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.fentlib.FentLib;

public class BiomeUtil {

    public static List<BiomeGenBase> getBiomeList() {
        List<BiomeGenBase> res = new ArrayList<>();

        Arrays.stream(BiomeGenBase.getBiomeGenArray())
            .filter(Objects::nonNull)
            .forEach(res::add);

        return (res);
    }

    public static void printBiomeNames() {
        FentLib.LOG.info("=========Biome List=========");
        for (BiomeGenBase b : getBiomeList()) {
            FentLib.LOG.info("{} ({})", b.biomeName, b.biomeID);
        }
        FentLib.LOG.info("=============================");
    }
}
