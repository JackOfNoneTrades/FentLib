package org.fentanylsolutions.fentlib.mixins.late.serverutilities;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.fentlib.util.ClientUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "serverutils.client.tab.ModernTabRenderer", priority = 999, remap = false)
public abstract class MixinModernTabRenderer {

    @Unique
    private ResourceLocation fentlib$currentTabSkin;

    @Redirect(
        method = "render(Lnet/minecraft/client/gui/ScaledResolution;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
            ordinal = 0,
            remap = true),
        remap = false)
    private void fentlib$captureTabSkin(TextureManager textureManager, ResourceLocation skin) {
        this.fentlib$currentTabSkin = skin;
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/gui/ScaledResolution;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;func_152125_a(IIFFIIIIFF)V",
            ordinal = 0,
            remap = true),
        remap = false)
    private void fentlib$drawModernTabFace(int x, int y, float u, float v, int uWidth, int vHeight, int width,
        int height, float tileWidth, float tileHeight) {
        ClientUtil.drawPlayerFace(this.fentlib$currentTabSkin, null, x, y, width, height, 1.0F);
    }

    @Redirect(
        method = "render(Lnet/minecraft/client/gui/ScaledResolution;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;func_152125_a(IIFFIIIIFF)V",
            ordinal = 1,
            remap = true),
        remap = false)
    private void fentlib$skipLegacyTabHat(int x, int y, float u, float v, int uWidth, int vHeight, int width,
        int height, float tileWidth, float tileHeight) {
        this.fentlib$currentTabSkin = null;
    }
}
