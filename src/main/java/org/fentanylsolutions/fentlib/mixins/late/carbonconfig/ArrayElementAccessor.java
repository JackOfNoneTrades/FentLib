package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.config.ArrayElement;

@Mixin(ArrayElement.class)
public interface ArrayElementAccessor {

    @Accessor(remap = false)
    IArrayNode getNode();
}
