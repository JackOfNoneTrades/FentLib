package org.fentanylsolutions.fentlib.mixins.late.betterquesting;

import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "betterquesting.api2.client.gui.panels.content.PanelPlayerPortrait", priority = 999, remap = false)
public abstract class MixinPanelPlayerPortrait {

    @Redirect(
        method = "<init>(Lbetterquesting/api2/client/gui/misc/IGuiRect;Lnet/minecraft/client/entity/AbstractClientPlayer;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getDownloadImageSkin(Lnet/minecraft/util/ResourceLocation;Ljava/lang/String;)Lnet/minecraft/client/renderer/ThreadDownloadImageData;",
            remap = true),
        remap = false)
    private ThreadDownloadImageData fentlib$skipLegacySkinDownload(ResourceLocation resourceLocationIn,
        String username) {
        return null;
    }
}
