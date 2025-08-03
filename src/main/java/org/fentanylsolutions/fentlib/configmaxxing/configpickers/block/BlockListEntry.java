package org.fentanylsolutions.fentlib.configmaxxing.configpickers.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.fentanylsolutions.fentlib.configmaxxing.configpickers.CommonSelectionGui;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.ListEntry;
import org.fentanylsolutions.fentlib.util.DrawUtil;
import org.lwjgl.opengl.GL11;

public class BlockListEntry extends ListEntry {

    Block block;

    public BlockListEntry(CommonSelectionGui selectionGui, Block block) {
        super(selectionGui);
        this.block = block;
    }

    @Override
    public void drawEntry(int index, int xpos, int ypos, int p_148279_4_, int p_148279_5_, Tessellator tessellator,
        int p_148279_7_, int p_148279_8_, boolean p_148279_9_) {
        Item item = Item.getItemFromBlock(block);
        /*
         * if (block instanceof BlockCauldron) {
         * item = (Item) Item.itemRegistry.getObject("cauldron");
         * }
         */
        if (item == null) {
            item = (Item) Item.itemRegistry.getObject(
                block.getUnlocalizedName()
                    .substring(5));
        }
        if (item != null) {
            DrawUtil.drawItemStack(new ItemStack(item), xpos, ypos, "");
            GL11.glColor4f(1, 1, 1, 1);
        }

        /* Drawing the main text */
        int i2;
        String s = entryPoint.getDisplayStringStyled(block);
        i2 = this.mc.fontRenderer.getStringWidth(s);
        if (i2 > 157) {
            s = this.mc.fontRenderer.trimStringToWidth(s, 157 - this.mc.fontRenderer.getStringWidth("...")) + "...";
        }
        this.mc.fontRenderer.drawStringWithShadow(s, xpos + 32 + 2, ypos + 1, 16777215);

        /* Drawing the second line */
        List list = this.mc.fontRenderer.listFormattedStringToWidth(entryPoint.getDescription(block), 157);
        for (int j2 = 0; j2 < 2 && j2 < list.size(); ++j2) {
            this.mc.fontRenderer
                .drawStringWithShadow((String) list.get(j2), xpos + 32 + 2, ypos + 12 + 10 * j2, 8421504);
        }
    }

    @Override
    public Object getValue() {
        return block.getUnlocalizedName();
    }

    @Override
    public String getDisplayString() {
        return entryPoint.getDisplayString(block);
    }
}
