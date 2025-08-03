package org.fentanylsolutions.fentlib.configmaxxing.configpickers.mob;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.util.ClassUtil;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

public class MobBlacklistEntryPoint extends EntryPoint {

    public MobBlacklistEntryPoint(GuiConfig owningScreen, GuiConfigEntries owningEntryList,
        IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement, MobBlacklistSelectionGui.class);
    }

    @Override
    public String formatValue(Object val) {
        return getDisplayStringStyled(val);
    }

    @Override
    public String getDescription(Object val) {
        String className = ClassUtil.getEntityClassByName((String) val);
        if (className != null) {
            return className;
        }
        return "";
    }

    @Override
    public String getDisplayStringStyled(Object val) {
        return getDisplayString(val);
    }

    @Override
    public String getDisplayString(Object val) {
        if (val == null) {
            return "None";
        }
        return (String) val;
    }
}
