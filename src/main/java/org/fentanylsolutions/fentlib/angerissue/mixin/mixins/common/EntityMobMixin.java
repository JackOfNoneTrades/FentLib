package org.fentanylsolutions.fentlib.angerissue.mixin.mixins.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityMob.class)
public abstract class EntityMobMixin {

    @Inject(method = "attackEntityAsMob", at = @At("HEAD"), require = 1)
    public void attackEntityAsMob(Entity p_70652_1_, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("BRUH (mob)");
    }
}
