package org.fentanylsolutions.fentlib.compat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.model.ModelRenderer;

public final class EntityDismembermentCompat {

    private EntityDismembermentCompat() {}

    /**
     * Empty parts are transform nodes, not gibs. Promote their descendants without mutating the
     * live model or changing how Entity Dismemberment groups parts that have their own geometry.
     */
    public static List<ModelRenderer> renderableParts(List<ModelRenderer> parts) {
        if (parts == null) {
            return null;
        }
        boolean hasEmptyParts = false;
        for (ModelRenderer part : parts) {
            if (part.cubeList.isEmpty()) {
                hasEmptyParts = true;
                break;
            }
        }
        if (!hasEmptyParts) {
            return parts;
        }

        List<ModelRenderer> result = new ArrayList<>();
        Set<ModelRenderer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        collectParts(parts, result, visited);
        return result;
    }

    private static void collectParts(List<ModelRenderer> parts, List<ModelRenderer> result,
        Set<ModelRenderer> visited) {
        for (ModelRenderer part : parts) {
            if (!visited.add(part)) {
                continue;
            }
            if (!part.cubeList.isEmpty()) {
                result.add(part);
            } else if (part.childModels != null) {
                collectParts(part.childModels, result, visited);
            }
        }
    }
}
