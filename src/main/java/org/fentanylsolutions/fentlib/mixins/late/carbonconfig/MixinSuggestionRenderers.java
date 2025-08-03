package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import carbonconfiglib.gui.widgets.SuggestionRenderers;

@Mixin(SuggestionRenderers.ItemEntry.class)
public class MixinSuggestionRenderers {

    /**
     * @author jack
     * @reason Fixes item lookup
     */
    @Overwrite(remap = false)
    public IChatComponent renderSuggestion(String value, int x, int y) {
        Item item = (Item) Item.itemRegistry.getObject(value);

        if (item == null) {
            return null;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ItemStack itemStack = new ItemStack(item);
        RenderItem renderer = new RenderItem();

        renderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemStack, x, y);

        return new ChatComponentText(itemStack.getDisplayName())
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW))
            .appendText("\n")
            .appendSibling(
                new ChatComponentText(value).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
    }
}
