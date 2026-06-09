package org.fentanylsolutions.fentlib.mixins.late.betterquesting;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

@SuppressWarnings("UnresolvedMixinReference")
@Pseudo
@Mixin(targets = "betterquesting.api2.utils.EntityPlayerPreview", priority = 999, remap = false)
public abstract class MixinEntityPlayerPreview extends EntityOtherPlayerMP {

    private MixinEntityPlayerPreview(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "func_110306_p", at = @At("HEAD"), cancellable = true, remap = false)
    private void fentlib$useVanillaSkinPipeline(CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(super.getLocationSkin());
    }
}
