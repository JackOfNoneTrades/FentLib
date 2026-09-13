package org.fentanylsolutions.fentlib.mixins.late.etfuturum;

import org.fentanylsolutions.fentlib.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.angelica.mixins.interfaces.IBatchEligibility;
import com.gtnewhorizons.angelica.rendering.tesr.BatchEligibility;

import cpw.mods.fml.common.Loader;

@Pseudo
@Mixin(targets = "ganymedes01.etfuturum.client.renderer.entity.NewBoatRenderer", remap = false)
public abstract class MixinNewBoatRenderer {

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void fentlib$preserveWaterMask(CallbackInfo ci) {
        if (Config.fixEtFuturumBoatRendering && Loader.isModLoaded("angelica")
            && (Object) this instanceof IBatchEligibility) {
            // Angelica does not capture glColorMask when batching model parts. The depth-only
            // noWater part becomes visible; drawing just that part immediately would also put
            // it before the queued hull. Keep the whole renderer in its original draw order.
            // ChestBoatRenderer calls this superclass constructor and gets the same fix.
            ((IBatchEligibility) (Object) this).angelica$setBatchState(BatchEligibility.DENIED);
        }
    }
}
