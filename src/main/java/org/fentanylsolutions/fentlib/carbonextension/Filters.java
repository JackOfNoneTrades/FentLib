package org.fentanylsolutions.fentlib.carbonextension;

// import org.fentanylsolutions.fentlib.Config;

@Deprecated
public class Filters {
    public static <E> boolean entityFilter(E x) {
        return false;
        /*
        if (x instanceof MobWrapper mobWrapper) {
            for (String rn : Config.entityBlacklist) {
                if (mobWrapper.resourceName.equals(rn)) {
                    return false;
                }
            }
        }
        return true;
    */
    }
}
