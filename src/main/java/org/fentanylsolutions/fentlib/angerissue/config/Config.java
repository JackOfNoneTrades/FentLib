package org.fentanylsolutions.fentlib.angerissue.config;

public class Config {

    private static class Defaults {

        public static final String mobList[] = new String[0];
        public static final float damage = 1;
    }

    public static class Categories {

        public static final String general = "general";
    }

    public static String mobList[] = Defaults.mobList;
    public static float damage = Defaults.damage;

    public static void synchronizeConfigurationCommon() {
        /*
         * Property mobListProperty = config
         * .get(Categories.general, "mobList", Defaults.mobList, "List of mobs being able to deal damage.");
         * if (!Util.isServer() && AngerIssue.varInstanceClient.configMaxxingLoaded) {
         * mobListProperty.setConfigEntryClass(MobEntryPoint.class);
         * }
         * mobList = mobListProperty.getStringList();
         * Property damageProperty = config
         * .get(Categories.general, "damage", Defaults.damage, "How much damage affected mobs deal.");
         * damage = (float) damageProperty.getDouble();
         * if (config.hasChanged()) {
         * config.save();
         * }
         * AngerIssue.varInstanceCommon.updateAffectedMobs();
         */

        // TODO: rename main to master
    }
}
