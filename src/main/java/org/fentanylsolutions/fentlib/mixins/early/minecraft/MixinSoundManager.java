package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;

import org.fentanylsolutions.fentlib.util.sound.SoundUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(value = SoundManager.class)
public abstract class MixinSoundManager {

    @ModifyArg(
        method = "playSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;newStreamingSource(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V",
            remap = false),
        require = 1,
        index = 9)
    private float fentlib$customStreamingMaxDistance(float vanillaMaxDistance, @Local(argsOnly = true) ISound sound) {
        return SoundUtil.getEffectiveMaxDistance(sound, vanillaMaxDistance);
    }

    @ModifyArg(
        method = "playSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;newSource(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V",
            remap = false),
        require = 1,
        index = 9)
    private float fentlib$customMaxDistance(float vanillaMaxDistance, @Local(argsOnly = true) ISound sound) {
        return SoundUtil.getEffectiveMaxDistance(sound, vanillaMaxDistance);
    }

    @ModifyArg(
        method = "playSound",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;play(Ljava/lang/String;)V",
            remap = false),
        require = 1,
        index = 0)
    private String fentlib$registerSourceRadius(String sourceName, @Local(argsOnly = true) ISound sound) {
        SoundUtil.registerSourceRadius(sourceName, sound);
        return sourceName;
    }
}
