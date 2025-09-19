package org.fentanylsolutions.fentlib;

import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.fentanylsolutions.fentlib.varinstances.VarInstanceClient;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        FentLib.varInstanceClient = new VarInstanceClient();

        S00PacketServerInfoModifyService.put(FentLib.ANIMATED_FEATURE, "1");
    }
}
