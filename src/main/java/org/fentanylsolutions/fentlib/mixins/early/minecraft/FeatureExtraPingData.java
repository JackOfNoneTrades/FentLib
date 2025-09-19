package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.io.IOException;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerStatusServer;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IC00PacketServerQuery;
import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FeatureExtraPingData {

    @Mixin(C00PacketServerQuery.class)
    public static class MixinC00PacketServerQuery implements IC00PacketServerQuery {

        @Unique
        String extraData;

        @Override
        public String getExtraData() {
            return this.extraData;
        }

        @Override
        public void setExtraData(String data) {
            this.extraData = data;
        }

        @Inject(method = "writePacketData", at = @At("TAIL"))
        private void writeClientCapabilities(PacketBuffer data, CallbackInfo ci) throws IOException {
            String extra = S00PacketServerInfoModifyService.getAsString();
            FentLib.debug("Writing extra data in writePacketData: " + extra);
            data.writeStringToBuffer(extra);
        }

        @Inject(method = "readPacketData", at = @At("TAIL"))
        private void readClientCapabilities(PacketBuffer data, CallbackInfo ci) throws IOException {
            // SERVER SIDE: Try to read capabilities, fallback gracefully for vanilla clients
            FentLib.debug("Reading extra data in readPacketData");
            try {
                // Check if there's data remaining in the buffer
                if (data.readableBytes() > 0) {
                    this.extraData = data.readStringFromBuffer(Integer.MAX_VALUE / 5);
                    FentLib.debug("Extra data: " + this.extraData);
                } else {
                    // Empty packet buffer = vanilla client
                    this.extraData = "";
                }
            } catch (Exception e) {
                // Any read error = vanilla client
                this.extraData = "";
            }
        }
    }

    @Mixin(NetHandlerStatusServer.class)
    public static class NetHandlerStatusServerMixin {

        @Shadow
        @Final
        NetworkManager field_147313_b;

        @Shadow
        @Final
        MinecraftServer field_147314_a;

        @Inject(method = "processServerQuery", at = @At("HEAD"))
        private void onProcessServerQuery(C00PacketServerQuery packetIn, CallbackInfo ci) {
            FentLib.debug("[Mixin] Received Server Query Packet: " + packetIn);
            String extraData = ((IC00PacketServerQuery) packetIn).getExtraData();
            FentLib.debug("[Mixin] Packet extra data: '" + extraData + "'");

            if (!extraData.isEmpty()) {
                FentLib.debug("[Mixin] Modifying outbound packet");
                // Call to a handler like this S00PacketServerInfoModifyService.modify(field_147314_a.func_147134_at(),
                // extraData);
                // S00PacketServerInfoModifyService.modify parses extraData into a Gson JsonObject
                // S00PacketServerInfoModifyService has a list of functions that take a ServerStatusResponse and a
                // JsonObject
                // those functions apply whatever they want to the ServerStatusResponse
                S00PacketServerInfoModifyService.modify(field_147314_a.func_147134_at(), extraData);
                // field_147314_a.func_147134_at().func_151319_a(new ServerStatusResponse.PlayerCountData(-69, -420));
            }
        }

        /*
         * @Inject(method = "processPing", at = @At("HEAD"))
         * private void onProcessPing(C01PacketPing packetIn, CallbackInfo ci) {
         * System.out.println("[Mixin] Received Ping Packet: " + packetIn);
         * }
         */
    }
}
