package org.fentanylsolutions.fentlib.services;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.ServerStatusResponse;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IServerStatusResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class S00PacketServerInfoModifyService {

    private static final List<BiFunction<ServerStatusResponse, Boolean, Object>> modifyHandlers = new ArrayList<>();
    private static final List<TriConsumer<ServerStatusResponse, JsonObject, ServerData>> deserializeHandlers = new ArrayList<>();
    private static final Gson gson = new Gson();

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {

        void accept(A a, B b, C c);
    }

    public static class KeyValue {

        public final String key;
        public final JsonElement value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = new JsonPrimitive(value);
        }

        public KeyValue(String key, JsonElement value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Register a handler to modify the server response.
     *
     * @param handler A BiConsumer that takes ServerStatusResponse and JsonObject (extraData)
     */
    public static void registerHandler(BiFunction<ServerStatusResponse, Boolean, ?> handler) {
        modifyHandlers.add((BiFunction<ServerStatusResponse, Boolean, Object>) handler);
    }

    public static void registerDeserializeHandler(TriConsumer<ServerStatusResponse, JsonObject, ServerData> handler) {
        deserializeHandlers.add(handler);
    }

    /**
     * Modify the server response using all registered handlers.
     *
     * @param response       The ServerStatusResponse to modify
     * @param fentLibPresent A string containing extra JSON data
     */
    public static void modify(ServerStatusResponse response, boolean fentLibPresent) {

        JsonObject fentlibPayload = new JsonObject();

        for (BiFunction<ServerStatusResponse, Boolean, Object> handler : modifyHandlers) {
            try {
                Object result = handler.apply(response, fentLibPresent);
                if (result != null) {
                    if (result instanceof String) {
                        fentlibPayload.add((String) result, new JsonPrimitive(""));
                    } else if (result instanceof KeyValue) {
                        KeyValue kv = (KeyValue) result;
                        fentlibPayload.add(kv.key, kv.value);
                    }
                }
            } catch (Exception e) {
                FentLib.LOG
                    .error("[S00PacketServerInfoModifyService] Modify handler threw exception: " + e.getMessage());
                FentLib.LOG.error(e);
            }
        }

        if (!fentlibPayload.entrySet()
            .isEmpty()) {
            ((IServerStatusResponse) response).setExtraData(fentlibPayload);
        }
    }

    public static void callDeserializeHandlers(ServerStatusResponse response, ServerData serverData) {
        JsonElement extra = ((IServerStatusResponse) response).getExtraData();
        if (extra != null && extra.isJsonObject()) {
            for (TriConsumer<ServerStatusResponse, JsonObject, ServerData> handler : deserializeHandlers) {
                try {
                    handler.accept(response, extra.getAsJsonObject(), serverData);
                } catch (Exception e) {
                    FentLib.LOG.error(
                        "[S00PacketServerInfoModifyService] Deserialize handler threw exception: " + e.getMessage());
                    FentLib.LOG.error(e);
                }
            }
        }
    }
}
