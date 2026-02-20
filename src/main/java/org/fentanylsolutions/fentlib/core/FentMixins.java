package org.fentanylsolutions.fentlib.core;

import java.util.List;
import java.util.Set;

import org.fentanylsolutions.fentlib.util.MixinUtil;

public abstract class FentMixins {

    private final MixinUtil.Registry registry = new MixinUtil.Registry();
    private boolean initialized = false;

    protected abstract void registerMixins(MixinUtil.Registry registry);

    private synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }
        registerMixins(registry);
        initialized = true;
    }

    public final List<String> getEarlyMixins() {
        ensureInitialized();
        return registry.resolveEarly();
    }

    public final List<String> getLateMixins(Set<String> loadedCoreMods) {
        ensureInitialized();
        return registry.resolveLate(loadedCoreMods);
    }
}
