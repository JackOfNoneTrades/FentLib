package org.fentanylsolutions.fentlib.configmaxxing.configpickers.dimension;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.WorldProvider;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.CommonSelectionGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.mixins.early.minecraftforge.DimensionManagerAccessor;
import org.fentanylsolutions.fentlib.util.DimensionUtil;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

public class DimensionSelectionGui extends CommonSelectionGui {

    public DimensionSelectionGui(int index, EntryPoint entryPoint) {
        super(index, entryPoint);
        this.title = I18n.format("eyesintheshadows.dimension_selection_title");

    }

    public void populateEntries() {
        for (int i : DimensionManagerAccessor.getProviders()
            .keySet()) {
            WorldProvider worldProvider = null;
            try {
                if (DimensionManagerAccessor.getProviders()
                    .get(i) != null) {
                    worldProvider = DimensionManagerAccessor.getProviders()
                        .get(i)
                        .newInstance();
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            if (worldProvider == null) {
                continue;
            }
            DimensionListEntry dimensionListEntry = new DimensionListEntry(
                this,
                new DimensionUtil.SimpleDimensionObj(
                    worldProvider.dimensionId,
                    worldProvider.getDimensionName(),
                    worldProvider.getDepartMessage()));
            this.availableEntries.add(dimensionListEntry);
        }
    }

    public void initGui() {
        super.initGui();

        int entryHeight = 36;

        /* The list element */
        this.availableEntriesListGui.setSlotXBoundsFromLeft(this.width / 2);
        this.availableEntriesListGui.registerScrollButtons(7, 8);
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // super.drawScreen(mouseX, mouseY, partialTicks);

        /* Drawing the background, obv */
        this.drawBackground(0);

        /* Setting the x coord of the list widget */
        this.availableEntriesListGui.left = 25;

        /* Setting the width of the list widget */
        this.availableEntriesListGui.right = this.width - 25;
        this.availableEntriesListGui.width = this.availableEntriesListGui.right - this.availableEntriesListGui.left;

        /*
         * Setting the width of the list elements (basically where the selection area and the rectangle that draws
         * around is)
         */
        this.availableEntriesListGui.setListWidth(this.availableEntriesListGui.width / 5 * 4);

        /* Drawing the list widget */
        this.availableEntriesListGui.drawScreen(mouseX, mouseY, partialTicks);

        /* Drawing the title of this gui */
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, 16, 16777215);

        /* additonal list widget footer, otherwise is buggy for some reason */
        drawFooter();

        /* Drawing the rest, esentially the search box and the done button */
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /* Draws the bottom of the list widget */
    public void drawFooter() {
        float opaqueColor = 0.25098039215F;
        GL11.glColor4f(opaqueColor, opaqueColor, opaqueColor, 1);
        DrawUtil.drawModalRectWithCustomSizedTexture(
            mc,
            optionsBackground,
            0,
            this.height - 30,
            this.width,
            30,
            this.width / 32,
            1);
        GL11.glColor4f(1, 1, 1, 1);
    }
}
