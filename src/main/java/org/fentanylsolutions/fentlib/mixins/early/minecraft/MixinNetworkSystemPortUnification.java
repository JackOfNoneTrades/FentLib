package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.network.NetworkSystem;

import org.fentanylsolutions.fentlib.services.http.HttpPortUnification;
import org.fentanylsolutions.fentlib.services.http.ProtocolSwitchHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.Channel;

/**
 * Targets the anonymous ChannelInitializer inside NetworkSystem.addLanEndpoint
 * so the vanilla and FML pipeline setup runs untouched and we only prepend
 * the protocol switch handler afterwards.
 */
@Mixin(targets = "net.minecraft.network.NetworkSystem$1")
public abstract class MixinNetworkSystemPortUnification {

    @Shadow(aliases = { "this$0", "field_151264_a" })
    @Final
    private NetworkSystem networkSystem;

    @Inject(method = "initChannel(Lio/netty/channel/Channel;)V", at = @At("TAIL"), remap = false)
    private void fentlib$addProtocolSwitch(Channel channel, CallbackInfo ci) {
        if (!HttpPortUnification.shouldInstall()) {
            return;
        }

        channel.pipeline()
            .addFirst(
                "fentlib_protocol_switch",
                new ProtocolSwitchHandler(((AccessorNetworkSystem) this.networkSystem).getNetworkManagers()));
    }
}
