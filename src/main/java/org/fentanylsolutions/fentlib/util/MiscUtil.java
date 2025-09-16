package org.fentanylsolutions.fentlib.util;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

public class MiscUtil {

    public static boolean isServer() {
        return FMLLaunchHandler.side()
            .isServer();
    }

    public static boolean isOp(EntityPlayerMP entityPlayerMP) {
        // func_152596_g: canSendCommands
        return FMLCommonHandler.instance()
            .getMinecraftServerInstance()
            .getConfigurationManager()
            .func_152596_g(entityPlayerMP.getGameProfile());
    }

    public enum Side {
        CLIENT,
        SERVER,
        BOTH;
    }

    public enum PermissionLevel {

        ALL(0), // Everyone
        OP(1), // Operator
        OP_2(2), // Higher-level OP
        OP_3(3),
        OP_4(4); // Full permissions (e.g., stop, op, etc.)

        private final int level;

        PermissionLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public static Object[] addAtIndex(Object[] arr, int index, Object val) {
        if (index < 0 || index > arr.length) {
            return null;
        }
        Object[] res = new Object[arr.length + 1];
        for (int i = 0, j = 0; i < res.length; i++) {
            if (i == index) {
                res[i] = val;
            } else {
                res[i] = arr[j];
                j++;
            }
        }
        return res;
    }

    public static Object[] removeAtIndex(Object[] arr, int index) {
        if (index < 0 || index >= arr.length) {
            return null;
        }
        Object[] res = new Object[arr.length - 1];
        for (int i = 0, j = 0; i < arr.length; i++) {
            if (i != index) {
                res[j] = arr[i];
                j++;
            }
        }
        return res;
    }
}
