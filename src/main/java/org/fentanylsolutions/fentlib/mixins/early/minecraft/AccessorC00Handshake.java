package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.network.handshake.client.C00Handshake;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C00Handshake.class)
public interface AccessorC00Handshake {

    @Accessor
    String getField_149598_b();
}
