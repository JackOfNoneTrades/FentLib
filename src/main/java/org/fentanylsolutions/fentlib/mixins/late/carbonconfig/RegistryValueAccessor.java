package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import carbonconfiglib.impl.entries.RegistryValue;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;

@Mixin(RegistryValue.class)
public interface RegistryValueAccessor {

    @Accessor(remap = false)
    <T> FMLControlledNamespacedRegistry<T> getRegistry();

    @Accessor(remap = false)
    <T> Class<T> getClz();

    @Accessor(remap = false)
    <T> Predicate<T> getFilter();
}
