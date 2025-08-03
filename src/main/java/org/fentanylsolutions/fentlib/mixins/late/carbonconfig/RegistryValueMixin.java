package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.impl.entries.RegistryValue;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.MultilinePolicy;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.StructureList;
import org.fentanylsolutions.fentlib.carbonextension.CarbonNamespacedRegistry;
import org.fentanylsolutions.fentlib.mixininterfaces.IRegistryValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import speiger.src.collections.objects.sets.ObjectLinkedOpenHashSet;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = RegistryValue.class, remap = false)
public abstract class RegistryValueMixin<T> extends ConfigEntry.CollectionConfigEntry<T, Set<T>> implements IRegistryValue<T> {

    @Unique
    private CarbonNamespacedRegistry<T> carbonRegistry;

    @Shadow
    private Predicate<T> filter;
    @Shadow private Class<T> clz;

    public RegistryValueMixin(String key, Set<T> defaultValue, String... comment) {
        super(key, defaultValue, comment);
    }

    public void setRegistry(CarbonNamespacedRegistry<T> registry) {
        this.carbonRegistry = registry;
    }

    @Inject(method = "copy", at = @At("RETURN"), cancellable = true)
    private void onCopyReturn(CallbackInfoReturnable<RegistryValue<T>> cir) {
        RegistryValue<T> copy = cir.getReturnValue();
        ((IRegistryValue)copy).setRegistry(carbonRegistry);
    }

    /**
     * @author jack
     * @reason RegistryValue overhaul in order to use custom registries
     */
    @Overwrite
    protected String serializedValue(MultilinePolicy policy, Set<T> value) {
        String[] result = new String[value.size()];
        int i = 0;
        for(Object entry : value) {
            // TODO: make an issue about this upstream
            result[i ++] = carbonRegistry.getNameForObject(entry);
        }
        return serializeArray(policy, result);
    }

    /**
     * @author jack
     * @reason RegistryValue overhaul in order to use custom registries
     */
    @Overwrite
    public ParseResult<Set<T>> parseValue(String value) {
        String[] values = Helpers.splitArray(value, ",");
        Set<T> result = new ObjectLinkedOpenHashSet<>();
        for(int i = 0,m=values.length;i<m;i++) {
            T entry = carbonRegistry.getObject(values[i]);
            if(entry == null || (filter != null && !filter.test(entry))) continue;
            result.add(entry);
        }
        return ParseResult.success(result);
    }

    /**
     * @author jack
     * @reason RegistryValue overhaul in order to use custom registries
     */
    @Overwrite
    public ParseResult<Boolean> canSet(Set<T> value) {
        ParseResult<Boolean> result = super.canSet(value);
        if(result.hasError()) return result;
        for(T entry : value) {
            if(carbonRegistry.getNameForObject(entry) == null) return ParseResult.partial(false, NoSuchElementException::new, "Value ["+entry+"] doesn't exist in the registry");
            if(filter != null && !filter.test(entry)) return ParseResult.partial(false, IllegalArgumentException::new, "Value ["+carbonRegistry.getNameForObject(entry)+"] isn't allowed");
        }
        return ParseResult.success(true);
    }

    /**
     * @author jack
     * @reason RegistryValue overhaul in order to use custom registries
     */
    @Overwrite
    public IStructuredData getDataType() {
        return StructureList.ListBuilder.variants(IStructuredData.EntryDataType.STRING, clz, this::parseEntry, carbonRegistry::getNameForObject).addSuggestions(ISuggestionProvider.wrapper(this::getSuggestions)).build(true);
    }

    private ParseResult<T> parseEntry(String value) {
        T entry = carbonRegistry.getObject(value);
        if(entry == null || (filter != null && !filter.test(entry))) return ParseResult.error(value, "Id ["+value+"] isn't valid");
        return ParseResult.success(entry);
    }

    /**
     * @author jack
     * @reason RegistryValue overhaul in order to use custom registries
     */
    @Overwrite
    public void serialize(IWriteBuffer buffer) {
        Set<T> value = getValue();
        buffer.writeVarInt(value.size());
        for(T entry : value) {
            buffer.writeVarInt(carbonRegistry.getId(entry));
        }
    }

    /**
     * @author jack
     * @reason RegistryValue overhaul in order to use custom registries
     */
    @Overwrite
    protected void deserializeValue(IReadBuffer buffer) {
        Set<T> result = new ObjectLinkedOpenHashSet<>();
        int size = buffer.readVarInt();
        for(int i = 0;i<size;i++) {
            T entry = carbonRegistry.getRaw(buffer.readVarInt());
            if(entry != null) {
                result.add(entry);
            }
        }
    }

    public CarbonNamespacedRegistry<T> getCarbonRegistry() {
        return carbonRegistry;
    }
}
