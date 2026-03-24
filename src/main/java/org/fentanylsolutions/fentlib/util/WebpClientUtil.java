package org.fentanylsolutions.fentlib.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-only WebP helpers for Minecraft textures and resource loading.
 */
@SideOnly(Side.CLIENT)
public final class WebpClientUtil {

    private WebpClientUtil() {}

    public static BufferedImage readImage(ResourceLocation location) throws IOException {
        try (InputStream in = Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(location)
            .getInputStream()) {
            return WebpUtil.readImage(in);
        }
    }

    public static DynamicTexture readDynamicTexture(byte[] webpBytes) throws IOException {
        return new DynamicTexture(WebpUtil.readImage(webpBytes));
    }

    public static DynamicTexture readDynamicTexture(InputStream in) throws IOException {
        return new DynamicTexture(WebpUtil.readImage(in));
    }

    public static DynamicTexture readDynamicTexture(ResourceLocation location) throws IOException {
        return new DynamicTexture(readImage(location));
    }
}
