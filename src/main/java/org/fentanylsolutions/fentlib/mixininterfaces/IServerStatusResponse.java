package org.fentanylsolutions.fentlib.mixininterfaces;

import com.google.gson.JsonElement;

public interface IServerStatusResponse {

    void setExtraData(JsonElement extraData);

    JsonElement getExtraData();
}
