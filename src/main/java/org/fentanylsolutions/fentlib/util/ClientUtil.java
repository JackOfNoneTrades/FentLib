package org.fentanylsolutions.fentlib.util;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.compat.LoadedMods;
import org.lwjgl.opengl.GL11;

public class ClientUtil {

    public static void drawModalRectWithCustomSizedTexture(Minecraft mc, ResourceLocation rl, int x, int y,
        int drawWidth, int drawHeight, int textureWidthPercentage, int textureHeightPercentage) {
        mc.getTextureManager()
            .bindTexture(rl);

        GL11.glEnable(GL11.GL_BLEND);

        Tessellator tessellator = Tessellator.instance;
        tessellator.setTranslation(0, 0, 0);
        tessellator.startDrawingQuads();

        /*
         * Ok so basically, the texture is a bit smaller than the size of the image itself.
         * These values are passed to the tesselator and tell it how much of the image it should actually draw,
         * in a 0 to 1 double (like a percentage).
         */
        // double textureWidth = 0.96875;
        // double textureHeight = 0.6484375;

        tessellator.addVertexWithUV(x, (y + drawHeight), 0.0D, 0, textureHeightPercentage);
        tessellator
            .addVertexWithUV((x + drawWidth), (y + drawHeight), 0.0D, textureWidthPercentage, textureHeightPercentage);
        tessellator.addVertexWithUV((x + drawWidth), y, 0.0D, textureWidthPercentage, 0);
        tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
        tessellator.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawTexFloat(float x, float y, float u, float v, int uWidth, int vHeight, int width, int height,
        float tileWidth, float tileHeight) {
        float f4 = 1.0F / tileWidth;
        float f5 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0.0D, u * f4, (v + (float) vHeight) * f5);
        tessellator.addVertexWithUV(x + width, y + height, 0.0D, (u + (float) uWidth) * f4, (v + (float) vHeight) * f5);
        tessellator.addVertexWithUV(x + width, y, 0.0D, (u + (float) uWidth) * f4, v * f5);
        tessellator.addVertexWithUV(x, y, 0.0D, u * f4, v * f5);
        tessellator.draw();
    }

    public static boolean useNewSkinFormat() {
        return LoadedMods.skinPortLoaded || LoadedMods.simpleSkinBackportLoaded; // || Config.forceNewSkinCompat;
    }

    public static void drawPlayerFace(ResourceLocation rl, float xPos, float yPos, float alpha, int size) {
        if (rl != null) {
            FentLib.varInstanceClient.minecraftRef.getTextureManager()
                .bindTexture(rl);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

            if (useNewSkinFormat()) {
                // Draw base face: (8, 8) to (16, 16)
                drawTexFloat(xPos, yPos, 8, 8, 8, 8, size, size, 64.0F, 64.0F);

                // Draw overlay (hat layer): (40, 8) to (48, 16)
                // Render it with alpha so it looks like a layer
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                drawTexFloat(xPos, yPos, 40, 8, 8, 8, size, size, 64.0F, 64.0F);
            } else {
                // Old skin format
                drawTexFloat(xPos, yPos, 8, 14, 8, 18, size, size, 64.0F, 64.0F);
            }
        }
    }

    public void copyToCliboard(String text) {
        StringSelection stringselection = new StringSelection(text);
        Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(stringselection, null);
    }
}
