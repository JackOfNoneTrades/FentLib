package org.fentanylsolutions.fentlib.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import org.fentanylsolutions.fentlib.Config;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PanoramaOverlayRenderer {

    private static final GradientRenderer GRADIENT_RENDERER = new GradientRenderer();

    private PanoramaOverlayRenderer() {}

    /**
     * Draws the vanilla title screen's white-to-black panorama gradient when enabled.
     *
     * @return {@code true} when the configured overlay was drawn
     */
    public static boolean drawMilkyPanorama(Minecraft mc) {
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        return drawMilkyPanorama(resolution.getScaledWidth(), resolution.getScaledHeight());
    }

    /**
     * Draws the vanilla title screen's white-to-black panorama gradient when enabled.
     *
     * @return {@code true} when the configured overlay was drawn
     */
    public static boolean drawMilkyPanorama(int width, int height) {
        if (!Config.milkyPanorama) {
            return false;
        }
        GRADIENT_RENDERER.draw(width, height);
        return true;
    }

    private static final class GradientRenderer extends Gui {

        private void draw(int width, int height) {
            drawGradientRect(0, 0, width, height, 0x80FFFFFF, 0x00FFFFFF);
            drawGradientRect(0, 0, width, height, 0x00000000, 0x80000000);
        }
    }
}
