package org.fentanylsolutions.fentlib.configmaxxing.configpickers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

public abstract class ListEntry {

    public CommonSelectionGui selectionGui;
    public EntryPoint entryPoint;
    public final Minecraft mc;
    int originalIndex;

    public ListEntry(CommonSelectionGui selectionGui) {
        this.selectionGui = selectionGui;
        this.entryPoint = selectionGui.entryPoint;
        this.mc = Minecraft.getMinecraft();
    }

    public abstract void drawEntry(int index, int xpos, int ypos, int p_148279_4_, int p_148279_5_,
        Tessellator tessellator, int p_148279_7_, int p_148279_8_, boolean p_148279_9_);

    public abstract Object getValue();

    protected boolean showHoverOverlay() {
        return true;
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_,
        int p_148278_6_) {
        if (this.showHoverOverlay() /* && p_148278_5_ <= 32 */) {
            return true;
        }
        return false;
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_,
        int p_148277_6_) {}

    public void hide() {
        this.selectionGui.availableEntries.remove(this);
        this.selectionGui.hiddenList.add(this);
    }

    public void show() {
        int ind = Math.max(0, Math.min(this.originalIndex, this.selectionGui.availableEntries.size() - 1));
        this.selectionGui.availableEntries.add(ind, this);
        this.selectionGui.hiddenList.remove(this);
    }

    public abstract String getDisplayString();
}
