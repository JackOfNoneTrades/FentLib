package org.fentanylsolutions.fentlib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.fentlib.FentLib;

public class BiomeUtil {

    private static volatile List<BiomeGenBase> cachedLotrBiomes;
    private static volatile boolean lotrBiomesResolved = false;

    public static List<BiomeGenBase> getBiomeList() {
        Map<Integer, BiomeGenBase> biomesById = new LinkedHashMap<>();

        Arrays.stream(BiomeGenBase.getBiomeGenArray())
            .filter(Objects::nonNull)
            .forEach(biome -> biomesById.putIfAbsent(biome.biomeID, biome));

        for (BiomeGenBase biome : getOptionalLotrBiomes()) {
            if (biome != null) {
                biomesById.putIfAbsent(biome.biomeID, biome);
            }
        }

        return new ArrayList<>(biomesById.values());
    }

    public static BiomeGenBase getBiomeGenBase(String selector) {
        if (selector == null) {
            return null;
        }
        String trimmed = selector.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return getBiomeGenBase(Integer.parseInt(trimmed));
        } catch (NumberFormatException ignored) {}

        for (BiomeGenBase biome : getBiomeList()) {
            if (biome.biomeName != null && biome.biomeName.equalsIgnoreCase(trimmed)) {
                return biome;
            }
        }
        return null;
    }

    public static BiomeGenBase getBiomeGenBase(int id) {
        for (BiomeGenBase biome : getBiomeList()) {
            if (biome.biomeID == id) {
                return biome;
            }
        }
        return null;
    }

    public static boolean matchesBiomeSelector(BiomeGenBase biome, String selector) {
        if (biome == null || selector == null) {
            return false;
        }
        String trimmed = selector.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if ("*".equals(trimmed)) {
            return true;
        }
        try {
            return biome.biomeID == Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {}
        return biome.biomeName != null && biome.biomeName.equalsIgnoreCase(trimmed);
    }

    public static void printBiomeNames() {
        FentLib.LOG.info("=========Biome List=========");
        for (BiomeGenBase b : getBiomeList()) {
            FentLib.LOG.info("{} ({}) ({})", b.biomeName, b.biomeID, b.getClass());
        }
        FentLib.LOG.info("=============================");
    }

    private static List<BiomeGenBase> getOptionalLotrBiomes() {
        if (lotrBiomesResolved) {
            return cachedLotrBiomes;
        }
        synchronized (BiomeUtil.class) {
            if (lotrBiomesResolved) {
                return cachedLotrBiomes;
            }

            List<BiomeGenBase> resolved = new ArrayList<>();

            try {
                Class<?> dimensionClass = Class.forName("lotr.common.LOTRDimension");
                addLotrDimensionBiomes(
                    resolved,
                    dimensionClass.getField("MIDDLE_EARTH")
                        .get(null));
                addLotrDimensionBiomes(
                    resolved,
                    dimensionClass.getField("UTUMNO")
                        .get(null));
            } catch (ClassNotFoundException ignored) {
                // LOTR is optional.
            } catch (Exception e) {
                FentLib.LOG.warn("Failed to enumerate LOTR biomes", e);
            }

            cachedLotrBiomes = resolved;
            lotrBiomesResolved = true;
            return cachedLotrBiomes;
        }
    }

    private static void addLotrDimensionBiomes(List<BiomeGenBase> resolved, Object dimension) throws Exception {
        if (dimension == null) {
            return;
        }
        Object biomeListObject = dimension.getClass()
            .getField("biomeList")
            .get(dimension);
        if (!(biomeListObject instanceof Iterable<?>)) {
            return;
        }
        Iterable<?> biomeList = (Iterable<?>) biomeListObject;
        for (Object biome : biomeList) {
            if (biome instanceof BiomeGenBase) {
                resolved.add((BiomeGenBase) biome);
            }
        }
    }
}
