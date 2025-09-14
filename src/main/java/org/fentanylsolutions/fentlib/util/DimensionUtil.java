package org.fentanylsolutions.fentlib.util;

import net.minecraft.world.WorldProvider;

import org.fentanylsolutions.fentlib.FentLib;

public class DimensionUtil {

    public static class SimpleDimensionObj {

        private final int id;
        private final String name;
        private final String leaveMessage;

        public SimpleDimensionObj(int id, String name, String leaveMessage) {
            this.id = id;
            this.name = name;
            this.leaveMessage = leaveMessage;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLeaveMessage() {
            return leaveMessage;
        }
    }

    public static SimpleDimensionObj getSimpleDimensionObj(String name) {
        if (FentLib.varInstanceCommon.providers == null) {
            return null;
        }
        for (int i : FentLib.varInstanceCommon.providers.keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (FentLib.varInstanceCommon.providers.get(i) != null) {
                    worldProvider = FentLib.varInstanceCommon.providers.get(i)
                        .newInstance();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (worldProvider == null) {
                continue;
            }
            if (name.equals(worldProvider.getDimensionName())) {
                return new SimpleDimensionObj(
                    worldProvider.dimensionId,
                    worldProvider.getDimensionName(),
                    worldProvider.getDepartMessage());
            }
        }
        return null;
    }

    public static SimpleDimensionObj getSimpleDimensionObj(int id) {
        if (FentLib.varInstanceCommon.providers == null) {
            return null;
        }
        for (int i : FentLib.varInstanceCommon.providers.keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (FentLib.varInstanceCommon.providers.get(i) != null) {
                    worldProvider = FentLib.varInstanceCommon.providers.get(i)
                        .newInstance();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (worldProvider == null) {
                continue;
            }
            if (worldProvider.dimensionId == id) {
                return new SimpleDimensionObj(
                    worldProvider.dimensionId,
                    worldProvider.getDimensionName(),
                    worldProvider.getDepartMessage());
            }
        }
        return null;
    }

    public static void printDimensionNames() {
        FentLib.LOG.info("=========Dimension List=========");
        for (int i : FentLib.varInstanceCommon.providers.keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (FentLib.varInstanceCommon.providers.get(i) != null) {
                    worldProvider = FentLib.varInstanceCommon.providers.get(i)
                        .newInstance();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (worldProvider == null) {
                continue;
            }
            FentLib.LOG.info("{} ({})", worldProvider.getDimensionName(), worldProvider.dimensionId);
        }
        FentLib.LOG.info("=============================");
    }
}
