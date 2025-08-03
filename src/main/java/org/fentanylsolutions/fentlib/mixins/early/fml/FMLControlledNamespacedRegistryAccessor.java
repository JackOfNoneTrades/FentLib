package org.fentanylsolutions.fentlib.mixins.early.fml;

import com.google.common.collect.BiMap;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = FMLControlledNamespacedRegistry.class, remap = false)
public interface FMLControlledNamespacedRegistryAccessor<I> {
    @Accessor
    Class<I> getSuperType();

    @Accessor
    String getOptionalDefaultName();

    @Accessor
    I getOptionalDefaultObject();

    @Accessor
    int getMaxId();

    @Accessor
    int getMinId();

    @Accessor
    char getDiscriminator();

    @Accessor
    Map<String, String> getAliases();

    @Accessor
    BiMap<String, I> getActiveSubstitutions();
}
