package org.fentanylsolutions.fentlib.mixins.late.clearmybackground;

import net.minecraft.client.Minecraft;

import org.fentanylsolutions.fentlib.gui.PanoramaOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.clear.clearmybackground.ClientHelper", remap = false)
public abstract class MixinClientHelper {

    @Inject(method = "renderPanorama", at = @At("RETURN"), remap = false)
    private static void fentlib$drawMilkyPanorama(Minecraft mc, CallbackInfo ci) {
        PanoramaOverlayRenderer.drawMilkyPanorama(mc);
    }
}
