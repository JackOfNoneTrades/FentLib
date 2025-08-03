package org.fentanylsolutions.fentlib.configmaxxing.configpickers.mob;

import net.minecraft.client.renderer.Tessellator;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.ListEntry;

public class MobListEntry extends ListEntry {

    protected final String mobString;

    public MobListEntry(MobSelectionGui mobSelectionGui, String mobString) {
        super(mobSelectionGui);
        this.mobString = mobString;
    }

    public void drawEntry(int p_148279_1_, int p_148279_2_, int p_148279_3_, int p_148279_4_, int p_148279_5_,
        Tessellator p_148279_6_, int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
        int i2;
        String s = entryPoint.getDisplayStringStyled(mobString);
        i2 = this.mc.fontRenderer.getStringWidth(s);

        if (i2 > 157) {
            s = this.mc.fontRenderer.trimStringToWidth(s, 157 - this.mc.fontRenderer.getStringWidth("...")) + "...";
        }

        this.mc.fontRenderer.drawStringWithShadow(s, p_148279_2_ + 2, p_148279_3_ + 1, 16777215);

        java.util.List list = this.mc.fontRenderer
            .listFormattedStringToWidth(entryPoint.getDescription(mobString), 157);
        for (int j2 = 0; j2 < 2 && j2 < list.size(); ++j2) {
            this.mc.fontRenderer
                .drawStringWithShadow((String) list.get(j2), p_148279_2_ + 2, p_148279_3_ + 12 + 10 * j2, 8421504);
        }
    }

    @Override
    public Object getValue() {
        return mobString;
    }

    @Override
    public String getDisplayString() {
        return selectionGui.entryPoint.getDisplayString(mobString);
    }
}
