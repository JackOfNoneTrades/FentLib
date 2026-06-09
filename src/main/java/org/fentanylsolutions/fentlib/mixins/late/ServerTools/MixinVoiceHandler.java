package org.fentanylsolutions.fentlib.mixins.late.ServerTools;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import info.servertools.core.chat.VoiceHandler;

@Mixin(value = VoiceHandler.class, remap = false)
public class MixinVoiceHandler {

    @Inject(method = "nameFormat", at = @At("HEAD"), cancellable = true)
    private void fentlib$skipWhenNoServer(PlayerEvent.NameFormat event, CallbackInfo ci) {
        if (MinecraftServer.getServer() == null) {
            ci.cancel();
        }
    }
}
