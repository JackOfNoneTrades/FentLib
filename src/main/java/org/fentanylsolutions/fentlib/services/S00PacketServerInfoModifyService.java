package org.fentanylsolutions.fentlib.services;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.network.ServerStatusResponse;

import org.fentanylsolutions.fentlib.FentLib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class S00PacketServerInfoModifyService {

    private static final List<BiConsumer<ServerStatusResponse, JsonObject>> handlers = new ArrayList<>();
    private static final Gson gson = new Gson();

    private static final JsonObject internalExtraData = new JsonObject();

    /**
     * Register a handler to modify the server response.
     *
     * @param handler A BiConsumer that takes ServerStatusResponse and JsonObject (extraData)
     */
    public static void registerHandler(BiConsumer<ServerStatusResponse, JsonObject> handler) {
        handlers.add(handler);
    }

    /**
     * Modify the server response using all registered handlers.
     *
     * @param response  The ServerStatusResponse to modify
     * @param extraData A string containing extra JSON data
     */
    public static void modify(ServerStatusResponse response, String extraData) {
        JsonObject json;

        try {
            json = gson.fromJson(extraData, JsonObject.class);
        } catch (JsonSyntaxException e) {
            FentLib.LOG.error("[S00PacketServerInfoModifyService] Failed to parse extraData: " + e.getMessage());
            return;
        }

        for (BiConsumer<ServerStatusResponse, JsonObject> handler : handlers) {
            try {
                handler.accept(response, json);
            } catch (Exception e) {
                FentLib.LOG.error("[S00PacketServerInfoModifyService] Handler threw exception: " + e.getMessage());
                FentLib.LOG.error(e);
            }
        }
    }

    /**
     * Add or replace a string key-value pair.
     */
    public static void put(String key, String value) {
        internalExtraData.addProperty(key, value);
    }

    /**
     * Add or replace a primitive value.
     */
    public static void put(String key, JsonElement value) {
        internalExtraData.add(key, value);
    }

    /**
     * Remove a field by key.
     */
    public static void remove(String key) {
        internalExtraData.remove(key);
    }

    /**
     * Clear all internal extra data.
     */
    public static void clear() {
        internalExtraData.entrySet()
            .clear();
    }

    /**
     * Get the internal extra data as a JSON string.
     */
    public static String getAsString() {
        return gson.toJson(internalExtraData);
    }
}
