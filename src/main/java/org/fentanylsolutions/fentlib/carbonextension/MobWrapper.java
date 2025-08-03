package org.fentanylsolutions.fentlib.carbonextension;

public class MobWrapper implements IWrapper {

    public String resourceName;

    public MobWrapper(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public String toString() {
        return this.resourceName;
    }

    @Override
    public boolean matchesWrapper(IWrapper other) {
        return ((MobWrapper)other).resourceName.equals(resourceName);
    }
}
