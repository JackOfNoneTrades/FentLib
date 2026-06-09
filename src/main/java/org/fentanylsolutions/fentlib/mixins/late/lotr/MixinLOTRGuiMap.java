package org.fentanylsolutions.fentlib.mixins.late.lotr;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.fentlib.util.ClientUtil;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "lotr.client.gui.LOTRGuiMap", remap = false)
public abstract class MixinLOTRGuiMap {

    @Unique
    private static final String FENTLIB$RENDER_PLAYER_ICON = "renderPlayerIcon(Lcom/mojang/authlib/GameProfile;Ljava/lang/String;DDII)D";

    @Unique
    private boolean fentlib$legacyMapIconSkin = true;

    @Redirect(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
            remap = true),
        remap = false)
    private void fentlib$bindAndDetectSkinLayout(TextureManager textureManager, ResourceLocation skin) {
        textureManager.bindTexture(skin);

        int texWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int texHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        if (texWidth > 0 && texHeight > 0) {
            this.fentlib$legacyMapIconSkin = texWidth == texHeight * 2;
            return;
        }

        this.fentlib$legacyMapIconSkin = !ClientUtil.useNewSkinFormat();
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 0,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernFaceVMax0(double original) {
        return this.fentlib$modernFaceVMax(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 1,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernFaceVMax1(double original) {
        return this.fentlib$modernFaceVMax(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 2,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernFaceVMin2(double original) {
        return this.fentlib$modernFaceVMin(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 3,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernFaceVMin3(double original) {
        return this.fentlib$modernFaceVMin(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 4,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernHatVMax4(double original) {
        return this.fentlib$modernFaceVMax(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 5,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernHatVMax5(double original) {
        return this.fentlib$modernFaceVMax(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 6,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernHatVMin6(double original) {
        return this.fentlib$modernFaceVMin(original);
    }

    @ModifyArg(
        method = FENTLIB$RENDER_PLAYER_ICON,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V",
            ordinal = 7,
            remap = true),
        index = 4,
        remap = false)
    private double fentlib$modernHatVMin7(double original) {
        return this.fentlib$modernFaceVMin(original);
    }

    @Unique
    private double fentlib$modernFaceVMin(double original) {
        return this.fentlib$legacyMapIconSkin ? original : 0.125D;
    }

    @Unique
    private double fentlib$modernFaceVMax(double original) {
        return this.fentlib$legacyMapIconSkin ? original : 0.25D;
    }
}
