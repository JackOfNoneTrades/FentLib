package org.fentanylsolutions.fentlib.mixins.early.minecraftforge;

import java.util.Hashtable;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionManager.class)
public interface DimensionManagerAccessor {

    @Accessor(remap = false)
    public static Hashtable<Integer, Class<? extends WorldProvider>> getProviders() {
        throw new AssertionError();
    }
}
