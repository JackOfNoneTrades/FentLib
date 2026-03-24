package org.fentanylsolutions.fentlib.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NodeList;

/**
 * Pure Java animated GIF reader using javax.imageio.
 * Handles GIF disposal methods, frame offsets, and transparency correctly.
 */
public final class NativeGifReader {

    private NativeGifReader() {}

    public static GifUtil.GifData read(byte[] gifBytes) throws IOException {
        ImageReader reader = null;
        ImageInputStream stream = null;
        try {
            stream = ImageIO.createImageInputStream(new ByteArrayInputStream(gifBytes));
            reader = ImageIO.getImageReadersByFormatName("gif")
                .next();
            reader.setInput(stream);

            int frameCount = reader.getNumImages(true);
            if (frameCount == 0) {
                throw new IOException("No frames found in GIF");
            }

            int canvasWidth = reader.getWidth(0);
            int canvasHeight = reader.getHeight(0);

            BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D canvasG = canvas.createGraphics();
            BufferedImage previousCanvas = null;

            List<BufferedImage> frames = new ArrayList<>();
            List<Integer> delays = new ArrayList<>();

            for (int i = 0; i < frameCount; i++) {
                BufferedImage rawFrame = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                FrameMeta meta = parseFrameMeta(metadata);

                // Save state before drawing if we might need to restore
                if ("restoreToPrevious".equals(meta.disposalMethod)) {
                    previousCanvas = copyImage(canvas);
                }

                // Draw frame onto canvas at its offset
                canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                canvasG.drawImage(rawFrame, meta.left, meta.top, null);

                // Snapshot the composited canvas as this frame
                frames.add(copyImage(canvas));
                delays.add(meta.delayMs);

                // Apply disposal
                switch (meta.disposalMethod) {
                    case "restoreToBackgroundColor":
                        canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                        canvasG.fillRect(meta.left, meta.top, rawFrame.getWidth(), rawFrame.getHeight());
                        canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                        break;
                    case "restoreToPrevious":
                        if (previousCanvas != null) {
                            canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
                            canvasG.drawImage(previousCanvas, 0, 0, null);
                            canvasG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                        }
                        break;
                    // "none" and "doNotDispose" — leave canvas as-is
                }
            }

            canvasG.dispose();

            BufferedImage[] frameArray = frames.toArray(new BufferedImage[0]);
            int[] delayArray = new int[delays.size()];
            for (int i = 0; i < delays.size(); i++) {
                delayArray[i] = delays.get(i);
            }

            return new GifUtil.GifData(frameArray, delayArray);
        } finally {
            if (reader != null) reader.dispose();
            if (stream != null) stream.close();
        }
    }

    private static BufferedImage copyImage(BufferedImage src) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static FrameMeta parseFrameMeta(IIOMetadata metadata) {
        int left = 0;
        int top = 0;
        int delayMs = 100; // default 100ms
        String disposalMethod = "none";

        if (metadata == null) {
            return new FrameMeta(left, top, delayMs, disposalMethod);
        }

        IIOMetadataNode root;
        try {
            root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_image_1.0");
        } catch (Exception e) {
            return new FrameMeta(left, top, delayMs, disposalMethod);
        }

        // ImageDescriptor: imageLeftPosition, imageTopPosition
        NodeList descriptors = root.getElementsByTagName("ImageDescriptor");
        if (descriptors.getLength() > 0) {
            IIOMetadataNode desc = (IIOMetadataNode) descriptors.item(0);
            left = parseIntAttr(desc, "imageLeftPosition", 0);
            top = parseIntAttr(desc, "imageTopPosition", 0);
        }

        // GraphicControlExtension: delayTime (centiseconds), disposalMethod
        NodeList gcExtensions = root.getElementsByTagName("GraphicControlExtension");
        if (gcExtensions.getLength() > 0) {
            IIOMetadataNode gce = (IIOMetadataNode) gcExtensions.item(0);
            int centiseconds = parseIntAttr(gce, "delayTime", 10);
            delayMs = centiseconds * 10;
            String disposal = gce.getAttribute("disposalMethod");
            if (disposal != null && !disposal.isEmpty()) {
                disposalMethod = disposal;
            }
        }

        return new FrameMeta(left, top, delayMs, disposalMethod);
    }

    private static int parseIntAttr(IIOMetadataNode node, String attr, int defaultValue) {
        String value = node.getAttribute(attr);
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static class FrameMeta {

        final int left;
        final int top;
        final int delayMs;
        final String disposalMethod;

        FrameMeta(int left, int top, int delayMs, String disposalMethod) {
            this.left = left;
            this.top = top;
            this.delayMs = delayMs;
            this.disposalMethod = disposalMethod;
        }
    }
}
