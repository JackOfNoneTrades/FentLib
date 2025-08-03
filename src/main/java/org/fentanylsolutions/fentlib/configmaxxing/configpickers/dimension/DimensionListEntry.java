package org.fentanylsolutions.fentlib.configmaxxing.configpickers.dimension;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.CommonSelectionGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.ListEntry;
import org.fentanylsolutions.fentlib.util.DimensionUtil;

public class DimensionListEntry extends ListEntry {

    DimensionUtil.SimpleDimensionObj simpleDimensionObj;

    public DimensionListEntry(CommonSelectionGui selectionGui, DimensionUtil.SimpleDimensionObj simpleDimensionObj) {
        super(selectionGui);
        this.simpleDimensionObj = simpleDimensionObj;
    }

    @Override
    public void drawEntry(int index, int xpos, int ypos, int p_148279_4_, int p_148279_5_, Tessellator tessellator,
        int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
        int i2;
        String s = entryPoint.getDisplayStringStyled(simpleDimensionObj);
        i2 = this.mc.fontRenderer.getStringWidth(s);
        /*
         * if (i2 > 157) {
         * s = this.mc.fontRenderer.trimStringToWidth(s, 157 - this.mc.fontRenderer.getStringWidth("...")) + "...";
         * }
         */
        this.mc.fontRenderer.drawStringWithShadow(s, xpos + 2, ypos + 1, 16777215);

        /* Drawing the second line */
        List list = this.mc.fontRenderer.listFormattedStringToWidth(
            entryPoint.getDescription(simpleDimensionObj),
            this.selectionGui.availableEntriesListGui.width / 4 * 3);
        for (int j2 = 0; j2 < 2 && j2 < list.size(); ++j2) {
            this.mc.fontRenderer.drawStringWithShadow((String) list.get(j2), xpos + 2, ypos + 12 + 10 * j2, 8421504);
        }
    }

    @Override
    public Object getValue() {
        return simpleDimensionObj.getName();
    }

    @Override
    public String getDisplayString() {
        return entryPoint.getDisplayString(simpleDimensionObj);
    }
}
