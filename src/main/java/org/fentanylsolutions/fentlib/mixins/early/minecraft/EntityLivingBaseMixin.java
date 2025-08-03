package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import net.minecraft.util.DamageSource;
import org.fentanylsolutions.fentlib.FentLib;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {

    @Inject(method = "attackEntityAsMob", at = @At("TAIL"), require = 1)
    public void attackEntityAsMob(Entity entity, CallbackInfoReturnable<Boolean> cir) {

        if (FentLib.varInstanceCommon.mobAggroValues.containsKey(((EntityLivingBase) (Object) this).getClass().getCanonicalName())) {
            entity.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) (Object) this), FentLib.varInstanceCommon.mobAggroValues.get(((EntityLivingBase) (Object) this).getClass().getCanonicalName()));
        }
    }
}
