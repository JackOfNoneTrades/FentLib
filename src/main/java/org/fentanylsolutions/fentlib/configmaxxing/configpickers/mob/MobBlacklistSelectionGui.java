package org.fentanylsolutions.fentlib.configmaxxing.configpickers.mob;

import net.minecraft.entity.EntityList;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;

public class MobBlacklistSelectionGui extends MobSelectionGui {

    public MobBlacklistSelectionGui(int index, EntryPoint entryPoint) {
        super(index, entryPoint);
    }

    @Override
    public void populateEntries() {
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            MobListEntry entry = new MobListEntry(this, (String) e);
            this.availableEntries.add(entry);
        }
    }
}
