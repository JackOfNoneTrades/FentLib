package org.fentanylsolutions.fentlib.mixins.late.modularui2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.cleanroommc.modularui.ModularUI;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(ModularUI.Mods.class)
public class NEIOverrideMixin {

    @WrapOperation(
        method = "isLoaded",
        at = @At(value = "INVOKE", target = "Lcpw/mods/fml/common/Loader;isModLoaded(Ljava/lang/String;)Z"),
        remap = false)
    private boolean overrideNEIIsLoaded(String modId, Operation<Boolean> original) {
        ModularUI.Mods self = (ModularUI.Mods) (Object) this;

        if (self == ModularUI.Mods.NEI) {
            return false;
        }

        return original.call(modId);
    }
}
