package org.fentanylsolutions.fentlib.services.fishing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.FishingHooks;

import org.fentanylsolutions.fentlib.FentLib;

public final class FishingLootInterop {

    private static final String LOTR_FISH_RESULT_CLASS_NAME = "lotr.common.entity.projectile.LOTRFishing$FishResult";
    private static final ThreadLocal<BiomeGenBase> lotrFishingBiome = new ThreadLocal<>();

    private static volatile Class<?> lotrFishResultClass;
    private static volatile Constructor<?> lotrFishResultConstructor;
    private static volatile Field lotrFishResultCategoryField;
    private static volatile boolean lotrFishResultReflectionResolved = false;
    private static volatile boolean warnedLotrFishResultReflection = false;

    private FishingLootInterop() {}

    public static void setLotrFishingBiome(BiomeGenBase biome) {
        if (biome == null) {
            clearLotrFishingBiome();
            return;
        }
        lotrFishingBiome.set(biome);
    }

    public static void clearLotrFishingBiome() {
        lotrFishingBiome.remove();
    }

    public static Object overrideLotrFishResult(Object currentResult, Random rand) {
        if (currentResult == null || rand == null) {
            return currentResult;
        }
        if (!resolveLotrFishResultReflection()) {
            return currentResult;
        }
        if (!lotrFishResultClass.isInstance(currentResult)) {
            return currentResult;
        }

        try {
            FishingHooks.FishableCategory category = (FishingHooks.FishableCategory) lotrFishResultCategoryField
                .get(currentResult);
            if (category == null) {
                return currentResult;
            }

            BiomeGenBase biome = lotrFishingBiome.get();
            ItemStack customLoot = FishingLootConfig.getRandomLoot(biome, category, rand);
            if (customLoot == null) {
                customLoot = new ItemStack(Items.stick);
            }

            return lotrFishResultConstructor.newInstance(category, customLoot);
        } catch (Exception e) {
            warnLotrFishResultReflection("Failed to override LOTR fishing loot", e);
            return currentResult;
        }
    }

    private static boolean resolveLotrFishResultReflection() {
        if (lotrFishResultReflectionResolved) {
            return lotrFishResultClass != null && lotrFishResultConstructor != null
                && lotrFishResultCategoryField != null;
        }

        synchronized (FishingLootInterop.class) {
            if (lotrFishResultReflectionResolved) {
                return lotrFishResultClass != null && lotrFishResultConstructor != null
                    && lotrFishResultCategoryField != null;
            }

            try {
                Class<?> resolvedClass = Class.forName(LOTR_FISH_RESULT_CLASS_NAME);
                Constructor<?> resolvedConstructor = resolvedClass
                    .getConstructor(FishingHooks.FishableCategory.class, ItemStack.class);
                Field resolvedCategoryField = resolvedClass.getField("category");

                lotrFishResultClass = resolvedClass;
                lotrFishResultConstructor = resolvedConstructor;
                lotrFishResultCategoryField = resolvedCategoryField;
            } catch (Exception e) {
                warnLotrFishResultReflection("Failed to resolve LOTR fishing result reflection", e);
            } finally {
                lotrFishResultReflectionResolved = true;
            }

            return lotrFishResultClass != null && lotrFishResultConstructor != null
                && lotrFishResultCategoryField != null;
        }
    }

    private static void warnLotrFishResultReflection(String message, Exception e) {
        if (warnedLotrFishResultReflection) {
            return;
        }
        synchronized (FishingLootInterop.class) {
            if (warnedLotrFishResultReflection) {
                return;
            }
            warnedLotrFishResultReflection = true;
            FentLib.LOG.warn(message, e);
        }
    }
}
