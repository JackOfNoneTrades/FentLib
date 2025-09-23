package org.fentanylsolutions.fentlib.eventlisteners;

import java.nio.charset.StandardCharsets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import io.netty.buffer.ByteBuf;

public class CustomPayloadListener {
    /*
     * @SubscribeEvent
     * public void onCustomPayload(FMLNetworkEvent.CustomPacketEvent event) {
     * if (!(event.getPacket() instanceof net.minecraft.network.play.client.C17PacketCustomPayload)) return;
     * C17PacketCustomPayload packet = (C17PacketCustomPayload) event.getPacket();
     * String channel = packet.getChannelName();
     * if ("sneed".equals(channel)) {
     * PacketBuffer data = (PacketBuffer) packet.getBufferData();
     * try {
     * byte[] bytes = new byte[data.readableBytes()];
     * data.readBytes(bytes);
     * String message = new String(bytes, StandardCharsets.UTF_8);
     * System.out.println("Received on 'sneed': " + message);
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * }
     * }
     */

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        ByteBuf buf = event.packet.payload();

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        String msg = new String(data, StandardCharsets.UTF_8);

        System.out.println("Received packet on 'sneed': " + msg);
    }
}
