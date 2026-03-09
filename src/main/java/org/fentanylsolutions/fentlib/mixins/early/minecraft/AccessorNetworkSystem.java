package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.util.List;

import net.minecraft.network.NetworkSystem;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NetworkSystem.class)
public interface AccessorNetworkSystem {

    @Accessor("networkManagers")
    List getNetworkManagers();

    @Accessor("mcServer")
    MinecraftServer getMcServer();
}
