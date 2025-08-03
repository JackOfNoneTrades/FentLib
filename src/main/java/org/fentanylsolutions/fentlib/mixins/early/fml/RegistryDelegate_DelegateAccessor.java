package org.fentanylsolutions.fentlib.mixins.early.fml;

import cpw.mods.fml.common.registry.RegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RegistryDelegate.Delegate.class, remap = false)
public interface RegistryDelegate_DelegateAccessor<T> {
    @Invoker
    void invokeChangeReference(T newTarget);

    @Invoker
    void invokeSetName(String name);
}
