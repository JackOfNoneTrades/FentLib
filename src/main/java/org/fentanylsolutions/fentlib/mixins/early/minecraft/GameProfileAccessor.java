package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameProfile.class)
public interface GameProfileAccessor {
    @Accessor(remap = false)
    void setName(String name);
}
