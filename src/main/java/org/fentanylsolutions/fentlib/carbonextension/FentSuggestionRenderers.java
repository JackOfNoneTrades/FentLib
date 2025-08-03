package org.fentanylsolutions.fentlib.carbonextension;

import carbonconfiglib.gui.widgets.SuggestionRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import carbonconfiglib.gui.api.ISuggestionRenderer;
import net.minecraft.util.ResourceLocation;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.configmaxxing.configpickers.mob.MobRenderTicker;
import org.fentanylsolutions.fentlib.util.MobUtil;
import org.fentanylsolutions.fentlib.util.entityrenderutil.EntityUtils;
import org.lwjgl.input.Mouse;

public class FentSuggestionRenderers {

    public static class MobSuggestionRenderer implements ISuggestionRenderer {
        private static final RenderItem RENDERER = new RenderItem();

        @Override
        public IChatComponent renderSuggestion(String value, int x, int y) {
            /*Item item = (Item)Item.itemRegistry.getObject("minecraft:apple");
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack itemStack = new ItemStack(item);
            RENDERER.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), itemStack, x, y);*/
            Entity entity = EntityList.createEntityByName(MobUtil.pseudoResourceToName(value), MobRenderTicker.world);
            if (!(entity instanceof EntityLivingBase)) {
                FentLib.LOG.debug("(setMob) failed to create entity from \"{}\"", MobUtil.pseudoResourceToName(value));
                return new ChatComponentText(value);
            }
            EntityLivingBase mob = (EntityLivingBase)entity;
            Minecraft mcClient = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(mcClient, mcClient.displayWidth, mcClient.displayHeight);
            final int mouseX = (Mouse.getX() * sr.getScaledWidth()) / mcClient.displayWidth;
            final int mouseY = sr.getScaledHeight() - ((Mouse.getY() * sr.getScaledHeight()) / mcClient.displayHeight) - 1;
            //int distanceToSide = ((mcClient.currentScreen.width / 2) + 98);
            //float targetHeight = (float) (sr.getScaledHeight_double() / 5.0F) / 1.8F;
            float scale = EntityUtils.getEntityScale(mob, 1.0F, 16F);
            mcClient.thePlayer = MobRenderTicker.clientPlayerMP;
            EntityUtils.drawEntityOnScreenFollowMouse(
                x,
                (int) (y + (mob.height * scale)),
                scale,
                x - mouseX,
                (y + (mob.height * scale))
                    - (mob.height * scale * (mob.getEyeHeight() / mob.height))
                    - mouseY,
                mob);
            return new ChatComponentText(value);
        }
    }

}
