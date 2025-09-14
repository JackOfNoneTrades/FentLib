package org.fentanylsolutions.fentlib.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

public class PacketUtil {

    /* Read and write longs in forge packets */
    public static long readVarLong(ByteBuf from) {
        long varlong = 0L;
        int length = 0;
        long part;
        do {
            part = from.readByte();
            varlong |= (part & 0x7F) << (length++ * 7);
            if (length > 10) {
                throw new DecoderException("VarLong too big");
            }
        } while ((part & 0x80) == 0x80);
        return varlong;
    }

    public static void writeVarLong(ByteBuf to, long varlong) {
        while ((varlong & 0xFFFFFFFFFFFFFF80L) != 0x0L) {
            to.writeByte((int) (varlong & 0x7FL) | 0x80);
            varlong >>>= 7;
        }
        to.writeByte((int) varlong);
    }
}
