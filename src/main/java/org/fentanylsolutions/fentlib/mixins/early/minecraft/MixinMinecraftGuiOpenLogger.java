package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.FentLib;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftGuiOpenLogger {

    @Shadow
    public GuiScreen currentScreen;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"))
    private void fentlib$logOpenedGui(GuiScreen guiScreen, CallbackInfo ci) {
        if (!Config.logOpenedGuis) {
            return;
        }

        FentLib.LOG.info(
            "[GUI Debug] displayGuiScreen: {} -> {}",
            fentlib$getGuiName(this.currentScreen),
            fentlib$getGuiName(guiScreen));
    }

    @Unique
    private static String fentlib$getGuiName(GuiScreen guiScreen) {
        return guiScreen == null ? "null"
            : guiScreen.getClass()
                .getName();
    }
}
