package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

import org.apache.logging.log4j.Logger;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IAnimatedServerData;
import org.fentanylsolutions.fentlib.util.FileUtil;
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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import io.netty.buffer.ByteBuf;

public class FeatureAnimatedIcon {

    @Mixin(ServerData.class)
    public static class MixinServerData implements IAnimatedServerData {

        @Unique
        private boolean isAnimatedIcon = false;

        @Unique
        private GifUtil.StitchedAnimationData stitchedAnimationData;

        @Inject(method = "getNBTCompound", at = @At("RETURN"))
        private void saveAnimatedIcon(CallbackInfoReturnable<NBTTagCompound> cir) {
            FentLib.debug("MixinServerData:saveAnimatedIcon hook");
            FentLib.debug("animatedIcon: " + this.isAnimatedIcon);
            NBTTagCompound nbtCompound = cir.getReturnValue();
            nbtCompound.setBoolean("animatedIcon", this.isAnimatedIcon);

            if (this.isAnimatedIcon) {
                ServerData self = (ServerData) (Object) this;
                String blob = self.getBase64EncodedIconData();
                if (blob != null) {
                    FentLib.debug("getBase64EncodedIconData() size: " + blob.length());
                    // Hash the blob, save to file
                    String hash = FileUtil.hashStringBlob(blob);
                    saveIconToFile(hash, blob);
                    // Replace the big GIF b64 with a reference to the hash
                    nbtCompound.setString("animatedIconHash", "gif:" + hash);
                    FentLib.debug("Saved icon as " + hash);
                } else {
                    FentLib.debug("getBase64EncodedIconData() is null");
                }
                nbtCompound.removeTag("icon");
            }
        }

