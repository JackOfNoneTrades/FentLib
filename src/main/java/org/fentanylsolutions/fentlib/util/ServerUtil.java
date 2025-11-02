package org.fentanylsolutions.fentlib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.FMLCommonHandler;

public class ServerUtil {

    public static boolean isOp(EntityPlayerMP entityPlayerMP) {
        // func_152596_g: canSendCommands
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance()
            .getConfigurationManager()
            .func_152596_g(entityPlayerMP.getGameProfile());
    }

    public static boolean onServer() {
        Minecraft mc = Minecraft.getMinecraft();
        return !mc.isSingleplayer() && !mc.isIntegratedServerRunning() && mc.thePlayer != null;
    }

    public static boolean isValidMinecraftUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,16}$");
    }
}
