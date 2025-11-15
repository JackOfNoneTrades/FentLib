package org.fentanylsolutions.fentlib;

import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceClient;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        FentLib.varInstanceClient = new VarInstanceClient();

        S00PacketServerInfoModifyService.registerDeserializeHandler((response, fentlibData, serverData) -> {
            if (fentlibData.has(FentLib.MODID)) {
                FentLib.debug("Fentlib detected for server @ " + serverData.serverIP);
            } else {
                FentLib.debug("Fentlib not detected for server @ " + serverData.serverIP);
            }
        });
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }
}
