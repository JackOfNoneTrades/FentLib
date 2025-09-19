package org.fentanylsolutions.fentlib.varinstances;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import net.minecraft.server.MinecraftServer;

import org.apache.commons.lang3.Validate;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.GifUtil;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public class VarInstanceServer {

    public String staticFaviconBlob = null;
    public String animatedFaviconBlob = null;

    public void loadFavicons() {
        File file1 = MinecraftServer.getServer()
            .getFile("server-icon.png");
        if (file1.isFile()) {
            ByteBuf bytebuf = Unpooled.buffer();
            try {
                BufferedImage bufferedimage = ImageIO.read(file1);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                ByteBuf bytebuf1 = io.netty.handler.codec.base64.Base64.encode(bytebuf);
                staticFaviconBlob = "data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8);
            } catch (Exception exception) {
                FentLib.LOG.error("Couldn\'t load static server favicon", exception);
            } finally {
                bytebuf.release();
                FentLib.LOG.info("Successfully loaded static server icon");
            }
        } else {
            FentLib.LOG.info("Static server icon not found");
            staticFaviconBlob = null;
        }

        File file = MinecraftServer.getServer()
            .getFile("server-icon.gif");
        if (file.isFile()) {
            byte[] gifBytes;

            try {
                gifBytes = Files.readAllBytes(file.toPath());

                GifUtil.StitchedAnimationData stichedData = GifUtil.stitchedFromBytes(gifBytes, 32, 32);
                if (stichedData == null) {
                    FentLib.LOG.error("Couldn't load animated server icon GIF into stitched data");
                    return;
                }

                byte[] serializedData = GifUtil.serializeStitchedData(stichedData);
                String base64 = Base64.getEncoder()
                    .encodeToString(serializedData);
                animatedFaviconBlob = "data:image/stitched;base64," + base64;
                FentLib.LOG.info("Successfully loaded animated server icon");
            } catch (Exception e) {
                FentLib.LOG.error("Couldn't load animated GIF favicon", e);
            }
        } else {
            FentLib.LOG.info("Animated server icon not found");
            animatedFaviconBlob = null;
        }
    }
}
