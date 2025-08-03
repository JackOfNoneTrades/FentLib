package org.fentanylsolutions.fentlib.varinstances;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.mob.MobRenderTicker;

public class VarInstanceClient {

    public MobRenderTicker mobRenderTicker;

    public void postInitHook() {
        if (FentLib.carbonConfigLoaded) {
            this.mobRenderTicker = new MobRenderTicker();
            MobRenderTicker.init();
            //this.mobRenderTicker.register();
        }
    }
}
