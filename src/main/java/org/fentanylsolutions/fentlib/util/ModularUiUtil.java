package org.fentanylsolutions.fentlib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.fentanylsolutions.fentlib.mixininterfaces.IModularPanel;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

public class ModularUiUtil {

    public static IWidget getWidgetByDebugName(IWidget widget, String name) {
        if (widget == null) {
            return null;
        }

        String debugName = widget.getName();
        if (debugName != null && debugName.equals(name)) {
            return widget;
        }

        for (IWidget child : widget.getChildren()) {
            IWidget found = getWidgetByDebugName(child, name);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    public static ModularPanel getPanelByDebugName(ModularScreen screen, String name) {
        for (ModularPanel p : screen.getPanelManager()
            .getOpenPanels()) {
            if (p.getName()
                .equals(name)) {
                return p;
            }
        }
        return null;
    }

    public static ModularPanel createMasterPanelWithParent(String name, int width, int height, GuiScreen parentScreen) {
        ModularPanel panel = new ModularPanel(name).size(width, height);
        ((IModularPanel) panel).setGuiScreen(parentScreen);
        return panel;
    }

    public static class ModularScreenWithParent extends ModularScreen {

        private GuiScreen parentScreen;
        private int dimColor;

        public ModularScreenWithParent(@org.jetbrains.annotations.NotNull ModularPanel mainPanel,
            GuiScreen parentScreen, float dimStrength) {
            super(mainPanel);
            this.parentScreen = parentScreen;
            dimStrength = Math.max(0f, Math.min(1f, dimStrength));
            int alpha = (int) (dimStrength * 255);
            dimColor = (alpha << 24) | 0x000000;
        }

        @Override
        public void drawScreen() {
            parentScreen.drawScreen(0, 0, 0);
            Gui.drawRect(0, 0, this.getScreenArea().width, this.getScreenArea().height, dimColor);
            super.drawScreen();
        }

        @Override
        public void onResize(int width, int height) {
            parentScreen.setWorldAndResolution(Minecraft.getMinecraft(), width, height);
            super.onResize(width, height);
        }
    }
}
