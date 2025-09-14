package org.fentanylsolutions.fentlib.mixins.early.fml;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.discovery.JarDiscoverer;
import cpw.mods.fml.common.discovery.asm.ASMModParser;

@SuppressWarnings("unused")
@Mixin(JarDiscoverer.class)
public class MixinJarDiscoverer {

    @WrapOperation(
        method = "discover",
        at = @At(value = "NEW", target = "cpw/mods/fml/common/discovery/asm/ASMModParser"),
        remap = false)
    private ASMModParser wrapAsmParserNew(InputStream stream, Operation<ASMModParser> original) {
        try {
            return original.call();
        } catch (LoaderException e) {
            System.err.println("[Mixin] Suppressed LoaderException in ASMModParser: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("[Mixin] Unexpected error in ASMModParser constructor: " + e.getMessage());
            return null;
        }
    }
}
