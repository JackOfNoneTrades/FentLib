package org.fentanylsolutions.fentlib;

import org.fentanylsolutions.fentlib.command.CommandReloadServerIcon;
import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceClient;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceCommon;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceServer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        FentLib.varInstanceCommon = new VarInstanceCommon();
        if (MiscUtil.isServer()) {
            FentLib.varInstanceServer = new VarInstanceServer();
        } else {
            FentLib.varInstanceClient = new VarInstanceClient();
        }
        Config.loadConfig(FentLib.confFile);
        FentLib.LOG.info("I am Fentlib at version " + Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {

    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        FentLib.varInstanceCommon.postInitHook();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandReloadServerIcon());
    }
}
