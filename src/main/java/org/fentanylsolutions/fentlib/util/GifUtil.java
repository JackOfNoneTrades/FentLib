package org.fentanylsolutions.fentlib.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import net.minecraft.client.renderer.texture.DynamicTexture;

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

    public static AnimatedGif bytesToGif(byte[] gifBytes) throws IOException {
        return AnimatedGifReader.read(ImageSource.of(new ByteArrayInputStream(gifBytes)));
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
        if (frameCount == 0) {
            throw new IOException("No frames found in GIF");
        }

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
        BufferedImage stitchedImage = spriteSheet.awt();

        // Create Minecraft-compatible dynamic texture
        DynamicTexture tex = new DynamicTexture(stitchedImage);

        return new GifAnimationData(tex, frameW, frameH, frameCount, frameDelayMs);
    }
}
