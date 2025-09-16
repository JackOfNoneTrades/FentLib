package org.fentanylsolutions.fentlib.mixins.early.fml;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.discovery.JarDiscoverer;
import cpw.mods.fml.common.discovery.asm.ASMModParser;

@SuppressWarnings("unused")
@Mixin(JarDiscoverer.class)
public class MixinJarDiscoverer {

    // TODO: The @Redirect works, the @WrapOperation does not (breaks a ton of shit)
    /*
     * @WrapOperation(
     * method = "discover",
     * at = @At(value = "NEW", target = "cpw/mods/fml/common/discovery/asm/ASMModParser"),
     * remap = false)
     * private ASMModParser wrapAsmParserNew(InputStream stream, Operation<ASMModParser> original) {
     * try {
     * return original.call();
     * } catch (LoaderException e) {
     * System.err.println("[Mixin] Suppressed LoaderException in ASMModParser: " + e.getMessage());
     * return null;
     * } catch (Exception e) {
     * System.err.println("[Mixin] Unexpected error in ASMModParser constructor: " + e.getMessage());
     * return null;
     * }
     * }
     */

    @Redirect(
        method = "discover",
        at = @At(value = "NEW", target = "cpw/mods/fml/common/discovery/asm/ASMModParser"),
        remap = false)
    private ASMModParser safeAsmParser(InputStream stream) {
        try {
            return new ASMModParser(stream);
        } catch (LoaderException e) {
            System.err.println("[Mixin] Suppressed LoaderException in JarDiscoverer: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[Mixin] Unexpected exception in ASMModParser: " + e.getMessage());
            return null;
        }
    }
}
