package org.fentanylsolutions.fentlib.configmaxxing.configpickers.potionslim;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.potion.Potion;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.potion.PotionListEntry;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.potion.PotionSelectionGui;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

public class PotionSlimListEntry extends PotionListEntry {

    public PotionSlimListEntry(PotionSelectionGui potionSelectionGui, Potion potion) {
        super(potionSelectionGui, potion);
    }

    @Override
    public void drawEntry(int index, int xpos, int ypos, int p_148279_4_, int p_148279_5_, Tessellator tessellator,
        int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
        int xmod = -2;
        int ymod = -1;

        /* Drawing the colored liquid inside of the potion */
        GL11.glColor4f(r, g, b, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((PotionSelectionGui) selectionGui).potionOverlayResourceLocation,
            xpos + xmod,
            ypos + ymod,
            16,
            16,
            1,
            1);

        /* Drwawing the glass bottle */
        GL11.glColor4f(1, 1, 1, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((PotionSelectionGui) selectionGui).potionResourceLocation,
            xpos + xmod,
            ypos + ymod,
            16,
            16,
            1,
            1);

        /* Drawing the main text */
        int i2;
        String s = entryPoint.getDisplayStringStyled(potion);
        i2 = this.mc.fontRenderer.getStringWidth(s);
        if (i2 > 157) {
            s = this.mc.fontRenderer.trimStringToWidth(s, 157 - this.mc.fontRenderer.getStringWidth("...")) + "...";
        }
        this.mc.fontRenderer.drawStringWithShadow(s, xpos + 20, ypos + 3, 16777215);

        /* Drawing the second line */
        /*
         * List list = this.mc.fontRenderer.listFormattedStringToWidth(entryPoint.getDescription(potion), 157);
         * for (int j2 = 0; j2 < 2 && j2 < list.size(); ++ j2) {
         * this.mc.fontRenderer.drawStringWithShadow((String)list.get(j2), xpos + 32 + 2, ypos + 12 + 10 * j2, 8421504);
         * }
         */
    }
}
