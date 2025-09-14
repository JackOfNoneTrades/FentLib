package org.fentanylsolutions.fentlib.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.fentanylsolutions.fentlib.FentLib;

public class ByteUtil {

    public static byte[] fillByteArrayLeading(byte[] a, int totalLen) throws IOException {
        if (a.length >= totalLen) {
            return a;
        }
        byte[] b = new byte[totalLen - a.length];
        return concatByteArrays(b, a);
    }

    public static byte[] concatByteArrays(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);

        return outputStream.toByteArray();
    }

    public static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4)
            .putInt(i)
            .array();
    }

    public static int fourFirstBytesToInt(byte[] array) {
        if (array.length < 4) {
            return -1;
        }
        byte[] temp = new byte[4];
        for (int i = 0; i < 4; i++) {
            temp[i] = array[i];
        }
        return ByteBuffer.wrap(temp)
            .getInt();
    }

    public static byte[] prependLengthToByteArray(byte[] byteArray) {
        return prependLengthToByteArray(byteArray, byteArray.length);
    }

    public static byte[] prependLengthToByteArray(byte[] byteArray, int length) {
        byte[] intBytes = intToByteArray(length);
        try {
            return concatByteArrays(intBytes, byteArray);
        } catch (IOException e) {
            FentLib.LOG.error("Failed to concatenate byte arrays!");
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] removeFourLeadingBytes(byte[] byteArray) {
        return removeXLeadingBytes(4, byteArray);
    }

    public static byte[] removeXLeadingBytes(int amount, byte[] byteArray) {
        if (byteArray.length < amount) {
            return new byte[0];
        }
        byte[] res = new byte[byteArray.length - amount];
        for (int i = amount; i < byteArray.length; i++) {
            res[i - amount] = byteArray[i];
        }
        return res;
    }

    public static byte[] getXLeadingBytes(int amount, byte[] byteArray) {
        byte[] res = new byte[amount];
        for (int i = 0; i < amount; i++) {
            if (i < byteArray.length) {
                res[i] = byteArray[i];
            }
        }
        return res;
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    public static void writeBytes(File file, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    public static byte[] readFileBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
}
