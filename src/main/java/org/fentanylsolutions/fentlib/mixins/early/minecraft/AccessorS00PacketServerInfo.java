package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.server.S00PacketServerInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(S00PacketServerInfo.class)
public interface AccessorS00PacketServerInfo {

    @Accessor
    void setField_149296_b(ServerStatusResponse val);
}
