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

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

/**
 * WebP image decoding. Safe for both client and server.
 * For client-only helpers (DynamicTexture, ResourceLocation), see {@link WebpClientUtil}.
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

    // --- Re-encoding (WebP -> PNG bytes, since TwelveMonkeys WebP is read-only) ---

    public static byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }
}
