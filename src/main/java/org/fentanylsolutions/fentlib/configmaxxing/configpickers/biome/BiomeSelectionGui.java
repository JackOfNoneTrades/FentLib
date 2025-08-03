package org.fentanylsolutions.fentlib.configmaxxing.configpickers.biome;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.AvailableEntriesListGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.CommonSelectionGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

public class BiomeSelectionGui extends CommonSelectionGui {

    public ResourceLocation grassResourceLocation = new ResourceLocation("textures/blocks/grass_top.png");
    public ResourceLocation leafResourceLocation = new ResourceLocation("textures/blocks/leaves_oak.png");
    public ResourceLocation waterResourceLocation = new ResourceLocation("textures/blocks/water_still.png");

    public BiomeSelectionGui(int index, EntryPoint entryPoint) {
        super(index, entryPoint);

        this.title = I18n.format("eyesintheshadows.biome_selection_title");
    }

    public void populateEntries() {
        for (int i = 0; i < (BiomeGenBase.getBiomeGenArray()).length; i++) {
            if (BiomeGenBase.getBiomeGenArray()[i] != null) {
                BiomeListEntry biomeListEntry = new BiomeListEntry(this, BiomeGenBase.getBiomeGenArray()[i]);
                this.availableEntries.add(biomeListEntry);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int entryHeight = 36;

        /* The list element */
        this.availableEntriesListGui = new AvailableEntriesListGui(
            60,
            this.height - 30,
            0 /* this.width / 4 * 3 */,
            this.height,
            entryHeight,
            this.availableEntries,
            index,
            this);
        this.availableEntriesListGui.setSlotXBoundsFromLeft(this.width / 2);
        this.availableEntriesListGui.registerScrollButtons(7, 8);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
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
