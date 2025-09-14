package org.fentanylsolutions.fentlib;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceClient;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceCommon;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceServer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = FentLib.MODID,
    version = Tags.VERSION,
    name = "FentLib",
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "org.fentanylsolutions." + FentLib.MODID + ".gui.GuiFactory")
public class FentLib {

    public static final String MODID = "fentlib";
    public static final String MODGROUP = "org.fentanylsolutions";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static File confFile;

    private static boolean DEBUG_MODE;

    public static VarInstanceClient varInstanceClient;
    public static VarInstanceServer varInstanceServer;
    public static VarInstanceCommon varInstanceCommon;

    @SidedProxy(
        clientSide = MODGROUP + "." + MODID + ".ClientProxy",
        serverSide = MODGROUP + "." + MODID + ".CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        confFile = event.getSuggestedConfigurationFile();
        String debugVar = System.getenv("MCMODDING_DEBUG_MODE");
        DEBUG_MODE = debugVar != null;
        FentLib.LOG.info("Debugmode: {}", DEBUG_MODE);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    public static boolean isDebugMode() {
        return DEBUG_MODE || Config.debugMode;
    }

    public static void debug(String message) {
        if (isDebugMode()) {
            LOG.info("DEBUG: {}", message);
        }
    }

    /*
     * ▖▖..........▗..▄▖..........
     * ▙▘▛▌▛▌▛▘█▌▛▘▜▘ ▗▘▌▌▛▘▀▌█▌▛▌
     * ▌▌▙▌▌▌▙▖▙▖▌ ▐▖ ▙▖▙▌▙▖▙▖▙▖▌▌
     * .................▄▌........
     */

    // TODO: Make the mod menu scrollable only in the list area. Don't forgor separate mixin for hodgepodge and vanilla
    // endercore
}
