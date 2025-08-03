package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.fentanylsolutions.fentlib.mixininterfaces.IRegistryValue;
import org.fentanylsolutions.fentlib.util.Test;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.impl.entries.RegistryValue;

@Mixin(value = RegistryValue.RegistrySuggestions.class, remap = false)
public class RegistryValue_RegistrySuggestionsMixin<T> {

    @Shadow
    RegistryValue<T> value;

    /**
     * @author jack
     * @reason Fixes item lookup
     */
    @Overwrite
    public void provideSuggestions(Consumer<ISuggestionProvider.Suggestion> output, Predicate<ISuggestionProvider.Suggestion> filter) {
        /*for(Object entry : ((IRegistryValue)value).getCarbonRegistry()) {
            String key = ((IRegistryValue)value).getCarbonRegistry().getNameForObject(entry).toString();
            ISuggestionProvider.Suggestion suggestion = ISuggestionProvider.Suggestion.namedTypeValue(key, key, ((RegistryValueAccessor) value).getClz());
            if(filter.test(suggestion)) output.accept(suggestion);
        }*/
        Test.provideSuggestions(output, filter, value);
    }
}
