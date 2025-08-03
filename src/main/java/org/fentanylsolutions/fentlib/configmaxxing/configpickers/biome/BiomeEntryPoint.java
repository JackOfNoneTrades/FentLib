package org.fentanylsolutions.fentlib.configmaxxing.configpickers.biome;

import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.util.BiomeUtil;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

public class BiomeEntryPoint extends EntryPoint {

    public BiomeEntryPoint(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement, BiomeSelectionGui.class);
    }

    @Override
    public String formatValue(Object val) {
        return getDisplayStringStyled(BiomeUtil.getBiomeGenBaseByName((String) val));
    }

    @Override
    public String getDescription(Object val) {
        BiomeGenBase biomeGenBase = (BiomeGenBase) val;
        if (biomeGenBase == null) {
            return "";
        }
        return "Temp: " + biomeGenBase.temperature
            + ", rainfall: "
            + biomeGenBase.rainfall
            + ", snow: "
            + biomeGenBase.getEnableSnow();
    }

    @Override
    public String getDisplayStringStyled(Object val) {
        return getDisplayString(val);
    }

    @Override
    public String getDisplayString(Object val) {
        BiomeGenBase biomeGenBase = (BiomeGenBase) val;
        if (biomeGenBase == null) {
            return "None";
        }
        return biomeGenBase.biomeName;
    }
}
