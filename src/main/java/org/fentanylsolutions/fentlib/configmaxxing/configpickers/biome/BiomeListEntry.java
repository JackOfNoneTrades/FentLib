package org.fentanylsolutions.fentlib.configmaxxing.configpickers.biome;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.CommonSelectionGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.ListEntry;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

public class BiomeListEntry extends ListEntry {

    protected final BiomeGenBase biomeGenBase;

    protected final float r_grass;
    protected final float g_grass;
    protected final float b_grass;

    protected final float r_leaf;
    protected final float g_leaf;
    protected final float b_leaf;

    protected final float r_water;
    protected final float g_water;
    protected final float b_water;

    public BiomeListEntry(CommonSelectionGui selectionGui, BiomeGenBase biomeGenBase) {
        super(selectionGui);

        this.biomeGenBase = biomeGenBase;

        int grassColor = biomeGenBase.getBiomeGrassColor(0, 0, 0); // getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(1,
                                                                   // 0));
        r_grass = ((grassColor >> 16) & 255) / (float) 255;
        g_grass = ((grassColor >> 8) & 255) / (float) 255;
        b_grass = ((grassColor) & 255) / (float) 255;

        int leafColor = biomeGenBase.getBiomeFoliageColor(0, 0, 0);
        r_leaf = ((leafColor >> 16) & 255) / (float) 255;
        g_leaf = ((leafColor >> 8) & 255) / (float) 255;
        b_leaf = ((leafColor) & 255) / (float) 255;

        int waterColor = biomeGenBase.getWaterColorMultiplier();
        r_water = ((waterColor >> 16) & 255) / (float) 255;
        g_water = ((waterColor >> 8) & 255) / (float) 255;
        b_water = ((waterColor) & 255) / (float) 255;
    }

    @Override
    public void drawEntry(int index, int xpos, int ypos, int p_148279_4_, int p_148279_5_, Tessellator tessellator,
        int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
        GL11.glColor4f(r_grass, g_grass, b_grass, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((BiomeSelectionGui) selectionGui).grassResourceLocation,
            xpos,
            ypos,
            32,
            32,
            1,
            1);

        GL11.glColor4f(r_leaf, g_leaf, b_leaf, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((BiomeSelectionGui) selectionGui).leafResourceLocation,
            xpos + 32,
            ypos,
            32,
            32,
            1,
            1);

        GL11.glColor4f(r_water, g_water, b_water, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            ((BiomeSelectionGui) selectionGui).waterResourceLocation,
            xpos + 64,
            ypos,
            32,
            32,
            1,
            0.03125);

        GL11.glColor4f(1, 1, 1, 1);

        /* Drawing the main text */
        int i2;
        String s = entryPoint.getDisplayStringStyled(biomeGenBase);
        i2 = this.mc.fontRenderer.getStringWidth(s);
        /*
         * if (i2 > 157) {
         * s = this.mc.fontRenderer.trimStringToWidth(s, 157 - this.mc.fontRenderer.getStringWidth("...")) + "...";
         * }
         */
        this.mc.fontRenderer.drawStringWithShadow(s, xpos + 96 + 2, ypos + 1, 16777215);

        /* Drawing the second line */
        List list = this.mc.fontRenderer.listFormattedStringToWidth(
            entryPoint.getDescription(biomeGenBase),
            this.selectionGui.availableEntriesListGui.width / 4 * 3);
        for (int j2 = 0; j2 < 2 && j2 < list.size(); ++j2) {
            this.mc.fontRenderer
                .drawStringWithShadow((String) list.get(j2), xpos + 96 + 2, ypos + 12 + 10 * j2, 8421504);
        }
    }

    @Override
    public Object getValue() {
        return biomeGenBase.biomeName;
    }

    @Override
    public String getDisplayString() {
        return entryPoint.getDisplayString(biomeGenBase);
    }
}
