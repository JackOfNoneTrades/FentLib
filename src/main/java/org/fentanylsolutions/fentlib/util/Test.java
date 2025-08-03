package org.fentanylsolutions.fentlib.util;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.impl.entries.RegistryValue;
import org.fentanylsolutions.fentlib.mixininterfaces.IRegistryValue;
import org.fentanylsolutions.fentlib.mixins.late.carbonconfig.RegistryValueAccessor;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Test {

    public static void provideSuggestions(Consumer<ISuggestionProvider.Suggestion> output, Predicate<ISuggestionProvider.Suggestion> filter, RegistryValue value) {
        for(Object entry : ((IRegistryValue)value).getCarbonRegistry()) {
            String key = ((IRegistryValue)value).getCarbonRegistry().getNameForObject(entry).toString();
            ISuggestionProvider.Suggestion suggestion = ISuggestionProvider.Suggestion.namedTypeValue(key, key, ((RegistryValueAccessor) value).getClz());
            if(filter.test(suggestion)) output.accept(suggestion);
        }
    }
}
