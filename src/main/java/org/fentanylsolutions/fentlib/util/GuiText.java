package org.fentanylsolutions.fentlib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GuiText {

    private static final String ELLIPSIS = "...";
    private static final int DEFAULT_BUTTON_TEXT_PADDING_PX = 8;

    private GuiText() {}

    public static String tr(String key) {
        return StatCollector.translateToLocal(key);
    }

    public static String tr(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }

    public static IKey key(String key) {
        return IKey.str(tr(key));
    }

    public static IKey key(String key, Object... args) {
        return IKey.str(tr(key, args));
    }

    public static ButtonWidget<?> fitButtonLabel(ButtonWidget<?> button, int buttonWidthPx, String key,
        Object... args) {
        return fitButtonLabelMaxWidth(button, Math.max(0, buttonWidthPx - DEFAULT_BUTTON_TEXT_PADDING_PX), key, args);
    }

    public static ButtonWidget<?> fitButtonLabelMaxWidth(ButtonWidget<?> button, int maxTextWidthPx, String key,
        Object... args) {
        String fullText = tr(key, args);
        String displayText = ellipsizeToPixelWidth(fullText, maxTextWidthPx);
        button.overlay(IKey.str(displayText));
        if (!displayText.equals(fullText)) {
            button.addTooltipLine(fullText);
        }
        return button;
    }

    public static String ellipsizeToPixelWidth(String text, int maxWidthPx) {
        if (text == null) {
            return "";
        }
        if (maxWidthPx <= 0) {
            return "";
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.fontRenderer == null) {
            return text.length() > 18 ? text.substring(0, 15) + ELLIPSIS : text;
        }

        if (mc.fontRenderer.getStringWidth(text) <= maxWidthPx) {
            return text;
        }

        int ellipsisWidth = mc.fontRenderer.getStringWidth(ELLIPSIS);
        if (ellipsisWidth >= maxWidthPx) {
            return ELLIPSIS;
        }

        String trimmed = mc.fontRenderer.trimStringToWidth(text, Math.max(0, maxWidthPx - ellipsisWidth));
        while (!trimmed.isEmpty() && mc.fontRenderer.getStringWidth(trimmed) + ellipsisWidth > maxWidthPx) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? ELLIPSIS : trimmed + ELLIPSIS;
    }
}
