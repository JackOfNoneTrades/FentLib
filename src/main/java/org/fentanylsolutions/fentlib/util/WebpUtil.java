package org.fentanylsolutions.fentlib.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

/**
 * Minecraft-friendly wrappers around WebP image decoding.
 * Uses TwelveMonkeys ImageIO WebP plugin directly (no SPI discovery needed).
 */
public final class WebpUtil {

    private static final WebPImageReaderSpi SPI = new WebPImageReaderSpi();

    private WebpUtil() {}

    // --- Decoding ---

    public static BufferedImage readImage(byte[] webpBytes) throws IOException {
        return readImage(new ByteArrayInputStream(webpBytes));
    }

    public static BufferedImage readImage(InputStream in) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        if (iis == null) {
            throw new IOException("Could not create ImageInputStream");
        }
        try {
            ImageReader reader = SPI.createReaderInstance();
            reader.setInput(iis);
            try {
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } finally {
            iis.close();
        }
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

    // --- Minecraft texture helpers ---

    public static DynamicTexture readDynamicTexture(byte[] webpBytes) throws IOException {
        return new DynamicTexture(readImage(webpBytes));
    }

    public static DynamicTexture readDynamicTexture(InputStream in) throws IOException {
        return new DynamicTexture(readImage(in));
    }

    public static DynamicTexture readDynamicTexture(ResourceLocation location) throws IOException {
        return new DynamicTexture(readImage(location));
    }

    // --- Re-encoding (WebP -> PNG bytes, since TwelveMonkeys WebP is read-only) ---

    public static byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }
}
