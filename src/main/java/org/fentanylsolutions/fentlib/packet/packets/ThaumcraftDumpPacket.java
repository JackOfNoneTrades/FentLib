package org.fentanylsolutions.fentlib.packet.packets;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.misc.ThaumcraftDumper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class ThaumcraftDumpPacket implements IMessageHandler<ThaumcraftDumpPacket.SimpleMessage, IMessage> {

    @Override
    public IMessage onMessage(SimpleMessage message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            FentLib.LOG.info("Received dump thaumonomicon packet");
            ThaumcraftDumper.dumpThaumonomicon(message.customMessage);
            return null;
        }
        return null;
    }

    public static class SimpleMessage implements IMessage {

        public String customMessage = "";

        // this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
        public SimpleMessage() {}

        public SimpleMessage(String message) {
            this.customMessage = message;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int length = buf.readInt();
            if (length > 0) {
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                customMessage = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            byte[] bytes = customMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        }
    }
}
