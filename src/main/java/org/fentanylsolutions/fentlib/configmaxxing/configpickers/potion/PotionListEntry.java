package org.fentanylsolutions.fentlib.configmaxxing.configpickers.potion;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.potion.Potion;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.ListEntry;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

public class PotionListEntry extends ListEntry {

    protected final Potion potion;

    protected final float r;
    protected final float g;
    protected final float b;

    public PotionListEntry(PotionSelectionGui potionSelectionGui, Potion potion) {
        super(potionSelectionGui);
        this.potion = potion;

        /* Splitting the potion color int into RGB floats */
        int color = potion.getLiquidColor();
        r = ((color >> 16) & 255) / (float) 255;
        g = ((color >> 8) & 255) / (float) 255;
        b = ((color) & 255) / (float) 255;
    }

    /* Draws everything */
    public void drawEntry(int index, int xpos, int ypos, int p_148279_4_, int p_148279_5_, Tessellator tessellator,
        int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
        /* Drawing the colored liquid inside of the potion */
        GL11.glColor4f(r, g, b, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((PotionSelectionGui) selectionGui).potionOverlayResourceLocation,
            xpos,
            ypos,
            32,
            32,
            1,
            1);

        /* Drwawing the glass bottle */
        GL11.glColor4f(1, 1, 1, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((PotionSelectionGui) selectionGui).potionResourceLocation,
            xpos,
            ypos,
            32,
            32,
            1,
            1);

        /* Drawing the main text */
        int i2;
        String s = entryPoint.getDisplayStringStyled(potion);
        i2 = this.mc.fontRenderer.getStringWidth(s);
        if (i2 > 157) {
            s = this.mc.fontRenderer.trimStringToWidth(s, 157 - this.mc.fontRenderer.getStringWidth("...")) + "...";
        }
        this.mc.fontRenderer.drawStringWithShadow(s, xpos + 32 + 2, ypos + 1, 16777215);

        /* Drawing the second line */
        List list = this.mc.fontRenderer.listFormattedStringToWidth(entryPoint.getDescription(potion), 157);
        for (int j2 = 0; j2 < 2 && j2 < list.size(); ++j2) {
            this.mc.fontRenderer
                .drawStringWithShadow((String) list.get(j2), xpos + 32 + 2, ypos + 12 + 10 * j2, 8421504);
        }
    }

    /* Returns the value of the selection */
    @Override
    public Object getValue() {
        return potion.getName();
    }

    @Override
    public String getDisplayString() {
        return entryPoint.getDisplayString(potion);
    }
}
