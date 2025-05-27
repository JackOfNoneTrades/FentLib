package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import carbonconfiglib.gui.config.ConfigElement;

@Mixin(ConfigElement.class)
public interface ConfigElementAccessor {

    @Invoker(value = "renderName", remap = false)
    boolean invokeRenderName();
}
