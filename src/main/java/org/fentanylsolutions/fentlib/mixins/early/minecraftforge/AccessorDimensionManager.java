package org.fentanylsolutions.fentlib.mixins.early.minecraftforge;

import java.util.Hashtable;

import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import org.fentanylsolutions.fentlib.util.MixinTargetAnnotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@MixinTargetAnnotation.MixinTarget(phase = MixinTargetAnnotation.Phase.EARLY, modid = "minecraftforge")
@Mixin(value = DimensionManager.class, remap = false)
public interface AccessorDimensionManager {

    @Accessor(value = "providers", remap = false)
    public static Hashtable<Integer, Class<? extends WorldProvider>> getProviders() {
        throw new AssertionError();
    }
}
