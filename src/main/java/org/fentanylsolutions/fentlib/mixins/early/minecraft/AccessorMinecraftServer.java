package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftServer.class)
public interface AccessorMinecraftServer {

    @Accessor
    ServerStatusResponse getField_147147_p();

    @Invoker
    void invokeFunc_147138_a(ServerStatusResponse response);
}
