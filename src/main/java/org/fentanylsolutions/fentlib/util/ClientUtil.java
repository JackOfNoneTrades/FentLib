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
        drawPlayerFace(rl, null, xPos, yPos, size, size, alpha);
    }

    public static void drawPlayerFace(ResourceLocation rl, float xPos, float yPos, float alpha) {
        drawPlayerFace(rl, null, xPos, yPos, 8, 8, alpha);
    }

    public static void drawPlayerFace(ResourceLocation rl, ResourceLocation fallback, float xPos, float yPos, int width,
        int height, float alpha) {
        ResourceLocation bound = bindTextureOrFallback(rl, fallback);
        if (bound == null) {
            return;
        }

        int texWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int texHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (texWidth <= 0 || texHeight <= 0) {
            texWidth = 64;
            texHeight = 64;
        }

        boolean legacyLayout = texWidth == texHeight * 2;
        float uScale = texWidth / 64.0F;
        float vScale = texHeight / (legacyLayout ? 32.0F : 64.0F);
        int sampleW = Math.max(1, Math.round(8.0F * uScale));
        int sampleH = Math.max(1, Math.round(8.0F * vScale));

        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        drawTexFloat(xPos, yPos, 8.0F * uScale, 8.0F * vScale, sampleW, sampleH, width, height, texWidth, texHeight);

        float hatU = 40.0F * uScale;
        float hatV = 8.0F * vScale;
        if (hatU + sampleW <= texWidth && hatV + sampleH <= texHeight) {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            drawTexFloat(xPos, yPos, hatU, hatV, sampleW, sampleH, width, height, texWidth, texHeight);
        }
    }

    private static ResourceLocation bindTextureOrFallback(ResourceLocation primary, ResourceLocation fallback) {
        if (bindTexture(primary)) {
            return primary;
        }
        if (fallback != null && !fallback.equals(primary) && bindTexture(fallback)) {
            return fallback;
        }
        return null;
    }

    private static boolean bindTexture(ResourceLocation location) {
        if (location == null) {
            return false;
        }

        try {
            Minecraft mc = FentLib.varInstanceClient != null && FentLib.varInstanceClient.minecraftRef != null
                ? FentLib.varInstanceClient.minecraftRef
                : Minecraft.getMinecraft();
            if (mc == null || mc.getTextureManager() == null) {
                return false;
            }
            mc.getTextureManager()
                .bindTexture(location);
            return true;
        } catch (RuntimeException e) {
            FentLib.debug("Failed to bind player face texture '" + location + "': " + e.getMessage());
            return false;
        }
    }

    public void copyToCliboard(String text) {
        StringSelection stringselection = new StringSelection(text);
        Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(stringselection, null);
    }
}
