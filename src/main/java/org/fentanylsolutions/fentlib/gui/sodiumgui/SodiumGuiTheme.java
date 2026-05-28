package org.fentanylsolutions.fentlib.gui.sodiumgui;

import org.fentanylsolutions.fentlib.Config;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SodiumGuiTheme {

    public static final int DEFAULT_ACCENT_COLOR = Config.DEFAULT_SODIUM_GUI_ACCENT_COLOR;
    public static final int ROW_IDLE_COLOR = 0x90000000;
    public static final int ROW_HOVER_COLOR = 0xE0000000;
    public static final int ROW_SELECTED_COLOR = 0xB0000000;
    public static final int ROW_DISABLED_COLOR = 0x60000000;
    public static final int SCROLLBAR_TRACK_COLOR = 0x55000000;
    public static final SodiumGuiTheme DEFAULT = new SodiumGuiTheme(
        DEFAULT_ACCENT_COLOR,
        ROW_IDLE_COLOR,
        ROW_HOVER_COLOR,
        ROW_SELECTED_COLOR,
        ROW_DISABLED_COLOR,
        SCROLLBAR_TRACK_COLOR);

    private final int accentColor;
    private final int rowIdleColor;
    private final int rowHoverColor;
    private final int rowSelectedColor;
    private final int rowDisabledColor;
    private final int scrollbarTrackColor;
    private static SodiumGuiTheme cachedDefault;
    private static int cachedDefaultAccentColor;

    private SodiumGuiTheme(int accentColor, int rowIdleColor, int rowHoverColor, int rowSelectedColor,
        int rowDisabledColor, int scrollbarTrackColor) {
        this.accentColor = accentColor;
        this.rowIdleColor = rowIdleColor;
        this.rowHoverColor = rowHoverColor;
        this.rowSelectedColor = rowSelectedColor;
        this.rowDisabledColor = rowDisabledColor;
        this.scrollbarTrackColor = scrollbarTrackColor;
    }

    public static SodiumGuiTheme withAccent(int accentColor) {
        return new SodiumGuiTheme(
            ensureAlpha(accentColor),
            ROW_IDLE_COLOR,
            ROW_HOVER_COLOR,
            ROW_SELECTED_COLOR,
            ROW_DISABLED_COLOR,
            SCROLLBAR_TRACK_COLOR);
    }

    public static SodiumGuiTheme create(int accentColor, int rowIdleColor, int rowHoverColor, int rowSelectedColor,
        int rowDisabledColor, int scrollbarTrackColor) {
        return new SodiumGuiTheme(
            ensureAlpha(accentColor),
            rowIdleColor,
            rowHoverColor,
            rowSelectedColor,
            rowDisabledColor,
            scrollbarTrackColor);
    }

    public static SodiumGuiTheme getDefault() {
        int accentColor = getDefaultAccentColor();
        if (cachedDefault == null || cachedDefaultAccentColor != accentColor) {
            cachedDefault = withAccent(accentColor);
            cachedDefaultAccentColor = accentColor;
        }
        return cachedDefault;
    }

    public static int getDefaultAccentColor() {
        return Config.sodiumGuiAccentColor;
    }

    private static int ensureAlpha(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    public int getAccentColor() {
        return this.accentColor;
    }

    public int getRowIdleColor() {
        return this.rowIdleColor;
    }

    public int getRowHoverColor() {
        return this.rowHoverColor;
    }

    public int getRowSelectedColor() {
        return this.rowSelectedColor;
    }

    public int getRowDisabledColor() {
        return this.rowDisabledColor;
    }

    public int getScrollbarTrackColor() {
        return this.scrollbarTrackColor;
    }

    public int getScrollbarThumbColor() {
        return withAlpha(this.accentColor, 0xD0);
    }

    public int withAlpha(int alpha) {
        return withAlpha(this.accentColor, alpha);
    }

    public static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}
