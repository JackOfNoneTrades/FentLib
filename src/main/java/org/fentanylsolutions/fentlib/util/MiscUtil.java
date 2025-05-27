package org.fentanylsolutions.fentlib.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class MiscUtil {

    public static boolean isServer() {
        return FMLCommonHandler.instance()
            .getSide() == Side.SERVER;
    }
}
