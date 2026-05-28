package org.fentanylsolutions.fentlib.gui.sodiumgui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SodiumGuiScrollState {

    private float scroll;
    private boolean dragging;
    private float dragOffset;

    public float getScroll() {
        return this.scroll;
    }

    public void setScroll(float scroll, int contentHeight, int viewportHeight) {
        this.scroll = SodiumGui.clamp(scroll, 0.0F, SodiumGui.getMaxScroll(contentHeight, viewportHeight));
    }

    public void clampTo(int contentHeight, int viewportHeight) {
        setScroll(this.scroll, contentHeight, viewportHeight);
    }

    public void scrollWheel(int wheel, int contentHeight, int viewportHeight) {
        if (wheel == 0) {
            return;
        }
        float wheelSteps = wheel / 120.0F;
        if (Math.abs(wheelSteps) < 1.0F) {
            wheelSteps = Math.signum(wheel);
        }
        setScroll(this.scroll - wheelSteps * SodiumGui.WHEEL_SCROLL_PIXELS, contentHeight, viewportHeight);
    }

    public void beginDrag(int trackY, int viewportHeight, int contentHeight, int mouseY) {
        SodiumGui.ScrollbarMetrics metrics = SodiumGui
            .getScrollbarMetrics(trackY, viewportHeight, contentHeight, this.scroll);
        if (metrics == null) {
            return;
        }

        this.dragging = true;
        if (mouseY >= metrics.getThumbY() && mouseY < metrics.getThumbY() + metrics.getThumbHeight()) {
            this.dragOffset = mouseY - metrics.getThumbY();
        } else {
            this.dragOffset = metrics.getThumbHeight() / 2.0F;
            dragTo(trackY, viewportHeight, contentHeight, mouseY);
        }
    }

    public void dragTo(int trackY, int viewportHeight, int contentHeight, int mouseY) {
        if (!this.dragging) {
            return;
        }
        this.scroll = SodiumGui.scrollFromScrollbarDrag(trackY, viewportHeight, contentHeight, this.dragOffset, mouseY);
    }

    public void endDrag() {
        this.dragging = false;
        this.dragOffset = 0.0F;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public int getFirstRow() {
        return Math.max(0, (int) Math.floor(this.scroll / SodiumGui.ROW_STEP));
    }

    public int getFirstRowY(int viewportY) {
        int firstRow = getFirstRow();
        return viewportY - Math.round(this.scroll - firstRow * SodiumGui.ROW_STEP);
    }

    public int getRowAt(int mouseY, int viewportY, int rowCount) {
        int localY = mouseY - viewportY + Math.round(this.scroll);
        if (localY < 0 || localY % SodiumGui.ROW_STEP >= SodiumGui.ROW_HEIGHT) {
            return -1;
        }
        int row = localY / SodiumGui.ROW_STEP;
        return row >= 0 && row < rowCount ? row : -1;
    }
}