        @Unique
        private static void saveIconToFile(String hash, String blob) {
            try {
                File file = new File(FentLib.varInstanceClient.serverIconDir, hash);
                java.nio.file.Files.write(file.toPath(), blob.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                FentLib.debug("[saveIconToFile] Saved animated icon to: " + file.getAbsolutePath());
            } catch (Exception e) {
                FentLib.LOG.error("Failed to save animated icon", e);
            }
        }

        @Unique
        private static String loadIconFromFile(String hash) {
            try {
                File file = new File(FentLib.varInstanceClient.serverIconDir, hash);
                if (!file.exists()) {
                    return null;
                }
                return new String(
                    java.nio.file.Files.readAllBytes(file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception e) {
                FentLib.LOG.error("Failed to load animated icon", e);
                return null;
            }
        }

        @Inject(method = "getServerDataFromNBTCompound", at = @At("RETURN"))
        private static void loadAnimatedIcon(NBTTagCompound nbtCompound, CallbackInfoReturnable<ServerData> cir) {
            FentLib.debug("MixinServerData:loadAnimatedIcon hook");
            ServerData serverData = cir.getReturnValue();
            IAnimatedServerData animated = (IAnimatedServerData) serverData;

            // An exception escaping this hook makes vanilla discard the entire server list
            try {
                String animatedIconHash = nbtCompound.getString("animatedIconHash");
                if (animatedIconHash.startsWith("gif:") && animatedIconHash.length() > 4) {
                    FentLib.debug("Got animatedIconHash: " + animatedIconHash);
                    String hash = animatedIconHash.substring(4);
                    String blob = loadIconFromFile(hash);
                    if (blob != null) {
                        serverData.func_147407_a(blob); // Restore actual blob to memory
                        animated.setIsAnimatedIcon(true);
                        FentLib.debug("Loaded animated icon from file: " + hash);
                    } else {
                        // File missing, clear icon
                        serverData.func_147407_a(null);
                        animated.setIsAnimatedIcon(false);
                        FentLib.LOG.warn("Animated icon file missing: {}", hash);
                    }
                } else if (!animatedIconHash.isEmpty()) {
                    FentLib.LOG.warn("Ignoring malformed animatedIconHash: {}", animatedIconHash);
                } else {
                    FentLib.debug("animatedIconHash is null");
                }
                if (nbtCompound.hasKey("animatedIcon")) {
                    animated.setIsAnimatedIcon(nbtCompound.getBoolean("animatedIcon"));
                } else {
                    animated.setIsAnimatedIcon(false);
                }
            } catch (Exception e) {
                FentLib.LOG.error("Failed to restore animated icon, keeping server entry", e);
            }
        }

        @Override
        public boolean getIsAnimatedIcon() {
            return this.isAnimatedIcon;
        }

        @Override
        public void setIsAnimatedIcon(boolean val) {
            FentLib.debug("setIsAnimatedIcon(" + val + ")");
            this.isAnimatedIcon = val;
        }

        @Override
        public GifUtil.StitchedAnimationData getStitchedAnimationData() {
            return this.stitchedAnimationData;
        }

        @Override
        public void setStitchedAnimationData(GifUtil.StitchedAnimationData data) {
            FentLib.debug("setStitchedAnimationData called");
            this.stitchedAnimationData = data;
        }
    }

    @Mixin(targets = "net.minecraft.client.network.OldServerPinger$1")
    public static class MixinOldServerPinger {

        @Shadow(aliases = "val$p_147224_1_", remap = false)
        @Final
        ServerData val$server;

        @Redirect(
            method = "handleServerInfo",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"))
        private boolean interceptStartsWith(String iconData, String prefix) {
            FentLib.debug("[mixin] interceptStartsWith");
            FentLib.debug("val$server: " + val$server);
            FentLib.debug(val$server.serverIP);
            if (iconData.startsWith("data:image/stitched;base64,")) {
                handleGifIcon(iconData, val$server);
                return false;
            }
            boolean isPng = iconData.startsWith(prefix);
            if (isPng) {
                FentLib.debug("Received static png data.");
            }
            ((IAnimatedServerData) val$server).setIsAnimatedIcon(false);
            ((IAnimatedServerData) val$server).setStitchedAnimationData(null);
            return isPng;
        }

        private static void handleGifIcon(String gifData, ServerData serverData) {
            String base64Data = gifData.substring("data:image/stitched;base64,".length());

            FentLib.debug(
                "Detected Stitched server icon: " + base64Data.substring(0, Math.min(50, base64Data.length())) + "...");
            serverData.func_147407_a(base64Data);
            ((IAnimatedServerData) serverData).setIsAnimatedIcon(true);
        }

        @Inject(method = "handleServerInfo", at = @At("HEAD"))
        private void onHandleServerInfo(S00PacketServerInfo packetIn, CallbackInfo ci) {
            FentLib.debug("[Mixin] handleServerInfo was called: " + packetIn);
        }

        @Redirect(
            method = "handleServerInfo",
            at = @At(
                value = "INVOKE",
                target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;)V",
                ordinal = 0,
                remap = false))
        private void redirectInvalidIconLog(Logger logger, String message, @Local String s) {
            if (s != null && s.startsWith("data:image/stitched;base64,")) {
                FentLib.debug("Bypassing unknown format error for stitched image");
                return;
            }
            logger.error(message);
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
            FentLib.varInstanceServer.loadFavicons();
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
            // Need to null those fields, otherwise the first reload from a gif to a static image will produce a
            // corrupted blend of the two.
            field_148305_h = null;
            // field_148306_i = null;
            String base64 = field_148301_e.getBase64EncodedIconData();
            if (base64 == null || !((IAnimatedServerData) field_148301_e).getIsAnimatedIcon()) {
                return;
            }

            FentLib.debug("Detected animated gif icon, drawing it instead");
            ci.cancel();

            Minecraft mc = Minecraft.getMinecraft();
            FentLib.varInstanceClient.gifloaderPool.submit(() -> {
                try {
                    long t0 = System.nanoTime();
                    byte[] decoded = Base64.getDecoder()
                        .decode(base64);
                    long t1 = System.nanoTime();
                    GifUtil.StitchedAnimationData stitchedData = GifUtil.deserializeStitchedData(decoded);
                    GifUtil.validateStitchedData(stitchedData);
                    long t2 = System.nanoTime();
                    BufferedImage stitchedImage = GifUtil.byteArrayToBufferedImage(stitchedData.stichedData);
                    if (stitchedImage == null) {
                        FentLib.LOG.error("Failed to reconstruct stitched image");
                        return;
                    }
                    long t3 = System.nanoTime();
                    FentLib.debug("Base64 decode: " + (t1 - t0) / 1_000_000.0 + " ms");
                    FentLib.debug("Deserialize:   " + (t2 - t1) / 1_000_000.0 + " ms");
                    FentLib.debug("Image decode:  " + (t3 - t2) / 1_000_000.0 + " ms");

                    mc.func_152344_a(() -> {
                        try {
                            long t4 = System.nanoTime();
                            field_148305_h = new DynamicTexture(stitchedImage);
                            field_148306_i = field_148300_d.getTextureManager()
                                .getDynamicTextureLocation("server_icon_" + field_148301_e.serverIP, field_148305_h);
                            ((IAnimatedServerData) field_148301_e).setStitchedAnimationData(stitchedData);
                            long t5 = System.nanoTime();
                            FentLib.debug("GL Upload:     " + (t5 - t4) / 1_000_000.0 + " ms");
                        } catch (Exception glEx) {
                            FentLib.LOG.error("Failed to upload dynamic texture", glEx);
                            field_148301_e.func_147407_a(null);
                        }
                    });

                } catch (Exception e) {
                    FentLib.LOG.error("Failed to decode or validate stitched animation", e);
                    mc.func_152344_a(() -> field_148301_e.func_147407_a(null));
                }
            });
        }

        @WrapOperation(
            method = "drawEntry",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;func_146110_a(IIFFIIFF)V", ordinal = 1))
        private void redirectServerIconDraw(int x, int y, float u, float v, int width, int height, float texWidth,
            float texHeight, Operation original) {
            if (!((IAnimatedServerData) field_148301_e).getIsAnimatedIcon()) {
                original.call(x, y, u, v, width, height, texWidth, texHeight);
                return;
            }
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
            } else {
                original.call(x, y, u, v, width, height, texWidth, texHeight);
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
            int length = ((PacketBuffer) (Object) this).readVarIntFromBuffer();

            // TODO: find some limit?
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
