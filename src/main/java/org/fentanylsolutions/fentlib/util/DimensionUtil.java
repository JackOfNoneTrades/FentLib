package org.fentanylsolutions.fentlib.util;

import net.minecraft.world.WorldProvider;

import org.fentanylsolutions.fentlib.mixins.early.minecraftforge.DimensionManagerAccessor;

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

    public static void test() {
        System.out.println("testerino dimensions");

        for (int i : DimensionManagerAccessor.getProviders()
            .keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (DimensionManagerAccessor.getProviders()
                    .get(i) != null) {
                    worldProvider = DimensionManagerAccessor.getProviders()
                        .get(i)
                        .newInstance();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (worldProvider == null) {
                continue;
            }
            System.out.println(
                worldProvider.dimensionId + ", "
                    + worldProvider.getDimensionName()
                    + ", "
                    + worldProvider.getDepartMessage());
        }
    }

    public static SimpleDimensionObj getSimpleDimensionObj(String name) {
        if (DimensionManagerAccessor.getProviders() == null) {
            return null;
        }
        for (int i : DimensionManagerAccessor.getProviders()
            .keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (DimensionManagerAccessor.getProviders()
                    .get(i) != null) {
                    worldProvider = DimensionManagerAccessor.getProviders()
                        .get(i)
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
        if (DimensionManagerAccessor.getProviders() == null) {
            return null;
        }
        for (int i : DimensionManagerAccessor.getProviders()
            .keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (DimensionManagerAccessor.getProviders()
                    .get(i) != null) {
                    worldProvider = DimensionManagerAccessor.getProviders()
                        .get(i)
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
}
