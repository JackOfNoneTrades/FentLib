package org.fentanylsolutions.fentlib;

import org.fentanylsolutions.fentlib.command.CommandDumpThaumonomicon;
import org.fentanylsolutions.fentlib.command.CommandReloadServerIcon;
import org.fentanylsolutions.fentlib.command.CommandWarpDim;
import org.fentanylsolutions.fentlib.compat.LoadedMods;
import org.fentanylsolutions.fentlib.packet.PacketHandler;
import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.fentanylsolutions.fentlib.services.fishing.FishingLootConfig;
import org.fentanylsolutions.fentlib.services.http.HttpPortProxyConfig;
import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceCommon;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceServer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        LoadedMods.init();
        FentLib.varInstanceCommon = new VarInstanceCommon();
        if (MiscUtil.isServer()) {
            FentLib.varInstanceServer = new VarInstanceServer();

            S00PacketServerInfoModifyService.registerHandler((response, fentLibPresent) -> {
                if (fentLibPresent) {
                    FentLib.debug("Fentlib detected");
                    if (FentLib.varInstanceServer.animatedFaviconBlob != null) {
                        FentLib.debug("Animated favicon not null, serving it");
                        response.func_151320_a(FentLib.varInstanceServer.animatedFaviconBlob);
                    } else {
                        FentLib.debug(
                            "Animated favicon null, serving static"
                                + (FentLib.varInstanceServer.staticFaviconBlob == null ? " (which is also null)" : ""));
                        response.func_151320_a(FentLib.varInstanceServer.staticFaviconBlob);
                    }
                } else {
                    FentLib.debug("Data has no animated feature, serving static");
                    response.func_151320_a(FentLib.varInstanceServer.staticFaviconBlob);
                }
                return null;
            });

            S00PacketServerInfoModifyService.registerHandler((s, j) -> { return FentLib.MODID; });
        }
        Config.loadConfig(FentLib.confFile);
        FentLib.fentlibDir = FentLib.getConfigDir();
        if (!FentLib.fentlibDir.exists()) {
            FentLib.fentlibDir.mkdirs();
        }
        FentLib.LOG.info("fentlibDir is located at {}", FentLib.fentlibDir);
        FishingLootConfig.ensureExists();
        if (MiscUtil.isServer()) {
            HttpPortProxyConfig.ensureExists();
        }

        PacketHandler.initPackets();

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
        event.registerServerCommand(new CommandDumpThaumonomicon());
        event.registerServerCommand(new CommandWarpDim());
    }

    public void serverStarting(FMLServerStartedEvent event) {
        System.out.println("SERVER STARTED");
    }
}
