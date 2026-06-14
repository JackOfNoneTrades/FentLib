package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.client.multiplayer.ServerAddress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerAddress.class)
public class MixinServerAddress {

    /**
     * Strip {@code http://} / {@code https://} scheme prefixes and any trailing
     * path, which vanilla ServerAddress parsing cannot handle (it splits on
     * {@code :} naively, so a pasted web URL resolves to host "https").
     */
    @ModifyVariable(method = "func_78860_a", at = @At("HEAD"), argsOnly = true)
    private static String fentlib$stripScheme(String address) {
        if (address == null) {
            return null;
        }
        String result = address;
        if (result.regionMatches(true, 0, "https://", 0, 8)) {
            result = result.substring(8);
        } else if (result.regionMatches(true, 0, "http://", 0, 7)) {
            result = result.substring(7);
        }
        int slash = result.indexOf('/');
        if (slash >= 0) {
            result = result.substring(0, slash);
        }
        return result;
    }
}
