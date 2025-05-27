package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import carbonconfiglib.gui.screen.ConfigScreen;

@Mixin(ConfigScreen.class)
public interface ConfigScreenAccessor {

    @Accessor(remap = false)
    GuiScreen getParent();
}
