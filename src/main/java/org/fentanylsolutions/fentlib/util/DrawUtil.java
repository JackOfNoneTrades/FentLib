package org.fentanylsolutions.fentlib.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class DrawUtil {

    public static void drawModalRectWithCustomSizedTexture(Minecraft mc, ResourceLocation rl, int x, int y,
        int drawWidth, int drawHeight, double textureWidthPercentage, double textureHeightPercentage) {
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

    public static void drawItemStack(ItemStack p_146982_1_, int p_146982_2_, int p_146982_3_, String p_146982_4_) {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 0.0F);

        // GL11.glScalef(2.0F, 2.0F, 0.0F);

        // GL11.glTranslatef(-13, -30, 0);

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        // GL11.glTranslatef(0.0F, 0.0F, 32.0F);
        FontRenderer font = null;
        if (p_146982_1_ != null) font = p_146982_1_.getItem()
            .getFontRenderer(p_146982_1_);
        if (font == null) font = Minecraft.getMinecraft().fontRenderer;
        RenderItem.getInstance()
            .renderItemAndEffectIntoGUI(
                font,
                Minecraft.getMinecraft()
                    .getTextureManager(),
                p_146982_1_,
                p_146982_2_,
                p_146982_3_);
        // RenderItem.getInstance().renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(),
        // p_146982_1_, p_146982_2_, p_146982_3_, p_146982_4_);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }
}
