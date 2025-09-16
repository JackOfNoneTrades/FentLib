package org.fentanylsolutions.fentlib.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.DynamicTexture;

import org.fentanylsolutions.fentlib.Config;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;

public class GifUtil {

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

    public static AnimatedGif bytesToGif(byte[] gifBytes) throws IOException {
        AnimatedGif res = AnimatedGifReader.read(ImageSource.of(new ByteArrayInputStream(gifBytes)));

        int frameCount = res.getFrameCount();
        if (frameCount == 0) {
            throw new IOException("No frames found in GIF");
        }
        return res;
    }

    public static BufferedImage stitchGif(AnimatedGif gif, int frameW, int frameH) throws IOException {
        int frameCount = gif.getFrameCount();
        // Use average delay (or first frame delay)
        int frameDelayMs = (int) gif.getDelay(0)
            .toMillis();
        if (frameDelayMs == 0) {
            frameDelayMs = 1000;
        }

        // Create empty image to hold stitched frames horizontally
        ImmutableImage spriteSheet = ImmutableImage.filled(frameW * frameCount, frameH, new Color(0, 0, 0, 0));

        // Place each resized frame into the stitched image
        for (int i = 0; i < frameCount; i++) {
            ImmutableImage frame = gif.getFrame(i)
                .scaleTo(frameW, frameH);
            spriteSheet = spriteSheet.overlay(frame, i * frameW, 0);
        }

        // Convert to BufferedImage
        return spriteSheet.awt();
    }

    /**
     * Loads a GIF from byte array, extracts and resizes frames,
     * stitches them into a single horizontal strip, and returns
     * a DynamicTexture along with animation info.
     */
    // TODO: maybe return null if something goes wrong
    public static GifAnimationData loadGifFromBytes(byte[] gifBytes, int frameW, int frameH) throws IOException {
        AnimatedGif gif = bytesToGif(gifBytes);
        int frameCount = gif.getFrameCount();
        // Use average delay (or first frame delay)
        int frameDelayMs = (int) gif.getDelay(0)
            .toMillis();
        if (frameDelayMs == 0) {
            frameDelayMs = 1000;
        }

        BufferedImage stitchedImage = stitchGif(gif, frameW, frameH);

        // Create Minecraft-compatible dynamic texture
        DynamicTexture tex = new DynamicTexture(stitchedImage);

        return new GifAnimationData(tex, frameW, frameH, frameCount, frameDelayMs);
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
        AnimatedGif gif = null;
        try {
            gif = bytesToGif(gifBytes);
        } catch (IOException e) {
            return null;
        }
        int frameCount = gif.getFrameCount();
        // Use average delay (or first frame delay)
        int frameDelayMs = (int) gif.getDelay(0)
            .toMillis();
        if (frameDelayMs == 0) {
            frameDelayMs = 1000;
        }

        BufferedImage stitchedImage = null;
        try {
            stitchedImage = stitchGif(gif, frameW, frameH);
        } catch (IOException e) {
            return null;
        }
        byte[] imgBytes = bufferedImageToByteArray(stitchedImage);
        if (imgBytes == null) {
            return null;
        }
        return new StitchedAnimationData(imgBytes, frameW, frameH, frameCount, frameDelayMs);
    }

    public static void validateStitchedData(StitchedAnimationData data) throws IOException {
        if (data.frameWidth != 32 || data.frameHeight != 32) {
            throw new IOException(
                "Invalid GIF dimensions: must be 32x32, got " + data.frameWidth + "x" + data.frameHeight);
        }
        if (data.frameCount <= 0 || data.frameCount > Config.maxGifFrameCount) {
            System.out.println("conf max: " + Config.maxGifFrameCount);
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
        if (imgLength <= 0 || imgLength > Config.gifSizeCap * 1024 * 1024) { // 2 MB cap
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

        // Write metadata
        dos.writeInt(data.frameWidth);
        dos.writeInt(data.frameHeight);
        dos.writeInt(data.frameCount);
        dos.writeInt(data.frameDelayMs);

        // Write image length + data
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
