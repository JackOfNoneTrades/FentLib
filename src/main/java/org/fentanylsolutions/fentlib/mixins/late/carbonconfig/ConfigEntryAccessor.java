package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.config.ConfigEntry;

@Mixin(ConfigEntry.class)
public interface ConfigEntryAccessor {

    @Invoker(value = "addInternal", remap = false)
    public void invokeAddInternal(ISuggestionProvider.Suggestion value, List<ISuggestionProvider.Suggestion> output);
}
