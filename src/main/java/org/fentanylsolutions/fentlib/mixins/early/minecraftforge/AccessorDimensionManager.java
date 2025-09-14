package org.fentanylsolutions.fentlib.mixins.early.minecraftforge;

import java.util.Hashtable;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DimensionManager.class)
public interface AccessorDimensionManager {

    @Accessor(value = "providers", remap = false)
    static Hashtable<Integer, Class<? extends WorldProvider>> getProviders() {
        throw new AssertionError();
    }
}
