package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.network.NetworkManager;
import net.minecraft.server.network.NetHandlerStatusServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetHandlerStatusServer.class)
public interface AccessorNetHandlerStatusServer {

    @Accessor
    NetworkManager getField_147313_b();
}
