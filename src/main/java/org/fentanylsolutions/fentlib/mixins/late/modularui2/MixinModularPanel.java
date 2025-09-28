package org.fentanylsolutions.fentlib.mixins.late.modularui2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IModularPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ModularPanel.class)
public class MixinModularPanel implements IModularPanel {

    @Unique
    GuiScreen guiScreen;

    @WrapOperation(
        method = "closeIfOpen",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    private void wrapCloseScreen(EntityPlayerSP player, Operation<Void> original) {
        if (player == null) {
            FentLib.debug("player is null, calling Minecraft.getMinecraft().displayGuiScreen(getGuiScreen()) instead");
            Minecraft.getMinecraft()
                .displayGuiScreen(getGuiScreen());
            return;
        }
        original.call(player);
    }

    @Override
    public void setGuiScreen(GuiScreen guiScreen) {
        this.guiScreen = guiScreen;
    }

    @Override
    public GuiScreen getGuiScreen() {
        return this.guiScreen;
    }
}
