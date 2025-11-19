package org.fentanylsolutions.fentlib;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import org.fentanylsolutions.fentlib.misc.AutomatorEventHandler;
import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceClient;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        AutomatorEventHandler.tailLog();

        FentLib.varInstanceClient = new VarInstanceClient();

        S00PacketServerInfoModifyService.registerDeserializeHandler((response, fentlibData, serverData) -> {
            if (fentlibData.has(FentLib.MODID)) {
                FentLib.debug("Fentlib detected for server @ " + serverData.serverIP);
            } else {
                FentLib.debug("Fentlib not detected for server @ " + serverData.serverIP);
            }
        });

        if (System.getenv("AUTOSTART_CLIENT") != null) {
            FentLib.debug("AUTOSTART_CLIENT env var is set, registering AutomatorEventHandler");
            AutomatorEventHandler lol = new AutomatorEventHandler();
            FMLCommonHandler.instance()
                .bus()
                .register(lol);
            MinecraftForge.EVENT_BUS.register(lol);
            Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }
}
