package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IAnimatedServerData;
import org.fentanylsolutions.fentlib.util.GifUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public class FeatureAnimatedIcon {

    @Mixin(ServerData.class)
    public static class MixinServerData implements IAnimatedServerData {

        @Unique
        private boolean isAnimatedIcon = false;

        @Unique
        private GifUtil.StitchedAnimationData stitchedAnimationData;

        @Inject(method = "getNBTCompound", at = @At("RETURN"))
        private void saveAnimatedIcon(CallbackInfoReturnable<NBTTagCompound> cir) {
            NBTTagCompound nbtCompound = cir.getReturnValue();
            nbtCompound.setBoolean("animatedIcon", this.isAnimatedIcon);
        }

        @Inject(method = "getServerDataFromNBTCompound", at = @At("RETURN"))
        private static void loadAnimatedIcon(NBTTagCompound nbtCompound, CallbackInfoReturnable<ServerData> cir) {
            ServerData serverData = cir.getReturnValue();
            MixinServerData mixinServerData = (MixinServerData) (Object) serverData;
            if (nbtCompound.hasKey("animatedIcon")) {
                mixinServerData.isAnimatedIcon = nbtCompound.getBoolean("animatedIcon");
            } else {
                mixinServerData.isAnimatedIcon = false;
            }
        }

        @Override
        public boolean getIsAnimatedIcon() {
            return this.isAnimatedIcon;
        }

        @Override
        public void setIsAnimatedIcon(boolean val) {
            this.isAnimatedIcon = val;
        }

        @Override
        public GifUtil.StitchedAnimationData getStitchedAnimationData() {
            return this.stitchedAnimationData;
        }

        @Override
        public void setStitchedAnimationData(GifUtil.StitchedAnimationData data) {
            this.stitchedAnimationData = data;
        }
    }

    @Mixin(targets = "net.minecraft.client.network.OldServerPinger$1") // This is the anonymous class
    public static class MixinOldServerPinger {

        @Shadow(aliases = "val$p_147224_1_", remap = false)
        @Final
        ServerData val$server;

        @Redirect(
            method = "handleServerInfo",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"))
        private boolean interceptStartsWith(String iconData, String prefix) {
            FentLib.LOG.info("[mixin] interceptStartsWith");
            System.out.println("val$server: " + val$server);
            System.out.println(val$server.serverIP);
            if (iconData.startsWith("data:image/stitched;base64,")) {
                handleGifIcon(iconData, val$server);
                return false;
            }
            return iconData.startsWith(prefix);
        }

        private static void handleGifIcon(String gifData, ServerData serverData) {
            String base64Data = gifData.substring("data:image/stitched;base64,".length());

            System.out.println(
                "Detected Stitched server icon: " + base64Data.substring(0, Math.min(50, base64Data.length())) + "...");
            serverData.func_147407_a(base64Data);
            ((IAnimatedServerData) serverData).setIsAnimatedIcon(true);
        }

        @Inject(method = "handleServerInfo", at = @At("HEAD"))
        private void onHandleServerInfo(S00PacketServerInfo packetIn, CallbackInfo ci) {
            System.out.println("[Mixin] handleServerInfo was called: " + packetIn);
        }
    }

    @Mixin(MinecraftServer.class)
    public static abstract class MixinMinecraftServer {

        @Shadow
        abstract File getFile(String fileName);

        @Shadow
        @Final
        static Logger logger;

        /**
         * @author jack
         * @reason Wholesale replacing the server icon loading
         */
        @Overwrite
        private void func_147138_a(ServerStatusResponse response) {
            if (false) {
                File file1 = this.getFile("server-icon.png");
                if (file1.isFile()) {
                    ByteBuf bytebuf = Unpooled.buffer();
                    try {
                        BufferedImage bufferedimage = ImageIO.read(file1);
                        Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                        Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                        ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
                        ByteBuf bytebuf1 = io.netty.handler.codec.base64.Base64.encode(bytebuf);
                        response.func_151320_a("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
                    } catch (Exception exception) {
                        logger.error("Couldn\'t load server icon", exception);
                    } finally {
                        bytebuf.release();
                    }
                } else {
                    FentLib.LOG.info("Server icon not found");
                }
            } else {

                File file = this.getFile("server-icon.gif");

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
                        response.func_151320_a("data:image/stitched;base64," + base64);
                    } catch (Exception e) {
                        logger.error("Couldn't load animated server icon GIF", e);
                    }
                }
            }
        }
    }

    @Mixin(ServerListEntryNormal.class)
    public static class MixinServerListEntryNormal {

        @Shadow
        @Final
        GuiMultiplayer field_148303_c;

        @Shadow
        @Final
        ServerData field_148301_e;

        @Shadow
        @Final
        Minecraft field_148300_d;

        @Shadow
        DynamicTexture field_148305_h;

        @Shadow
        ResourceLocation field_148306_i;

        @Inject(method = "func_148297_b", at = @At("HEAD"), cancellable = true)
        private void onFunc148297b(CallbackInfo ci) {
            String base64 = field_148301_e.getBase64EncodedIconData();
            if (base64 == null || !((IAnimatedServerData) field_148301_e).getIsAnimatedIcon()) {
                return;
            }
            System.out.println("Detected animated gif icon, drawing it instead");
            ci.cancel();

            /*
             * FentLib.varInstanceClient.gifloaderPool.submit(() -> {
             * try {
             * // Step 1: Decode Base64
             * byte[] decoded = Base64.getDecoder()
             * .decode(field_148301_e.getBase64EncodedIconData());
             * // Step 2: Load gif data (should return your GifAnimationData)
             * GifUtil.StitchedAnimationData stitchedData = GifUtil.deserializeStitchedData(decoded);
             * // Step 3: Store it somewhere (can cache on serverData or static map if needed)
             * ((IAnimatedServerData) this.field_148301_e).setStitchedAnimationData(stitchedData);
             * // Step 4: Set the dynamic texture
             * BufferedImage stitchedImage = GifUtil.byteArrayToBufferedImage(stitchedData.stichedData);
             * if (stitchedImage == null) {
             * FentLib.LOG.error("Failed to reconstruct image from stitched data");
             * return;
             * }
             * this.field_148305_h = new DynamicTexture(stitchedImage);
             * // Step 5: Set the texture location
             * this.field_148306_i = field_148300_d.getTextureManager()
             * .getDynamicTextureLocation("server_icon_" + field_148301_e.serverIP, this.field_148305_h);
             * } catch (Exception e) {
             * FentLib.LOG.error("Failed to decode animated gif icon", e);
             * field_148301_e.func_147407_a(null);
             * }
             * });
             */

            FentLib.varInstanceClient.gifloaderPool.submit(() -> {
                try {
                    byte[] decoded = Base64.getDecoder()
                        .decode(base64);
                    GifUtil.StitchedAnimationData stitchedData = GifUtil.deserializeStitchedData(decoded);
                    GifUtil.validateStitchedData(stitchedData);

                    BufferedImage stitchedImage = GifUtil.byteArrayToBufferedImage(stitchedData.stichedData);
                    if (stitchedImage == null) {
                        FentLib.LOG.error("Failed to reconstruct stitched image");
                        return;
                    }

                    Minecraft mc = Minecraft.getMinecraft();
                    synchronized (mc) {
                        mc.func_152344_a(() -> {
                            try {
                                field_148305_h = new DynamicTexture(stitchedImage);
                                field_148306_i = field_148300_d.getTextureManager()
                                    .getDynamicTextureLocation(
                                        "server_icon_" + field_148301_e.serverIP,
                                        field_148305_h);
                                ((IAnimatedServerData) field_148301_e).setStitchedAnimationData(stitchedData);
                            } catch (Exception e) {
                                FentLib.LOG.error("Failed to create DynamicTexture on main thread", e);
                            }
                        });
                    }

                } catch (Exception e) {
                    FentLib.LOG.error("Failed to decode or validate stitched animation", e);
                    field_148301_e.func_147407_a(null);
                }
            });
        }

        @Redirect(
            method = "drawEntry",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;func_146110_a(IIFFIIFF)V", ordinal = 1))
        private void redirectServerIconDraw(int x, int y, float u, float v, int width, int height, float texWidth,
            float texHeight) {
            /*
             * GifUtil.GifAnimationData gif = ((IMixinServerData)field_148301_e).getGifAnimationData();
             * if (gif != null) {
             * // Animated icon: calculate frame UVs
             * long time = Minecraft.getSystemTime();
             * int frameIndex = (int) ((time / gif.frameDelayMs) % gif.frameCount);
             * int uPixels = frameIndex * gif.frameWidth;
             * Minecraft.getMinecraft().getTextureManager().bindTexture(this.field_148306_i);
             * Gui.func_146110_a(
             * x, y,
             * (float) uPixels, 0.0f,
             * gif.frameWidth, gif.frameHeight,
             * gif.frameWidth * gif.frameCount, gif.frameHeight
             * );
             * } else {
             * // Default (static) icon path
             * Gui.func_146110_a(x, y, u, v, width, height, texWidth, texHeight);
             * }
             */
            redirectServerIconDraw(x, y, u, v, width, height, texWidth, texHeight, field_148301_e, field_148306_i);
        }

        private static void redirectServerIconDraw(int x, int y, float u, float v, int width, int height,
            float texWidth, float texHeight, ServerData field_148301_e, ResourceLocation field_148306_i) {
            GifUtil.StitchedAnimationData stitchedData = ((IAnimatedServerData) field_148301_e)
                .getStitchedAnimationData();

            if (stitchedData != null) {
                // Animated icon: calculate frame UVs
                long time = Minecraft.getSystemTime();
                int frameIndex = (int) ((time / stitchedData.frameDelayMs) % stitchedData.frameCount);
                int uPixels = frameIndex * stitchedData.frameWidth;

                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(field_148306_i);

                Gui.func_146110_a(
                    x,
                    y,
                    (float) uPixels,
                    0.0f,
                    stitchedData.frameWidth,
                    stitchedData.frameHeight,
                    stitchedData.frameWidth * stitchedData.frameCount,
                    stitchedData.frameHeight);
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(Gui.icons);
            }
        }
    }

    @Mixin(PacketBuffer.class)
    public static class MixinPacketBuffer {

        @Shadow
        @Final
        private ByteBuf field_150794_a;

        /**
         * @author jack
         * @reason Yeeting the limit here
         */
        @Overwrite
        public String readStringFromBuffer(int maxLength) {
            System.out.println("Custom readStringFromBuffer is active");
            int length = ((PacketBuffer) (Object) this).readVarIntFromBuffer();

            if (length > 9_600_000) {
                // throw new DecoderException("The received string length is longer than maximum allowed (" + length + "
                // >
                // 2,600,000)");
            }

            byte[] bytes = new byte[length * 4]; // UTF-8 max
            this.field_150794_a.readBytes(bytes, 0, length);
            return new String(bytes, 0, length, StandardCharsets.UTF_8);
        }

        /**
         * @author jack
         * @reason Yeeting the limit here
         */
        @Overwrite
        public void writeStringToBuffer(String str) {
            System.out.println("Custom writeStringFromBuffer is active");
            byte[] abyte = str.getBytes(Charsets.UTF_8);

            if (abyte.length > 9_600_000) {
                // throw new DecoderException("The received string length is longer than maximum allowed (" + length + "
                // >
                // 2,600,000)");
            }

            ((PacketBuffer) (Object) this).writeVarIntToBuffer(abyte.length);
            ((PacketBuffer) (Object) this).writeBytes(abyte);
        }
    }
}
