package org.fentanylsolutions.fentlib.configmaxxing.configpickers.potionslim;

import net.minecraft.potion.Potion;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.potion.PotionSelectionGui;

public class PotionSlimSelectionGui extends PotionSelectionGui {

    public PotionSlimSelectionGui(int index, EntryPoint entryPoint) {
        super(index, entryPoint);
    }

    @Override
    public void initGui() {
        super.initGui();

        int entryHeight = 18;

        /* The list element */
        this.availableEntriesListGui.setSlotXBoundsFromLeft(this.width / 2);
        this.availableEntriesListGui.registerScrollButtons(7, 8);
    }

    @Override
    public void populateEntries() {
        for (Potion p : Potion.potionTypes) {
            if (p != null) {
                PotionSlimListEntry entry = new PotionSlimListEntry(this, p);
                this.availableEntries.add(entry);
            }
        }
    }
}
