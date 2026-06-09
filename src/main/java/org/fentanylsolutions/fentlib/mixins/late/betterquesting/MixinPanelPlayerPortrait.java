package org.fentanylsolutions.fentlib.mixins.late.betterquesting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "betterquesting.api2.client.gui.panels.content.PanelPlayerPortrait", priority = 999, remap = false)
public abstract class MixinPanelPlayerPortrait {

    @Shadow(remap = false)
    @Final
    private AbstractClientPlayer player;

    @Inject(
        method = "<init>(Lbetterquesting/api2/client/gui/misc/IGuiRect;Lnet/minecraft/client/entity/AbstractClientPlayer;)V",
        at = @At("TAIL"),
        remap = false)
    private void fentlib$loadSkinThroughSkinManager(CallbackInfo ci) {
        if (this.player == null || this.player.getGameProfile() == null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }

        SkinManager skinManager = mc.func_152342_ad();
        if (skinManager != null) {
            skinManager.func_152790_a(this.player.getGameProfile(), this.player, true);
        }
    }

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
