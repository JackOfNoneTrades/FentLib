package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import carbonconfiglib.gui.config.ConfigElement;

@Mixin(ConfigElement.AlignOffset.class)
public interface AlignOffsetAccessor {

    @Accessor(remap = false)
    ConfigElement.GuiAlign getAlign();

    @Accessor(remap = false)
    int getOffset();
}
