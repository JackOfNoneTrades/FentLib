package org.fentanylsolutions.fentlib.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import me.saharnooby.qoi.QOIImage;
import me.saharnooby.qoi.QOIUtil;

/**
 * Minecraft-friendly wrappers around QOI image encoding/decoding.
 */
public final class QoiUtil {

    private QoiUtil() {}

    // --- Decoding ---

    public static BufferedImage readImage(byte[] qoiBytes) throws IOException {
        QOIImage qoi = QOIUtil.readImage(new ByteArrayInputStream(qoiBytes));
        return toBufferedImage(qoi);
    }

    public static BufferedImage readImage(InputStream in) throws IOException {
        QOIImage qoi = QOIUtil.readImage(in);
        return toBufferedImage(qoi);
    }

    public static BufferedImage readImage(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return readImage(fis);
        }
    }

    public static BufferedImage readImage(ResourceLocation location) throws IOException {
        try (InputStream in = net.minecraft.client.Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(location)
            .getInputStream()) {
            return readImage(in);
        }
    }

    // --- Encoding ---

    public static byte[] encode(BufferedImage image) throws IOException {
        QOIImage qoi = fromBufferedImage(image);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        QOIUtil.writeImage(qoi, out);
        return out.toByteArray();
    }

    public static void writeImage(BufferedImage image, File file) throws IOException {
        QOIImage qoi = fromBufferedImage(image);
        QOIUtil.writeImage(qoi, file);
    }

    // --- Minecraft texture helpers ---

    public static DynamicTexture readDynamicTexture(byte[] qoiBytes) throws IOException {
        return new DynamicTexture(readImage(qoiBytes));
    }

    public static DynamicTexture readDynamicTexture(InputStream in) throws IOException {
        return new DynamicTexture(readImage(in));
    }

    public static DynamicTexture readDynamicTexture(ResourceLocation location) throws IOException {
        return new DynamicTexture(readImage(location));
    }

    // --- Conversion ---

    public static BufferedImage toBufferedImage(QOIImage qoi) {
        int w = qoi.getWidth();
        int h = qoi.getHeight();
        int channels = qoi.getChannels();
        byte[] pixels = qoi.getPixelData();

        boolean hasAlpha = channels == 4;
        BufferedImage image = new BufferedImage(
            w,
            h,
            hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        int[] rgbArray = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            int offset = i * channels;
            int r = pixels[offset] & 0xFF;
            int g = pixels[offset + 1] & 0xFF;
            int b = pixels[offset + 2] & 0xFF;
            int a = hasAlpha ? (pixels[offset + 3] & 0xFF) : 0xFF;
            rgbArray[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        image.setRGB(0, 0, w, h, rgbArray, 0, w);
        return image;
    }

    public static QOIImage fromBufferedImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        boolean hasAlpha = image.getColorModel()
            .hasAlpha();
        int channels = hasAlpha ? 4 : 3;

        int[] rgbArray = image.getRGB(0, 0, w, h, null, 0, w);
        byte[] pixels = new byte[w * h * channels];
        for (int i = 0; i < w * h; i++) {
            int argb = rgbArray[i];
            int offset = i * channels;
            pixels[offset] = (byte) ((argb >> 16) & 0xFF);
            pixels[offset + 1] = (byte) ((argb >> 8) & 0xFF);
            pixels[offset + 2] = (byte) (argb & 0xFF);
            if (hasAlpha) {
                pixels[offset + 3] = (byte) ((argb >> 24) & 0xFF);
            }
        }
        return QOIUtil.createFromPixelData(pixels, w, h, channels);
    }
}
