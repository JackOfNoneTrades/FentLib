package org.fentanylsolutions.fentlib.mixins.late.sonicvisuals;

import java.util.Random;

import org.fentanylsolutions.fentlib.FentLib;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.sonicjumper.enhancedvisuals.visuals.VisualManager", remap = false)
public class MixinVisualManager {

    @Unique
    private static boolean fentlib$loggedInvalidVisualLifetime = false;

    @Redirect(
        method = "addVisualsWithShading",
        at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"),
        remap = false)
    private int fentlib$sanitizeNextIntBound(Random random, int bound) {
        if (bound <= 0) {
            this.fentlib$logInvalidVisualLifetime("random bound " + bound);
            return 0;
        }
        return random.nextInt(bound);
    }

    @ModifyArg(
        method = "addVisualsWithShading",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sonicjumper/enhancedvisuals/visuals/Animation;<init>(Lcom/sonicjumper/enhancedvisuals/visuals/VisualType;ILjava/awt/Color;)V"),
        index = 1,
        remap = false)
    private int fentlib$sanitizeAnimationLifetime(int lifetime) {
        return this.fentlib$sanitizeLifetime(lifetime);
    }

    @ModifyArg(
        method = "addVisualsWithShading",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sonicjumper/enhancedvisuals/visuals/Overlay;<init>(Lcom/sonicjumper/enhancedvisuals/visuals/VisualType;ILjava/awt/Color;)V"),
        index = 1,
        remap = false)
    private int fentlib$sanitizeOverlayLifetime(int lifetime) {
        return this.fentlib$sanitizeLifetime(lifetime);
    }

    @ModifyArg(
        method = "addVisualsWithShading",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sonicjumper/enhancedvisuals/visuals/Splat;<init>(Lcom/sonicjumper/enhancedvisuals/visuals/VisualType;ILjava/awt/Color;)V"),
        index = 1,
        remap = false)
    private int fentlib$sanitizeSplatLifetime(int lifetime) {
        return this.fentlib$sanitizeLifetime(lifetime);
    }

    @ModifyArg(
        method = "addVisualsWithShading",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sonicjumper/enhancedvisuals/visuals/Visual;<init>(Lcom/sonicjumper/enhancedvisuals/visuals/VisualType;ILjava/awt/Color;)V"),
        index = 1,
        remap = false)
    private int fentlib$sanitizeBaseLifetime(int lifetime) {
        return this.fentlib$sanitizeLifetime(lifetime);
    }

    @Unique
    private int fentlib$sanitizeLifetime(int lifetime) {
        if (lifetime < 0) {
            this.fentlib$logInvalidVisualLifetime("lifetime " + lifetime);
            return 0;
        }
        return lifetime;
    }

    @Unique
    private void fentlib$logInvalidVisualLifetime(String detail) {
        if (fentlib$loggedInvalidVisualLifetime) {
            return;
        }
        fentlib$loggedInvalidVisualLifetime = true;
        FentLib.LOG.warn("Sanitized invalid Enhanced Visuals value ({}) to avoid a crash", detail);
    }
}
