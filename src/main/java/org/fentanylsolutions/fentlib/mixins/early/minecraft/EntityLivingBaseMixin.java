package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.MixinTargetAnnotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@MixinTargetAnnotation.MixinTarget(phase = MixinTargetAnnotation.Phase.EARLY)
@Mixin(value = EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {

    private EntityLivingBase thiz() {
        return (EntityLivingBase) (Object) this;
    }

    @Inject(method = "attackEntityAsMob", at = @At("TAIL"), require = 1)
    public void attackEntityAsMob(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (FentLib.varInstanceCommon.passiveMobsWhichCanInflictDamage.contains(thiz().getClass())) {
            entity.attackEntityFrom(DamageSource.causeMobDamage(thiz()), 1);
        }
    }
}
