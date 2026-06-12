package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.util.regex.Matcher;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBanIp;

import org.fentanylsolutions.fentlib.util.NetworkAddressUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Relax vanilla's IPv4-only regex so /ban-ip accepts IPv6 addresses. */
@Mixin(CommandBanIp.class)
public abstract class MixinCommandBanIpIpv6 {

    @Redirect(
        method = "processCommand",
        at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;matches()Z", remap = false))
    private boolean fentlib$looksLikeIp(Matcher matcher, ICommandSender sender, String[] args) {
        return NetworkAddressUtil.looksLikeIp(args[0]);
    }
}
