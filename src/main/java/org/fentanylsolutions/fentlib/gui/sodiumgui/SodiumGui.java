package org.fentanylsolutions.fentlib.gui.sodiumgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SodiumGui {

    public static final int ACCENT_COLOR = SodiumGuiTheme.DEFAULT_ACCENT_COLOR;
    public static final int ROW_IDLE_COLOR = SodiumGuiTheme.ROW_IDLE_COLOR;
    public static final int ROW_HOVER_COLOR = SodiumGuiTheme.ROW_HOVER_COLOR;
    public static final int ROW_SELECTED_COLOR = SodiumGuiTheme.ROW_SELECTED_COLOR;
    public static final int ROW_DISABLED_COLOR = SodiumGuiTheme.ROW_DISABLED_COLOR;
    public static final int SCROLLBAR_TRACK_COLOR = SodiumGuiTheme.SCROLLBAR_TRACK_COLOR;
    public static final int SCROLLBAR_THUMB_COLOR = SodiumGuiTheme.DEFAULT.getScrollbarThumbColor();
    public static final int ROW_HEIGHT = 18;
    public static final int ROW_GAP = 1;
    public static final int ROW_STEP = ROW_HEIGHT + ROW_GAP;
    public static final int SCROLLBAR_WIDTH = 6;
    public static final int SCROLLBAR_GAP = 2;
    public static final int SCROLLBAR_AREA_WIDTH = SCROLLBAR_WIDTH + SCROLLBAR_GAP;
    public static final float WHEEL_SCROLL_PIXELS = 16.0F;

    private static final String ELLIPSIS = "...";

    private SodiumGui() {}

    public static void drawFlatBox(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }

    public static void drawRectOutline(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + 1, color);
        Gui.drawRect(x, y + height - 1, x + width, y + height, color);
        Gui.drawRect(x, y, x + 1, y + height, color);
        Gui.drawRect(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawScrollbar(int x, int y, int width, int height, int contentHeight, float scroll) {
        drawScrollbar(SodiumGuiTheme.getDefault(), x, y, width, height, contentHeight, scroll);
    }

    public static void drawScrollbar(SodiumGuiTheme theme, int x, int y, int width, int height, int contentHeight,
        float scroll) {
        ScrollbarMetrics metrics = getScrollbarMetrics(y, height, contentHeight, scroll);
        if (metrics == null) {
            return;
        }

        SodiumGuiTheme resolvedTheme = theme == null ? SodiumGuiTheme.getDefault() : theme;
        drawFlatBox(x, y, width, height, resolvedTheme.getScrollbarTrackColor());
        drawFlatBox(x, metrics.getThumbY(), width, metrics.getThumbHeight(), resolvedTheme.getScrollbarThumbColor());
    }

    public static SodiumGuiTheme getDefaultTheme() {
        return SodiumGuiTheme.getDefault();
    }

    public static int getDefaultAccentColor() {
        return SodiumGuiTheme.getDefaultAccentColor();
    }

    public static ScrollbarMetrics getScrollbarMetrics(int y, int height, int contentHeight, float scroll) {
        if (contentHeight <= height || height <= 0) {
            return null;
        }

        int thumbHeight = Math.max(12, Math.round(height * (height / (float) contentHeight)));
        thumbHeight = Math.min(height, thumbHeight);
        float maxScroll = getMaxScroll(contentHeight, height);
        int travel = Math.max(0, height - thumbHeight);
        int thumbY = y + Math.round(travel * (scroll / maxScroll));
        return new ScrollbarMetrics(thumbY, thumbHeight);
    }

    public static float scrollFromScrollbarDrag(int y, int height, int contentHeight, float dragOffset, int mouseY) {
        ScrollbarMetrics metrics = getScrollbarMetrics(y, height, contentHeight, 0.0F);
        if (metrics == null) {
            return 0.0F;
        }

        float maxScroll = getMaxScroll(contentHeight, height);
        int travel = Math.max(1, height - metrics.getThumbHeight());
        float position = (mouseY - y - dragOffset) / (float) travel;
        return clamp(position * maxScroll, 0.0F, maxScroll);
    }

    public static int getVisibleRows(int height) {
        return Math.max(1, height / ROW_STEP + 1);
    }

    public static int getContentHeight(int rowCount) {
        if (rowCount <= 0) {
            return 0;
        }
        return rowCount * ROW_STEP - ROW_GAP;
    }

    public static float getMaxScroll(int contentHeight, int viewportHeight) {
        return Math.max(0.0F, contentHeight - viewportHeight);
    }

    public static String ellipsize(FontRenderer fontRenderer, String text, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return "";
        }
        if (fontRenderer.getStringWidth(text) <= maxWidth) {
            return text;
        }

        int ellipsisWidth = fontRenderer.getStringWidth(ELLIPSIS);
        if (ellipsisWidth >= maxWidth) {
            return fontRenderer.trimStringToWidth(text, maxWidth);
        }

        String trimmed = fontRenderer.trimStringToWidth(text, maxWidth - ellipsisWidth)
            .trim();
        while (!trimmed.isEmpty() && fontRenderer.getStringWidth(trimmed + ELLIPSIS) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ELLIPSIS;
    }

    public static boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void beginScissor(Minecraft minecraft, int x, int y, int width, int height) {
        ScaledResolution resolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int scaleFactor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            x * scaleFactor,
            (resolution.getScaledHeight() - y - height) * scaleFactor,
            width * scaleFactor,
            height * scaleFactor);
    }

    public static void endScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static final class ScrollbarMetrics {

        private final int thumbY;
        private final int thumbHeight;

        private ScrollbarMetrics(int thumbY, int thumbHeight) {
            this.thumbY = thumbY;
            this.thumbHeight = thumbHeight;
        }

        public int getThumbY() {
            return this.thumbY;
        }

        public int getThumbHeight() {
            return this.thumbHeight;
        }
    }
}
