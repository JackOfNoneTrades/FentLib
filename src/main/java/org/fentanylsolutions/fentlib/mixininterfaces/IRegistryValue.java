package org.fentanylsolutions.fentlib.mixininterfaces;

import org.fentanylsolutions.fentlib.carbonextension.CarbonNamespacedRegistry;

public interface IRegistryValue<T> {
    void setRegistry(CarbonNamespacedRegistry<T> registry);
    CarbonNamespacedRegistry<T> getCarbonRegistry();
}
