package org.fentanylsolutions.fentlib.configmaxxing.configpickers;

import org.fentanylsolutions.fentlib.FentLib;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

public abstract class EntryPoint extends GuiConfigEntries.ArrayEntry {

    Class<? extends CommonSelectionGui> selectionGuiClass;
    public GuiConfig owningScreen;

    public EntryPoint(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement,
        Class<? extends CommonSelectionGui> selectionGuiClass) {
        super(owningScreen, owningEntryList, configElement);
        this.owningScreen = owningScreen;
        this.selectionGuiClass = selectionGuiClass;

        for (int i = 0; i < currentValues.length; i++) {
            Object o = currentValues[i];
            if (o == null) {
                if (configElement.getType() == ConfigGuiType.BOOLEAN) {
                    currentValues[i] = false;
                } else if (configElement.getType() == ConfigGuiType.INTEGER
                    || configElement.getType() == ConfigGuiType.DOUBLE) {
                        currentValues[i] = 0;
                    } else {
                        currentValues[i] = "None";
                    }
            }
        }

        ConfigGuiType configGuiType = configElement.getType();
        FentLib.LOG.debug(configGuiType.name());
        FentLib.LOG.debug(String.valueOf(configElement.isList()));
    }

    @Override
    public void valueButtonPressed(int slotIndex) {
        for (int i = 0; i < currentValues.length; i++) {
            Object o = currentValues[i];
            if (o == null) {
                if (configElement.getType() == ConfigGuiType.BOOLEAN) {
                    currentValues[i] = false;
                } else if (configElement.getType() == ConfigGuiType.INTEGER
                    || configElement.getType() == ConfigGuiType.DOUBLE) {
                        currentValues[i] = 0;
                    } else {
                        currentValues[i] = "None";
                    }
            }
        }
        if (configElement.isList()) {} else {

        }
    }

    /* Text displayed on the button that leads to the selection menu */
    public abstract String formatValue(Object val);

    /* Returns the string in the second row of the entry */
    public abstract String getDescription(Object val);

    /* The name as displayed in the list, with the right formatting like color */
    public abstract String getDisplayStringStyled(Object val);

    public abstract String getDisplayString(Object val);

    @Override
    public void updateValueButtonText() {
        if (configElement.isList()) {
            int i = 0;
            this.btnValue.displayString = "";
            for (Object o : currentValues) {
                this.btnValue.displayString += ", [" + formatValue(o) + "]";
                i++;
                if (i > 4) {
                    break;
                }
            }

            this.btnValue.displayString = this.btnValue.displayString.replaceFirst(", ", "");
        } else {
            this.btnValue.displayString = formatValue(configElement.get());
        }
    }
}
