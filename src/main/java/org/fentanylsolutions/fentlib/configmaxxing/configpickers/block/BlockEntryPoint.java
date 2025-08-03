package org.fentanylsolutions.fentlib.configmaxxing.configpickers.block;

import net.minecraft.block.Block;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.util.BlockUtil;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

public class BlockEntryPoint extends EntryPoint {

    public BlockEntryPoint(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement, BlockSelectionGui.class);
    }

    @Override
    public String formatValue(Object val) {
        return getDisplayStringStyled(BlockUtil.getBlockByName((String) val));
    }

    @Override
    public String getDescription(Object val) {
        Block block = (Block) val;
        if (block == null) {
            return "";
        }
        return block.getUnlocalizedName();
    }

    @Override
    public String getDisplayStringStyled(Object val) {
        return getDisplayString(val);
    }

    @Override
    public String getDisplayString(Object val) {
        Block block = (Block) val;
        if (block == null) {
            return "None";
        }
        return block.getLocalizedName();
    }
}
