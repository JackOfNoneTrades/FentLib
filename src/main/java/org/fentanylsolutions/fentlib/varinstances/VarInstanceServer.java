package org.fentanylsolutions.fentlib.varinstances;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import net.minecraft.server.MinecraftServer;

import org.apache.commons.lang3.Validate;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.GifUtil;
import org.fentanylsolutions.fentlib.util.ImageUtil;
import org.fentanylsolutions.fentlib.util.QoiUtil;
import org.fentanylsolutions.fentlib.util.WebpUtil;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public class VarInstanceServer {

    public String staticFaviconBlob = null;
    public String animatedFaviconBlob = null;

    public void loadFavicons() {
        // --- Static icon (png > qoi > webp, skip animated webp) ---
        File pngFile = MinecraftServer.getServer()
            .getFile("server-icon.png");
        File qoiFile = MinecraftServer.getServer()
            .getFile("server-icon.qoi");
        File webpFile = MinecraftServer.getServer()
            .getFile("server-icon.webp");
        boolean webpIsAnimated = false;
        if (webpFile.isFile()) {
            try {
                byte[] webpBytes = Files.readAllBytes(webpFile.toPath());
                webpIsAnimated = WebpUtil.isAnimated(webpBytes);
            } catch (Exception e) {
                FentLib.LOG.error("Couldn't probe server-icon.webp", e);
            }
        }

        if (pngFile.isFile()) {
            loadStaticFavicon(pngFile);
        } else if (qoiFile.isFile()) {
            loadStaticFavicon(qoiFile);
        } else if (webpFile.isFile() && !webpIsAnimated) {
            loadStaticFavicon(webpFile);
        } else if (!webpIsAnimated) {
            FentLib.LOG.info("Static server icon not found");
            staticFaviconBlob = null;
        }

        // --- Animated icon (gif > animated webp) ---
        File gifFile = MinecraftServer.getServer()
            .getFile("server-icon.gif");
        if (gifFile.isFile()) {
            loadAnimatedFavicon(gifFile, false);
        } else if (webpIsAnimated) {
            loadAnimatedFavicon(webpFile, true);
        } else {
            FentLib.LOG.info("Animated server icon not found");
            animatedFaviconBlob = null;
        }
    }

    private void loadAnimatedFavicon(File file, boolean isWebp) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            GifUtil.GifData gifData;
            if (isWebp) {
                gifData = WebpUtil.readAnimated(fileBytes);
            } else {
                gifData = GifUtil.readGif(fileBytes);
            }

            // Build stitched animation data for fentlib clients
            int frameDelay = GifUtil.getFrameDelay(gifData);
            BufferedImage stitched = GifUtil.stitchGif(gifData, 32, 32);
            byte[] imgBytes = GifUtil.bufferedImageToByteArray(stitched);
            if (imgBytes == null) {
                FentLib.LOG.error("Couldn't encode stitched animated icon from {}", file.getName());
                return;
            }
            GifUtil.StitchedAnimationData stitchedData = new GifUtil.StitchedAnimationData(
                imgBytes,
                32,
                32,
                gifData.getFrameCount(),
                frameDelay);
            byte[] serializedData = GifUtil.serializeStitchedData(stitchedData);
            String base64 = Base64.getEncoder()
                .encodeToString(serializedData);
            animatedFaviconBlob = "data:image/stitched;base64," + base64;
            FentLib.LOG.info("Successfully loaded animated server icon from {}", file.getName());

            // Set first frame as static fallback for non-fentlib clients
            if (staticFaviconBlob == null) {
                BufferedImage firstFrame = GifUtil.scaleFrame(gifData.getFrame(0), 64, 64);
                setStaticFaviconFromImage(firstFrame, file.getName());
            }
        } catch (Exception e) {
            FentLib.LOG.error("Couldn't load animated favicon from " + file.getName(), e);
        }
    }

    private void loadStaticFavicon(File file) {
        ByteBuf bytebuf = Unpooled.buffer();
        try {
            BufferedImage bufferedimage = readStaticIcon(file);
            Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
            Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
            ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
            ByteBuf bytebuf1 = io.netty.handler.codec.base64.Base64.encode(bytebuf);
            staticFaviconBlob = "data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8);
            FentLib.LOG.info("Successfully loaded static server icon from {}", file.getName());
        } catch (Exception exception) {
            FentLib.LOG.error("Couldn't load static server favicon from " + file.getName(), exception);
        } finally {
            bytebuf.release();
        }
    }

    private void setStaticFaviconFromImage(BufferedImage image, String sourceName) {
        ByteBuf bytebuf = Unpooled.buffer();
        try {
            Validate.validState(image.getWidth() == 64, "Must be 64 pixels wide");
            Validate.validState(image.getHeight() == 64, "Must be 64 pixels high");
            ImageIO.write(image, "PNG", new ByteBufOutputStream(bytebuf));
            ByteBuf bytebuf1 = io.netty.handler.codec.base64.Base64.encode(bytebuf);
            staticFaviconBlob = "data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8);
            FentLib.LOG.info("Set static fallback icon from {}", sourceName);
        } catch (Exception e) {
            FentLib.LOG.error("Couldn't set static fallback icon from " + sourceName, e);
        } finally {
            bytebuf.release();
        }
    }

    private static BufferedImage readStaticIcon(File file) throws IOException {
        String name = file.getName()
            .toLowerCase();
        if (name.endsWith(".qoi")) {
            return QoiUtil.readImage(file);
        }
        if (name.endsWith(".webp")) {
            return WebpUtil.readImage(file);
        }
        BufferedImage image = ImageIO.read(file);
        return image == null ? null : ImageUtil.copyToArgb(image);
    }
}
