package org.fentanylsolutions.fentlib.configmaxxing.configpickers;

import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class CommonSelectionGui extends GuiScreen {

    public EntryPoint entryPoint;
    public int lastKeyboardEventKey = -1;
    public int index;
    Object originalValue;
    public ArrayList<ListEntry> availableEntries;
    public ArrayList<ListEntry> hiddenList;
    public String title = "Changeme";
    public AvailableEntriesListGui availableEntriesListGui;

    public SearchBox searchBox;
    /* Counts added entries so they can get readded back in order when the search term changes */
    private int addCounter = 0;

    public CommonSelectionGui(int index, EntryPoint entryPoint) {
        this.mc = Minecraft.getMinecraft();

        this.index = index;
    }

}
