package org.fentanylsolutions.fentlib.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.DynamicTexture;

import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.FentLib;

import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;

public class GifUtil {

    public static class GifData {

        private final BufferedImage[] frames;
        private final int[] delaysMs;

        public GifData(BufferedImage[] frames, int[] delaysMs) {
            this.frames = frames;
            this.delaysMs = delaysMs;
        }

        public int getFrameCount() {
            return frames.length;
        }

        public BufferedImage getFrame(int index) {
            return frames[index];
        }

        public int getDelayMs(int index) {
            return delaysMs[index];
        }
    }

    public static class GifAnimationData {

        public final DynamicTexture dynamicTexture;
        public final int frameWidth;
        public final int frameHeight;
        public final int frameCount;
        public final int frameDelayMs;

        public GifAnimationData(DynamicTexture tex, int frameWidth, int frameHeight, int frameCount, int frameDelayMs) {
            this.dynamicTexture = tex;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.frameCount = frameCount;
            this.frameDelayMs = frameDelayMs;
        }
    }

    public static class StitchedAnimationData {

        public final byte[] stichedData;
        public final int frameWidth;
        public final int frameHeight;
        public final int frameCount;
        public final int frameDelayMs;

        public StitchedAnimationData(byte[] stichedData, int frameWidth, int frameHeight, int frameCount,
            int frameDelayMs) {
            this.stichedData = stichedData;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.frameCount = frameCount;
            this.frameDelayMs = frameDelayMs;
        }
    }

    /**
     * Reads an animated GIF into a GifData using the configured backend.
     */
    public static GifData readGif(byte[] gifBytes) throws IOException {
        if (Config.useNativeGifReader) {
            FentLib.debug("Reading GIF with native reader");
            return NativeGifReader.read(gifBytes);
        }
        FentLib.debug("Reading GIF with scrimage reader");
        return readGifWithScrimage(gifBytes);
    }

    private static GifData readGifWithScrimage(byte[] gifBytes) throws IOException {
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(new ByteArrayInputStream(gifBytes)));
        int frameCount = gif.getFrameCount();
        if (frameCount == 0) {
            throw new IOException("No frames found in GIF");
        }

