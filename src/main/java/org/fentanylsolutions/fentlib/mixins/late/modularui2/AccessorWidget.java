package org.fentanylsolutions.fentlib.mixins.late.modularui2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.cleanroommc.modularui.widget.Widget;

@Mixin(value = Widget.class, remap = false)
public interface AccessorWidget {

    @Accessor
    String getDebugName();
}
