package org.fentanylsolutions.fentlib.util;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public final class ImageUtil {

    private ImageUtil() {}

    public static BufferedImage copyToArgb(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }

        return copyToNewArgb(image);
    }

    public static BufferedImage copyToNewArgb(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }

        BufferedImage argb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copyIntoArgb(image, argb);
        return argb;
    }

    private static void copyIntoArgb(BufferedImage source, BufferedImage target) {
        if (source.getColorModel()
            .getColorSpace()
            .getType() == ColorSpace.TYPE_GRAY) {
            copyGrayToArgb(source, target);
            return;
        }

        int width = source.getWidth();
        int[] row = new int[width];
        for (int y = 0; y < source.getHeight(); y++) {
            source.getRGB(0, y, width, 1, row, 0, width);
            target.setRGB(0, y, width, 1, row, 0, width);
        }
    }

    private static void copyGrayToArgb(BufferedImage source, BufferedImage target) {
        Raster raster = source.getRaster();
        boolean hasAlpha = source.getColorModel()
            .hasAlpha();
        int alphaBand = raster.getNumBands() - 1;
        int width = source.getWidth();
        int[] row = new int[width];

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                int gray = raster.getSample(x, y, 0);
                int alpha = hasAlpha ? raster.getSample(x, y, alphaBand) : 255;
                row[x] = alpha << 24 | gray << 16 | gray << 8 | gray;
            }
            target.setRGB(0, y, width, 1, row, 0, width);
        }
    }
}
