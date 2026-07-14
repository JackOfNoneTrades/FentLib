package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.nio.IntBuffer;

import org.fentanylsolutions.fentlib.util.sound.SoundUtil;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import paulscode.sound.Channel;
import paulscode.sound.Source;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

@Mixin(value = SourceLWJGLOpenAL.class, remap = false)
public abstract class MixinSourceLWJGLOpenAL {

    @Unique
    private static final int fentlib$AL_SOURCE_RADIUS = 0x1031;
    @Unique
    private static final String fentlib$SOURCE_RADIUS_EXTENSION = "AL_EXT_SOURCE_RADIUS";
    @Unique
    private float fentlib$sourceRadius = Float.NaN;

    @Inject(
        method = "play",
        at = @At(value = "INVOKE", target = "Lpaulscode/sound/Source;play(Lpaulscode/sound/Channel;)V"))
    private void fentlib$resolveSourceRadius(Channel channel, CallbackInfo ci) {
        if (Float.isNaN(this.fentlib$sourceRadius)) {
            String sourceName = ((Source) (Object) this).sourcename;
            this.fentlib$sourceRadius = SoundUtil.takeSourceRadius(sourceName);
        }
    }

    @Inject(
        method = "play",
        at = @At(
            value = "INVOKE",
            target = "Lpaulscode/sound/Source;play(Lpaulscode/sound/Channel;)V",
            shift = At.Shift.AFTER))
    private void fentlib$applySourceRadius(Channel channel, CallbackInfo ci) {
        if (!(channel instanceof ChannelLWJGLOpenAL)) {
            return;
        }

        IntBuffer alSource = ((ChannelLWJGLOpenAL) channel).ALSource;
        if (alSource == null || alSource.capacity() == 0) {
            return;
        }

        try {
            if (AL10.alIsExtensionPresent(fentlib$SOURCE_RADIUS_EXTENSION)) {
                AL10.alSourcef(alSource.get(0), fentlib$AL_SOURCE_RADIUS, this.fentlib$sourceRadius);
            }
        } catch (Throwable ignored) {}
    }
}
