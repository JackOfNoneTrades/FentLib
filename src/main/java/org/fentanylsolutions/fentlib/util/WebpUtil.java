package org.fentanylsolutions.fentlib.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

/**
 * WebP image decoding, including animated WebP support.
 * Safe for both client and server.
 * For client-only helpers (DynamicTexture, ResourceLocation), see {@link WebpClientUtil}.
 */
public final class WebpUtil {

    private static final WebPImageReaderSpi SPI = new WebPImageReaderSpi();

    private static final int FOURCC_RIFF = 0x46464952; // "RIFF" in LE
    private static final int FOURCC_WEBP = 0x50424557; // "WEBP" in LE
    private static final int FOURCC_VP8X = 0x58385056; // "VP8X" in LE
    private static final int FOURCC_ANMF = 0x464D4E41; // "ANMF" in LE

    private WebpUtil() {}

    // --- Static decoding ---

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
                return ImageUtil.copyToArgb(reader.read(0));
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

    // --- Animated WebP ---

    /**
     * Reads an animated WebP into a {@link GifUtil.GifData} (frames + per-frame delays).
     * Composites frames onto a canvas respecting ANMF offsets, blending, and disposal.
     * Falls back to a single-frame result for static WebP files.
     */
    public static GifUtil.GifData readAnimated(byte[] webpBytes) throws IOException {
        List<AnmfMeta> anmfFrames = parseAnmfMeta(webpBytes);
        int canvasWidth = parseCanvasWidth(webpBytes);
        int canvasHeight = parseCanvasHeight(webpBytes);
        Color bgColor = parseBackgroundColor(webpBytes);

        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(webpBytes));
        if (iis == null) {
            throw new IOException("Could not create ImageInputStream");
        }
        try {
            ImageReader reader = SPI.createReaderInstance();
            reader.setInput(iis);
            try {
                int frameCount = reader.getNumImages(true);
                if (frameCount == 0) {
                    throw new IOException("No frames found in WebP");
                }

                // If not animated (no ANMF chunks), return single frame
                if (anmfFrames == null || anmfFrames.isEmpty()) {
                    BufferedImage[] frames = new BufferedImage[] { ImageUtil.copyToArgb(reader.read(0)) };
                    return new GifUtil.GifData(frames, new int[] { 100 });
                }

                BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D canvasG = canvas.createGraphics();
                BufferedImage previousCanvas = null;

                BufferedImage[] compositedFrames = new BufferedImage[frameCount];
                int[] delays = new int[frameCount];

                for (int i = 0; i < frameCount; i++) {
                    BufferedImage rawFrame = ImageUtil.copyToArgb(reader.read(i));
                    AnmfMeta meta = i < anmfFrames.size() ? anmfFrames.get(i) : AnmfMeta.DEFAULT;

                    // Save canvas before drawing if we might need to restore
                    if (meta.dispose) {
                        previousCanvas = copyImage(canvas);
                    }

                    // Draw frame onto canvas
                    if (meta.blend) {
                        canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                    } else {
                        canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
                    }
                    canvasG.drawImage(rawFrame, meta.x, meta.y, null);

                    // Snapshot the composited canvas
                    compositedFrames[i] = copyImage(canvas);
                    delays[i] = meta.durationMs > 0 ? meta.durationMs : 100;

                    // Apply disposal
                    if (meta.dispose) {
                        // Dispose to background: clear the frame's region
                        canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
                        canvasG.setColor(bgColor);
                        canvasG.fillRect(meta.x, meta.y, meta.width, meta.height);
                        canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                    }
                    // If not dispose, canvas stays as-is for next frame
                }

                canvasG.dispose();
                return new GifUtil.GifData(compositedFrames, delays);
            } finally {
                reader.dispose();
            }
        } finally {
            iis.close();
        }
    }

    public static GifUtil.GifData readAnimated(InputStream in) throws IOException {
        return readAnimated(readAllBytes(in));
    }

    public static GifUtil.GifData readAnimated(File file) throws IOException {
        return readAnimated(Files.readAllBytes(file.toPath()));
    }

    /**
     * Returns true if the WebP file contains animation (VP8X with animation flag set).
     */
    public static boolean isAnimated(byte[] webpBytes) {
        if (webpBytes == null || webpBytes.length < 30) return false;
        ByteBuffer buf = ByteBuffer.wrap(webpBytes)
            .order(ByteOrder.LITTLE_ENDIAN);
        if (buf.getInt(0) != FOURCC_RIFF) return false;
        if (buf.getInt(8) != FOURCC_WEBP) return false;
        if (buf.getInt(12) != FOURCC_VP8X) return false;
        int flags = buf.get(20) & 0xFF;
        return (flags & 0x02) != 0;
    }

    // --- Re-encoding (WebP -> PNG bytes, since TwelveMonkeys WebP is read-only) ---

    public static byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }

    // --- RIFF/ANMF parsing ---

    private static final int FOURCC_ANIM = 0x4D494E41; // "ANIM" in LE

    private static int readUint24LE(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16);
    }

    private static int parseCanvasWidth(byte[] data) {
        if (data == null || data.length < 30) return 0;
        ByteBuffer buf = ByteBuffer.wrap(data)
            .order(ByteOrder.LITTLE_ENDIAN);
        if (buf.getInt(12) != FOURCC_VP8X) return 0;
        return readUint24LE(data, 24) + 1;
    }

    private static int parseCanvasHeight(byte[] data) {
        if (data == null || data.length < 30) return 0;
        ByteBuffer buf = ByteBuffer.wrap(data)
            .order(ByteOrder.LITTLE_ENDIAN);
        if (buf.getInt(12) != FOURCC_VP8X) return 0;
        return readUint24LE(data, 27) + 1;
    }

    private static Color parseBackgroundColor(byte[] data) {
        if (data == null || data.length < 30) return new Color(0, 0, 0, 0);
        ByteBuffer buf = ByteBuffer.wrap(data)
            .order(ByteOrder.LITTLE_ENDIAN);
        // Find ANIM chunk
        int pos = 12;
        while (pos + 8 <= data.length) {
            int fourcc = buf.getInt(pos);
            int chunkSize = buf.getInt(pos + 4);
            int payloadStart = pos + 8;
            if (fourcc == FOURCC_ANIM && payloadStart + 6 <= data.length) {
                // BGRA order in the spec
                int b = data[payloadStart] & 0xFF;
                int g = data[payloadStart + 1] & 0xFF;
                int r = data[payloadStart + 2] & 0xFF;
                int a = data[payloadStart + 3] & 0xFF;
                return new Color(r, g, b, a);
            }
            pos = payloadStart + chunkSize + (chunkSize & 1);
        }
        return new Color(0, 0, 0, 0);
    }

    /**
     * Parses full ANMF chunk metadata (offsets, dimensions, delays, blending/disposal).
     */
    private static List<AnmfMeta> parseAnmfMeta(byte[] data) {
        if (data == null || data.length < 30) return null;
        ByteBuffer buf = ByteBuffer.wrap(data)
            .order(ByteOrder.LITTLE_ENDIAN);

        if (buf.getInt(0) != FOURCC_RIFF) return null;
        if (buf.getInt(8) != FOURCC_WEBP) return null;

        List<AnmfMeta> frames = new ArrayList<>();
        int pos = 12;

        while (pos + 8 <= data.length) {
            int fourcc = buf.getInt(pos);
            int chunkSize = buf.getInt(pos + 4);
            int p = pos + 8;

            if (fourcc == FOURCC_ANMF && p + 16 <= data.length) {
                int x = readUint24LE(data, p) * 2;
                int y = readUint24LE(data, p + 3) * 2;
                int width = readUint24LE(data, p + 6) + 1;
                int height = readUint24LE(data, p + 9) + 1;
                int durationMs = readUint24LE(data, p + 12);
                int flags = data[p + 15] & 0xFF;
                boolean blend = (flags & 0x02) == 0; // 0 = alpha-blend, 1 = no blend
                boolean dispose = (flags & 0x01) != 0; // 1 = dispose to background
                frames.add(new AnmfMeta(x, y, width, height, durationMs, blend, dispose));
            }

            pos = p + chunkSize + (chunkSize & 1);
        }

        return frames.isEmpty() ? null : frames;
    }

    private static BufferedImage copyImage(BufferedImage src) {
        return ImageUtil.copyToNewArgb(src);
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    private static class AnmfMeta {

        static final AnmfMeta DEFAULT = new AnmfMeta(0, 0, 0, 0, 100, true, false);

        final int x;
        final int y;
        final int width;
        final int height;
        final int durationMs;
        final boolean blend; // true = alpha-blend with previous, false = overwrite
        final boolean dispose; // true = dispose to background after display

        AnmfMeta(int x, int y, int width, int height, int durationMs, boolean blend, boolean dispose) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.durationMs = durationMs;
            this.blend = blend;
            this.dispose = dispose;
        }
    }
}