        BufferedImage[] frames = new BufferedImage[frameCount];
        int[] delays = new int[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = gif.getFrame(i)
                .awt();
            delays[i] = (int) gif.getDelay(i)
                .toMillis();
        }
        return new GifData(frames, delays);
    }

    /**
     * Scales a frame to the target dimensions using progressive bilinear downscaling.
     * Repeatedly halves each dimension until close to the target, then does one final
     * bicubic step. This avoids the quality loss of a single large bilinear step.
     */
    public static BufferedImage scaleFrame(BufferedImage src, int targetW, int targetH) {
        if (src.getWidth() == targetW && src.getHeight() == targetH) {
            return src;
        }

        BufferedImage current = src;
        int w = current.getWidth();
        int h = current.getHeight();

        // Progressive halving for downscaling
        while (w > targetW * 2 || h > targetH * 2) {
            int nextW = Math.max(w / 2, targetW);
            int nextH = Math.max(h / 2, targetH);
            BufferedImage step = new BufferedImage(nextW, nextH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = step.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(current, 0, 0, nextW, nextH, null);
            g.dispose();
            current = step;
            w = nextW;
            h = nextH;
        }

        // Final step with bicubic
        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(current, 0, 0, targetW, targetH, null);
        g.dispose();
        return result;
    }

    /**
     * Stitches GIF frames into a horizontal sprite sheet.
     */
    public static BufferedImage stitchGif(GifData gif, int frameW, int frameH) {
        int frameCount = gif.getFrameCount();
        BufferedImage spriteSheet = new BufferedImage(frameW * frameCount, frameH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = spriteSheet.createGraphics();
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = scaleFrame(gif.getFrame(i), frameW, frameH);
            g.drawImage(frame, i * frameW, 0, null);
        }
        g.dispose();
        return spriteSheet;
    }

    /**
     * Returns the first frame's delay in ms, defaulting to 1000 if zero.
     */
    public static int getFrameDelay(GifData gif) {
        int delay = gif.getDelayMs(0);
        return delay > 0 ? delay : 1000;
    }

    /**
     * Loads a GIF from byte array, extracts and resizes frames,
     * stitches them into a single horizontal strip, and returns
     * a DynamicTexture along with animation info.
     */
    public static GifAnimationData loadGifFromBytes(byte[] gifBytes, int frameW, int frameH) throws IOException {
        GifData gif = readGif(gifBytes);
        int frameDelayMs = getFrameDelay(gif);
        BufferedImage stitchedImage = stitchGif(gif, frameW, frameH);
        DynamicTexture tex = new DynamicTexture(stitchedImage);
        return new GifAnimationData(tex, frameW, frameH, gif.getFrameCount(), frameDelayMs);
    }

    public static byte[] bufferedImageToByteArray(BufferedImage img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);
        } catch (IOException e) {
            return null;
        }
        try {
            baos.flush();
        } catch (IOException e) {
            return null;
        }
        byte[] byteArray = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            return null;
        }
        return byteArray;
    }

    public static BufferedImage byteArrayToBufferedImage(byte[] arr) {
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            return null;
        }
    }

    public static StitchedAnimationData stitchedFromBytes(byte[] gifBytes, int frameW, int frameH) {
        GifData gif;
        try {
            gif = readGif(gifBytes);
        } catch (IOException e) {
            return null;
        }
        int frameDelayMs = getFrameDelay(gif);

        BufferedImage stitchedImage = stitchGif(gif, frameW, frameH);
        byte[] imgBytes = bufferedImageToByteArray(stitchedImage);
        if (imgBytes == null) {
            return null;
        }
        return new StitchedAnimationData(imgBytes, frameW, frameH, gif.getFrameCount(), frameDelayMs);
    }

    public static void validateStitchedData(StitchedAnimationData data) throws IOException {
        if (data.frameWidth != 32 || data.frameHeight != 32) {
            throw new IOException(
                "Invalid GIF dimensions: must be 32x32, got " + data.frameWidth + "x" + data.frameHeight);
        }
        if (data.frameCount <= 0 || data.frameCount > Config.maxGifFrameCount) {
            throw new IOException(
                "Invalid frame count: " + data.frameCount + "(maximum configured: " + Config.maxGifFrameCount + ")");
        }
        if (data.frameDelayMs < 1 || data.frameDelayMs > 60000) {
            throw new IOException("Invalid frame delay: " + data.frameDelayMs);
        }

        if (data.stichedData == null) {
            throw new IOException("Stitched image data is null");
        }

        int imgLength = data.stichedData.length;
        if (imgLength <= 0 || imgLength > Config.gifSizeCap * 1024 * 1024) {
            throw new IOException(
                "Invalid stitched image size: " + imgLength
                    + "(maximum configured: "
                    + Config.gifSizeCap * 1024 * 1024
                    + ")");
        }
    }

    public static byte[] serializeStitchedData(StitchedAnimationData data) throws IOException {
        validateStitchedData(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(data.frameWidth);
        dos.writeInt(data.frameHeight);
        dos.writeInt(data.frameCount);
        dos.writeInt(data.frameDelayMs);

        dos.writeInt(data.stichedData.length);
        dos.write(data.stichedData);

        dos.flush();
        return out.toByteArray();
    }

    public static StitchedAnimationData deserializeStitchedData(byte[] bytes) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        int frameW = dis.readInt();
        int frameH = dis.readInt();
        int frameCount = dis.readInt();
        int frameDelayMs = dis.readInt();

        int imgLength = dis.readInt();
        byte[] imgBytes = new byte[imgLength];
        dis.readFully(imgBytes);

        StitchedAnimationData res = new StitchedAnimationData(imgBytes, frameW, frameH, frameCount, frameDelayMs);
        validateStitchedData(res);
        return res;
    }
}
