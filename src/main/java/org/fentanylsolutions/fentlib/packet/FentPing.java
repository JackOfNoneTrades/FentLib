package org.fentanylsolutions.fentlib.packet;

import java.io.IOException;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

public class FentPing extends Packet {

    private String clientCapabilities;

    public FentPing() {}

    public FentPing(String clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.clientCapabilities = buf.readStringFromBuffer(Integer.MAX_VALUE / 5);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeStringToBuffer(this.clientCapabilities);
    }

    @Override
    public void processPacket(INetHandler handler) {
        // Only send from client to server; no processing on client side
    }

    public String getClientCapabilities() {
        return clientCapabilities;
    }
}
