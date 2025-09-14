package org.fentanylsolutions.fentlib.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

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
}
