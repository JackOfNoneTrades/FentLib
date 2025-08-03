package org.fentanylsolutions.fentlib.configmaxxing.configpickers.mob;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.fentlib.configmaxxing.Config;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.CommonSelectionGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.EntryPoint;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

/* The gui presenting the list of potions to choose from */
public class MobSelectionGui extends CommonSelectionGui {

    ResourceLocation mobBackground = new ResourceLocation("textures/gui/demo_background.png");

    public MobSelectionGui(int index, EntryPoint entryPoint) {
        super(index, entryPoint);

        this.title = I18n.format("eyesintheshadows.mob_selection_title");

        /*
         * if (index < 0 || index >= returnInfo.values.length) {
         * this.originalValue = "None";
         * } else {
         * this.originalValue = (String) returnInfo.values[index];
         * }
         */
    }

    @Override
    public void initGui() {
        super.initGui();

        // this.availablePotionListGui.setSlotXBoundsFromLeft(this.width / 2 - 4 - 200);
        this.availableEntriesListGui.setSlotXBoundsFromLeft(this.width / 2);
        // this.availablePotionListGui.setS
        this.availableEntriesListGui.registerScrollButtons(7, 8);
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawBackground(0);

        // this.availableMobListGui.top = 35;
        this.availableEntriesListGui.left = 25;
        // this.availableMobListGui.bottom = this.height - 30;
        this.availableEntriesListGui.right = this.width / 2;

        this.availableEntriesListGui.drawScreen(mouseX, mouseY, partialTicks);
        drawMobBackground(this.width / 2 + 15, 60, this.width / 2 - 70, this.height - 90);
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 16, 16777215);
        drawFooter();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

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

    public void populateEntries() {
        for (Object e : EntityList.stringToClassMapping.keySet()) {
            boolean foundInBlackList = false;
            for (String s : Config.mobBlackListArray) {
                if (s.equals(e)) {
                    foundInBlackList = true;
                    break;
                }
            }
            if (!foundInBlackList) {
                MobListEntry entry = new MobListEntry(this, (String) e);
                this.availableEntries.add(entry);
            }
        }
    }

    public void drawMobBackground(int x, int y, int drawWidth, int drawHeight) {
        this.mc.getTextureManager()
            .bindTexture(mobBackground);

        GL11.glEnable(GL11.GL_BLEND);

        Tessellator tessellator = Tessellator.instance;
        tessellator.setTranslation(0, 0, 0);
        tessellator.startDrawingQuads();

        /*
         * Ok so basically, the texture is a bit smaller than the size of the image itself.
         * These values are passed to the tesselator and tell it how much of the image it should actually draw,
         * in a 0 to 1 double (like a percentage).
         */
        double textureWidth = 0.96875;
        double textureHeight = 0.6484375;

        tessellator.addVertexWithUV(x, (y + drawHeight), 0.0D, 0, textureHeight);
        tessellator.addVertexWithUV((x + drawWidth), (y + drawHeight), 0.0D, textureWidth, textureHeight);
        tessellator.addVertexWithUV((x + drawWidth), y, 0.0D, textureWidth, 0);
        tessellator.addVertexWithUV(x, y, 0.0D, 0, 0);
        tessellator.draw();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void onEntrySelect(int index, boolean doScroll) {
        MobRenderTicker.setMob(
            (String) availableEntries.get(index)
                .getValue());
    }
}
