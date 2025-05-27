package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import carbonconfiglib.gui.config.Element;

@Mixin(Element.class)
public interface ElementAccessor {

    @Accessor(remap = false)
    Minecraft getMc();
}
