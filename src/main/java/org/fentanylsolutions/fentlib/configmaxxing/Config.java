package org.fentanylsolutions.fentlib.configmaxxing;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {

    static Configuration config = new Configuration(new File("s"));

    private static class Defaults {

        public static final boolean debug = false;

        public static final String[] mobBlackListArray = { "MinecartChest", "EyeOfEnderSignal", "ItemFrame",
            "MinecartCommandBlock", "Item", "Fireball", "MinecartTNT", "ThrownPotion", "LeashKnot", "Arrow",
            "MinecartSpawner", "EnderCrystal", "Snowball", "MinecartHopper", "XPOrb", "ThrownExpBottle",
            "FireworksRocketEntity", "FallingSand", "PrimedTnt", "ThrownEnderpearl", "Monster", "SmallFireball",
            "MinecartRideable", "Mob", "Painting", "MinecartFurnace", "WitherSkull", "Boat" };

        public static final String singlePotion = "potion.moveSpeed";
        public static final String[] potionArray = {};
        public static final String[] potionArrayMaxLen = {};

        /*
         * Fixed size lists have to be initialized with elements otherwise it NPE's. It's a Forge limitation I don't
         * care enough about to fix.
         */
        public static final String[] potionArrayFixedSize = { "potion.moveSpeed", "potion.moveSlowdown",
            "potion.digSpeed", "potion.digSlowDown", "potion.damageBoost" };

        public static final String singleMob = "Wolf";
        public static final String[] mobArray = {};
        public static final String[] mobArrayMaxLen = {};
        public static final String[] mobArrayFixedSize = { "EntityHorse", "Ozelot", "EnderDragon", "Enderman",
            "Zombie" };

        public static final String[] biomeArray = {};
        public static final String[] blockArray = {};
        public static final String[] dimensionArray = {};
    }

    public static class Categories {

        public static final String general = "general";
        public static final String debug = "debug";
        public static final String examples = "examples";
        public static final String test = "test";
    }

    public static String singlePotion = Defaults.singlePotion;
    public static String[] potionArray = Defaults.potionArray;
    public static String[] potionArrayMaxLen = Defaults.potionArrayMaxLen;
    public static String[] potionArrayFixedSize = Defaults.potionArrayFixedSize;

    public static String singleMob = Defaults.singleMob;
    public static String[] mobArray = Defaults.mobArray;
    public static String[] mobArrayMaxLen = Defaults.mobArrayMaxLen;
    public static String[] mobArrayFixedSize = Defaults.mobArrayFixedSize;
    public static String singlePotionButSlimBecauseGTMEGA = Defaults.singlePotion;
    public static String[] mobBlackListArray = Defaults.mobBlackListArray;

    public static String[] biomeArray = Defaults.biomeArray;
    public static String[] blockArray = Defaults.blockArray;
    public static String[] dimensionArray = Defaults.dimensionArray;

    public static boolean debug;

    /* multiple values entry */
    public static HashMap<String, Property> multiVal;

    /*
     * public static void synchronizeConfigurationCommon() {
     * Property debugProperty = config.get(Categories.debug, "debug", Defaults.debug, "Enable/disable debug mode");
     * debug = debugProperty.getBoolean();
     * Property mobBlackListArrayProperty = config
     * .get(Categories.general, "mobBlackListArray", Defaults.mobBlackListArray, "Example list of potions.");
     * if (!Util.isServer()) {
     * mobBlackListArrayProperty.setConfigEntryClass(MobBlacklistEntryPoint.class);
     * }
     * mobBlackListArray = mobBlackListArrayProperty.getStringList();
     * Property singlePotionProperty = config
     * .get(Categories.examples, "singlePotion", Defaults.singlePotion, "Example single potion.");
     * if (!Util.isServer()) {
     * singlePotionProperty.setConfigEntryClass(PotionEntryPoint.class);
     * }
     * singlePotion = singlePotionProperty.getString();
     * Property potionArrayProperty = config
     * .get(Categories.examples, "potionArray", Defaults.potionArray, "Example list of potions.");
     * if (!Util.isServer()) {
     * potionArrayProperty.setConfigEntryClass(PotionEntryPoint.class);
     * }
     * potionArray = potionArrayProperty.getStringList();
     * Property potionArrayMaxLenProperty = config.get(
     * Categories.examples,
     * "potionArrayMaxLen",
     * Defaults.potionArrayMaxLen,
     * "Example list of potions with a maximum size.",
     * false,
     * 5,
     * null);
     * if (!Util.isServer()) {
     * potionArrayMaxLenProperty.setConfigEntryClass(PotionEntryPoint.class);
     * }
     * potionArrayMaxLen = potionArrayMaxLenProperty.getStringList();
     * Property potionArrayFixedSizeProperty = config.get(
     * Categories.examples,
     * "potionArrayFixedSize",
     * Defaults.potionArrayFixedSize,
     * "Example list of potions with a fixed size.",
     * true,
     * 5,
     * null);
     * if (!Util.isServer()) {
     * potionArrayFixedSizeProperty.setConfigEntryClass(PotionEntryPoint.class);
     * }
     * potionArrayFixedSize = potionArrayFixedSizeProperty.getStringList();
     * Property singleMobProperty = config
     * .get(Categories.examples, "singleMob", Defaults.singleMob, "Example single mob.");
     * if (!Util.isServer()) {
     * singleMobProperty.setConfigEntryClass(MobEntryPoint.class);
     * }
     * singleMob = singleMobProperty.getString();
     * Property mobArrayProperty = config
     * .get(Categories.examples, "mobArray", Defaults.mobArray, "Example list of mobs.");
     * if (!Util.isServer()) {
     * mobArrayProperty.setConfigEntryClass(MobEntryPoint.class);
     * }
     * mobArray = mobArrayProperty.getStringList();
     * Property mobArrayMaxLenProperty = config.get(
     * Categories.examples,
     * "mobArrayMaxLen",
     * Defaults.mobArrayMaxLen,
     * "Example list of mobs with a maximum size.",
     * false,
     * 5,
     * null);
     * if (!Util.isServer()) {
     * mobArrayMaxLenProperty.setConfigEntryClass(MobEntryPoint.class);
     * }
     * mobArrayMaxLen = mobArrayMaxLenProperty.getStringList();
     * Property mobArrayFixedSizeProperty = config.get(
     * Categories.examples,
     * "mobArrayFixedSize",
     * Defaults.mobArrayFixedSize,
     * "Example list of mobs with a fixed size.",
     * true,
     * 5,
     * null);
     * if (!Util.isServer()) {
     * mobArrayFixedSizeProperty.setConfigEntryClass(MobEntryPoint.class);
     * }
     * mobArrayFixedSize = mobArrayFixedSizeProperty.getStringList();
     * Property biomeArrayProperty = config
     * .get(Categories.examples, "biomeArray", Defaults.biomeArray, "Example list of biomes.");
     * if (!Util.isServer()) {
     * biomeArrayProperty.setConfigEntryClass(BiomeEntryPoint.class);
     * }
     * biomeArray = biomeArrayProperty.getStringList();
     * Property blockArrayProperty = config
     * .get(Categories.examples, "blockArray", Defaults.blockArray, "Example list of blocks.");
     * if (!Util.isServer()) {
     * blockArrayProperty.setConfigEntryClass(BlockEntryPoint.class);
     * }
     * blockArray = blockArrayProperty.getStringList();
     * Property dimensionArrayProperty = config
     * .get(Categories.examples, "dimensionArray", Defaults.dimensionArray, "Example list of dimensions.");
     * if (!Util.isServer()) {
     * dimensionArrayProperty.setConfigEntryClass(DimensionEntryPoint.class);
     * }
     * dimensionArray = dimensionArrayProperty.getStringList();
     * Property singlePotionSlimProperty = config.get(
     * Categories.examples,
     * "slimExample for skill issue",
     * Defaults.singlePotion,
     * "This is to demonstrate slimmer selection list entries. For the MEGA modpack people. Houston, if you want smaller entries, just make them yourself"
     * );
     * if (!Util.isServer()) {
     * try {
     * Class entry = Class
     * .forName("trollogyadherent.configmaxxing.configpickers.potionslim.PotionSlimEntryPoint");
     * singlePotionSlimProperty.setConfigEntryClass(entry);
     * } catch (ClassNotFoundException e) {
     * }
     * }
     * singlePotionButSlimBecauseGTMEGA = singlePotionSlimProperty.getString();
     * Property testPotionProperty = MultiValueRegistry.vars.config
     * .get(MultiValueEntryPoint.MULTIVAL_CATEGORY, "testPotion", Defaults.singlePotion, "");
     * if (!Util.isServer()) {
     * testPotionProperty.setConfigEntryClass(PotionEntryPoint.class);
     * }
     * Property testMobProperty = MultiValueRegistry.vars.config
     * .get(MultiValueEntryPoint.MULTIVAL_CATEGORY, "testMob", Defaults.singleMob, "");
     * if (!Util.isServer()) {
     * testMobProperty.setConfigEntryClass(MobEntryPoint.class);
     * }
     * Property intPropForMulti = config.get(MultiValueEntryPoint.MULTIVAL_CATEGORY, "multiint", 6969);
     * MultiValueConfigProperty multiValProp = new MultiValueConfigProperty(
     * Categories.test,
     * "testMultival",
     * "lalala",
     * Config.config);
     * multiValProp.registerProp(testPotionProperty);
     * multiValProp.registerProp(testMobProperty);
     * multiValProp.registerProp(intPropForMulti);
     * multiVal = multiValProp.getValues();
     * Property intTestProp = config.get(Categories.test, "my_int", 420);
     * Property intTestProp2 = config.get(Categories.test, "my_int2", 69);
     * Property intTestProp3 = config.get(Categories.test, "my_int3", -1);
     * }
     */
}
