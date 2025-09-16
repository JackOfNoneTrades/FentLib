package org.fentanylsolutions.fentlib.mixins;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ServerStatusResponse;

import com.google.gson.Gson;

public class Debug {

    protected static void readPacketData(PacketBuffer data, ServerStatusResponse field_149296_b, Gson field_149297_a) {
        String json = null;
        try {
            json = data.readStringFromBuffer(32767);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("sneederino maximo:");
        System.out.println(json);
        System.out.println(field_149297_a);

        System.out.println(field_149296_b);
        field_149296_b = field_149297_a.fromJson(json, ServerStatusResponse.class);
        System.out.println(field_149296_b);
    }
}
