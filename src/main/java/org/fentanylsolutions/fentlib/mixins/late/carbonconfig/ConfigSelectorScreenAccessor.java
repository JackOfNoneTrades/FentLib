package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import net.minecraft.util.IChatComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import carbonconfiglib.gui.screen.ConfigSelectorScreen;

@Mixin(ConfigSelectorScreen.class)
public interface ConfigSelectorScreenAccessor {

    @Accessor(remap = false)
    IChatComponent getModName();
}
