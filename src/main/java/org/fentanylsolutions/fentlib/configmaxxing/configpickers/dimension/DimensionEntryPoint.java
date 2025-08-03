package org.fentanylsolutions.fentlib.configmaxxing.configpickers.dimension;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.util.DimensionUtil;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

public class DimensionEntryPoint extends EntryPoint {

    public DimensionEntryPoint(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement, DimensionSelectionGui.class);
    }

    @Override
    public String formatValue(Object val) {
        return getDisplayStringStyled(DimensionUtil.getSimpleDimensionObj((String) val));
    }

    @Override
    public String getDescription(Object val) {
        DimensionUtil.SimpleDimensionObj simpleDimensionObj = (DimensionUtil.SimpleDimensionObj) val;
        if (simpleDimensionObj == null) {
            return "";
        }
        if (simpleDimensionObj.getLeaveMessage() != null) {
            return "\"" + simpleDimensionObj.getLeaveMessage() + "\"";
        } else {
            return "";
        }
    }

    @Override
    public String getDisplayStringStyled(Object val) {
        return getDisplayString(val);
    }

    @Override
    public String getDisplayString(Object val) {
        DimensionUtil.SimpleDimensionObj simpleDimensionObj = (DimensionUtil.SimpleDimensionObj) val;
        if (simpleDimensionObj == null) {
            return "None";
        }
        return simpleDimensionObj.getName();
    }
}
