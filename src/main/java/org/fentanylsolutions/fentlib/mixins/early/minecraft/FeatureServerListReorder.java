package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.gui.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.fentlib.mixininterfaces.IServerEntryMoveHost;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class FeatureServerListReorder {

    @Mixin(GuiMultiplayer.class)
    public static abstract class MixinGuiMultiplayer extends GuiScreen implements IServerEntryMoveHost {

        @Shadow
        private ServerSelectionList field_146803_h;

        @Shadow
        public abstract void func_146790_a(int index);

        @Shadow
        public abstract ServerList func_146795_p();

        @Override
        public void fentlib$moveServerEntry(int fromIndex, int toIndex) {
            if (fromIndex == toIndex || fromIndex < 0 || toIndex < 0) {
                return;
            }

            ServerList serverList = this.func_146795_p();
            if (serverList == null || fromIndex >= serverList.countServers() || toIndex >= serverList.countServers()) {
                return;
            }

            serverList.swapServers(fromIndex, toIndex);
            this.field_146803_h.func_148195_a(serverList);
            this.func_146790_a(toIndex);
            this.field_146803_h.scrollBy((toIndex > fromIndex ? 1 : -1) * this.field_146803_h.getSlotHeight());
        }
    }

    @Mixin(ServerListEntryNormal.class)
    public static class MixinServerListEntryNormal {

        private static final int MOVE_CONTROLS_SIZE = 32;
        private static final int MOVE_CONTROLS_HOTSPOT_X_MIN = 16;
        private static final int MOVE_CONTROLS_HOTSPOT_X_MAX = 32;
        private static final int MOVE_CONTROLS_SPLIT_Y = 16;

        private static final ResourceLocation MOVE_CONTROLS = new ResourceLocation("textures/gui/resource_packs.png");

        @Shadow
        private GuiMultiplayer field_148303_c;

        @Inject(method = "drawEntry", at = @At("RETURN"))
        private void fentlib$drawMoveControls(int slotIndex, int x, int y, int listWidth, int slotHeight,
            Tessellator tessellator, int mouseX, int mouseY, boolean isSelected, CallbackInfo ci) {

            if (!isShiftDown() || !(field_148303_c instanceof IServerEntryMoveHost)) {
                return;
            }

            ServerList serverList = field_148303_c.func_146795_p();
            int serverCount = serverList != null ? serverList.countServers() : 0;
            boolean canMoveUp = slotIndex > 0;
            boolean canMoveDown = slotIndex >= 0 && slotIndex < serverCount - 1;

            if (!isSelected || (!canMoveUp && !canMoveDown)) {
                return;
            }

            drawMoveControls(x, y, mouseX, mouseY, canMoveUp, canMoveDown);
        }

        @Inject(method = "mousePressed", at = @At("HEAD"), cancellable = true)
        private void fentlib$onMousePressed(int slotIndex, int mouseX, int mouseY, int mouseButton, int relX, int relY,
            CallbackInfoReturnable<Boolean> cir) {

            if (mouseButton != 0 || !isShiftDown() || !(field_148303_c instanceof IServerEntryMoveHost)) {
                return;
            }

            ServerList serverList = field_148303_c.func_146795_p();
            int serverCount = serverList != null ? serverList.countServers() : 0;
            boolean canMoveUp = slotIndex > 0;
            boolean canMoveDown = slotIndex >= 0 && slotIndex < serverCount - 1;

            if (canMoveUp && isMoveUpHit(relX, relY)) {
                ((IServerEntryMoveHost) field_148303_c).fentlib$moveServerEntry(slotIndex, slotIndex - 1);
                cir.setReturnValue(true);
                return;
            }

            if (canMoveDown && isMoveDownHit(relX, relY)) {
                ((IServerEntryMoveHost) field_148303_c).fentlib$moveServerEntry(slotIndex, slotIndex + 1);
                cir.setReturnValue(true);
            }
        }

        private void drawMoveControls(int x, int y, int mouseX, int mouseY, boolean canMoveUp, boolean canMoveDown) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            try {
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(MOVE_CONTROLS);
                Gui.drawRect(x, y, x + MOVE_CONTROLS_SIZE, y + MOVE_CONTROLS_SIZE, 0xA0000000);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                int relX = mouseX - x;
                int relY = mouseY - y;
                if (canMoveUp) {
                    Gui.func_146110_a(
                        x,
                        y,
                        96.0F,
                        isMoveUpHit(relX, relY) ? 32.0F : 0.0F,
                        MOVE_CONTROLS_SIZE,
                        MOVE_CONTROLS_SIZE,
                        256.0F,
                        256.0F);
                }
                if (canMoveDown) {
                    Gui.func_146110_a(
                        x,
                        y,
                        64.0F,
                        isMoveDownHit(relX, relY) ? 32.0F : 0.0F,
                        MOVE_CONTROLS_SIZE,
                        MOVE_CONTROLS_SIZE,
                        256.0F,
                        256.0F);
                }
            } finally {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glPopAttrib();
            }
        }

        private static boolean isMoveUpHit(int relX, int relY) {
            return relX >= MOVE_CONTROLS_HOTSPOT_X_MIN && relX < MOVE_CONTROLS_HOTSPOT_X_MAX
                && relY >= 0
                && relY < MOVE_CONTROLS_SPLIT_Y;
        }

        private static boolean isMoveDownHit(int relX, int relY) {
            return relX >= MOVE_CONTROLS_HOTSPOT_X_MIN && relX < MOVE_CONTROLS_HOTSPOT_X_MAX
                && relY >= MOVE_CONTROLS_SPLIT_Y
                && relY < MOVE_CONTROLS_SIZE;
        }

        private static boolean isShiftDown() {
            return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        }
    }
}
