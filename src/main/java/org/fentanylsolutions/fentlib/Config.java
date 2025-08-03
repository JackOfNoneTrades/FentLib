package org.fentanylsolutions.fentlib;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean carbonConfigFixes = true;
    public static boolean disableDefaultCarbonHijack = true;
    public static String[] entityBlacklist = {
        "minecraft:Arrow",
        "minecraft:Boat",
    "minecraft:EnderCrystal",
    "minecraft:EyeOfEnderSignal",
    "minecraft:FallingSand",
    "minecraft:Fireball",
    "minecraft:FireworksRocketEntity",
    "minecraft:ItemFrame",
    "minecraft:Item",
    "minecraft:LeashKnot",
    "minecraft:MinecartChest",
    "minecraft:MinecartCommandBlock",
    "minecraft:MinecartFurnace",
    "minecraft:MinecartHopper",
    "minecraft:MinecartRideable",
    "minecraft:MinecartSpawner",
    "minecraft:MinecartTNT",
    "minecraft:Mob",
    "minecraft:Monster",
    "minecraft:Painting",
    "minecraft:PrimedTnt",
    "minecraft:SmallFireball",
    "minecraft:Snowball",
    "minecraft:ThrownEnderpearl",
    "minecraft:ThrownExpBottle",
    "minecraft:ThrownPotion",
    "minecraft:WitherSkull",
    "minecraft:XPOrb",
        "witchery:bolt",
        "witchery:brew2",
        "witchery:brew",
        "witchery:broom",
        "witchery:droplet",
        "witchery:eye",
        "witchery:grenade",
        "witchery:item",
        "witchery:soulfire",
        "witchery:spellEffect",
        "witchery:splatter"
    };

    public static void synchronizeConfiguration(File configFile) {
        FentLib.LOG.info("Syncing Fentlib base config");
        Configuration configuration = new Configuration(configFile);

        carbonConfigFixes = configuration.getBoolean(
            "carbonConfigFixes",
            "general",
            carbonConfigFixes,
            "Whether to fix Carbon Config list serialization and the display of list tooltips. It also disables carbon config hijacking regular forge config GUIs by default.");

        disableDefaultCarbonHijack = configuration.getBoolean(
            "disableDefaultCarbonHijack",
            "general",
            disableDefaultCarbonHijack,
            "Whether to disable carbon config hijacking regular forge config GUIs by default.");
        entityBlacklist = configuration.getStringList("entityBlacklist", "general", entityBlacklist, "A list of entities to ignore for the mob picker.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
