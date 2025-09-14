package org.fentanylsolutions.fentlib.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class MiscUtil {

    public static boolean isServer() {
        return FMLCommonHandler.instance()
            .getSide() == Side.SERVER;
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
