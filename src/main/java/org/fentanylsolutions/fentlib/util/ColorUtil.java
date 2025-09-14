package org.fentanylsolutions.fentlib.util;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {

    public static enum Color {
        DARKRED,
        RED,
        GOLD,
        YELLOW,
        DARKGREEN,
        GREEN,
        AQUA,
        DARKAQUA,
        DARKBLUE,
        BLUE,
        LIGHTPURPLE,
        DARKPURPLE,
        WHITE,
        GREY,
        DARKGREY,
        BLACK,

        RESET
    }

    public static String colorCode(Color color) {
        Map<Color, String> colorMap = new HashMap<Color, String>() {

            {
                put(Color.DARKRED, "4");
                put(Color.RED, "c");
                put(Color.GOLD, "6");
                put(Color.YELLOW, "e");
                put(Color.DARKGREEN, "2");
                put(Color.GREEN, "a");
                put(Color.AQUA, "b");
                put(Color.DARKAQUA, "3");
                put(Color.DARKBLUE, "1");
                put(Color.BLUE, "9");
                put(Color.LIGHTPURPLE, "d");
                put(Color.DARKPURPLE, "5");
                put(Color.WHITE, "f");
                put(Color.GREY, "7");
                put(Color.DARKGREY, "8");
                put(Color.BLACK, "0");
                put(Color.RESET, "r");

            }
        };
        return (char) 167 + colorMap.get(color);
    }
}
