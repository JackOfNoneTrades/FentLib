package org.fentanylsolutions.fentlib;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

import org.fentanylsolutions.fentlib.carbonextension.carbonannotations.AnnotationConfigManager;
import org.fentanylsolutions.fentlib.carbonextension.carbonannotations.CarbonConfigAnnotations;
import org.fentanylsolutions.fentlib.carbonextension.carbonannotations.CarbonConfigAnnotations.ConfigInt;
import org.fentanylsolutions.fentlib.carbonextension.carbonannotations.CarbonConfigAnnotations.FentConfig;
import org.fentanylsolutions.fentlib.carbonextension.MobWrapper;

import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.RegistryValue;
import org.fentanylsolutions.fentlib.mixininterfaces.IRegistryValue;

@FentConfig(name = "testfentconf")
public class TestConf {

    @ConfigInt(
        name = "test_value",
        comment = "This is a test integer value",
        defaultValue = 5,
        min = -10,
        max = 10,
        category = "general")
    public static int testValue;

    @CarbonConfigAnnotations.ConfigArray(name = "testStrArray", category = "general", comment = "hmmmmm")
    public static String[] testStrings = { "bruh", "lmao" };

    @CarbonConfigAnnotations.ConfigArray(name = "testIntArray", category = "general", comment = "hmmmmm")
    public static int[] testInts = { 1, 3, -8 };

    @CarbonConfigAnnotations.ConfigArray(name = "testBoolArray", category = "general", comment = "hmmmmm")
    public static boolean[] testBools = { true, false, false };

    public static int testColor = 0xA0A0A0;
    static ColorValue colorCE;

    public static String testItem = "";
    static ConfigEntry.StringValue itemCE;

    @CarbonConfigAnnotations.FentConfigOnCreate
    public static void onCreate(Config config) {
        ConfigSection generalSection = AnnotationConfigManager.getOrCreateSection(config, "general");
        colorCE = generalSection.add(new ColorValue("testColor", testColor, "Test color (hex RGB)"));

        // N.B. withFilter is ONLY applied when saving values, not proposing them from a list.
        // If you want to present a tailored list to the user, you have to create a new Registry...

        RegistryValue<Item> itemCE = RegistryValue.builder("testItem", Item.class)
            .addDefault(Items.apple)
            .withFilter(x -> true)
            .withComment("custom item")
            .build(null);
        ((IRegistryValue)itemCE).setRegistry(FentLib.varInstanceCommon.carbonCompat.itemFMLRegistry);
        generalSection.add(itemCE);

        RegistryValue<MobWrapper> mobCE = RegistryValue.builder("testMob", MobWrapper.class).
            addDefault(new MobWrapper("minecraft:Pig")).
            withComment("custom mob conf elem")
                .build(null);
        ((IRegistryValue)mobCE).setRegistry(FentLib.varInstanceCommon.carbonCompat.entityRegistry);
        generalSection.add(mobCE);
    }

    @CarbonConfigAnnotations.FentConfigOnSync
    public static void onSync() {
        FentLib.LOG.info("test conf additional on sync triggered");
        testColor = colorCE.get();
    }
}
