package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.io.IOException;
import java.lang.reflect.Type;

import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerStatusServer;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IC00PacketServerQuery;
import org.fentanylsolutions.fentlib.mixininterfaces.IServerStatusResponse;
import org.fentanylsolutions.fentlib.packet.FentPing;
import org.fentanylsolutions.fentlib.services.S00PacketServerInfoModifyService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import cpw.mods.fml.client.FMLClientHandler;
import io.netty.util.concurrent.GenericFutureListener;

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
            // data.writeStringToBuffer(extra);
        }

        @Inject(method = "readPacketData", at = @At("TAIL"))
        private void readClientCapabilities(PacketBuffer data, CallbackInfo ci) throws IOException {
            FentLib.debug("Reading extra data in readPacketData");
            try {
                if (data.readableBytes() > 0) {
                    this.extraData = data.readStringFromBuffer(Integer.MAX_VALUE / 5);
                    FentLib.debug("Extra data: " + this.extraData);
                } else {
                    this.extraData = "";
                }
            } catch (Exception e) {
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

            if (extraData.isEmpty()) {
                extraData = "{}";
            }

            FentLib.debug("[Mixin] Modifying outbound packet");
            S00PacketServerInfoModifyService.modify(field_147314_a.func_147134_at(), extraData);
        }

        /*
         * @Inject(method = "processPing", at = @At("HEAD"))
         * private void onProcessPing(C01PacketPing packetIn, CallbackInfo ci) {
         * System.out.println("[Mixin] Received Ping Packet: " + packetIn);
         * }
         */
    }

    @Mixin(ServerStatusResponse.class)
    public static class MixinServerStatusResponse implements IServerStatusResponse {

        @Unique
        private JsonElement extraData;

        @Override
        public void setExtraData(JsonElement extraData) {
            this.extraData = extraData;
        }

        @Override
        public JsonElement getExtraData() {
            return this.extraData;
        }
    }

    @Mixin(ServerStatusResponse.Serializer.class)
    public static class ServerStatusResponseSerializerMixin {

        @Inject(method = "serialize", at = @At("RETURN"), cancellable = true)
        private void injectFentLibEnhancement(ServerStatusResponse response, Type type,
            JsonSerializationContext context, CallbackInfoReturnable<JsonElement> cir) {
            JsonElement returnValue = cir.getReturnValue();
            if (!(returnValue instanceof JsonObject)) return;
            JsonObject json = (JsonObject) returnValue;
            JsonElement extraData = ((IServerStatusResponse) response).getExtraData();
            if (extraData != null) {
                json.add(FentLib.MODID, extraData);
            }
            cir.setReturnValue(json);
        }

        @Inject(method = "deserialize", at = @At("RETURN"))
        private void interceptFentLibEnhancement(JsonElement jsonElement, Type type,
            com.google.gson.JsonDeserializationContext context, CallbackInfoReturnable<ServerStatusResponse> cir) {
            if (!(jsonElement instanceof JsonObject)) return;
            JsonObject rootJson = (JsonObject) jsonElement;
            JsonElement fentlibData = rootJson.get(FentLib.MODID);
            if (fentlibData == null || !fentlibData.isJsonObject()) return;
            ServerStatusResponse response = cir.getReturnValue();
            JsonObject fentlibJson = fentlibData.getAsJsonObject();
            ((IServerStatusResponse) response).setExtraData(fentlibJson);
        }

    }

    @Mixin(value = FMLClientHandler.class, remap = false)
    public static class MixinFMLClientHandler {

        @Inject(method = "bindServerListData", at = @At("TAIL"))
        private void fentlib$handleFentLibDeserialization(ServerData data, ServerStatusResponse originalResponse,
            CallbackInfo ci) {
            System.out.println("fentlib$handleFentLibDeserialization hook");
            JsonElement extra = ((IServerStatusResponse) originalResponse).getExtraData();
            System.out.println(extra);

            if (extra != null && extra.isJsonObject()) {
                S00PacketServerInfoModifyService.callDeserializeHandlers(originalResponse, data);
            }
        }
    }

    @Mixin(OldServerPinger.class)
    public static class MixinOldServerPinger {

        @Inject(
            method = "func_147224_a",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/network/NetworkManager;scheduleOutboundPacket(Lnet/minecraft/network/Packet;[Lio/netty/util/concurrent/GenericFutureListener;)V",
                ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
        private void injectFentPing(ServerData server, CallbackInfo ci, ServerAddress serverAddress,
            NetworkManager networkManager) {
            // Construct your custom FentPing data
            String extra = "{\"fentlib\":true,\"version\":\"1.0.0\"}";
            FentPing fentPing = new FentPing(extra);

            // Send your packet before the standard ServerQuery
            networkManager.scheduleOutboundPacket(fentPing, new GenericFutureListener[0]);
        }
    }
}
