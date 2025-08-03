package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.util.RegistryNamespaced;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RegistryNamespaced.class)
public interface RegistryNamespacedAccessor {
    @Accessor
    Map getField_148758_b();
}
