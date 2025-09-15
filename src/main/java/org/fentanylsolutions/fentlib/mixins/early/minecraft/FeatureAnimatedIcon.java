package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ServerListEntryNormal;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IAnimatedServerData;
import org.fentanylsolutions.fentlib.mixins.DummyTarget;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sksamuel.scrimage.nio.AnimatedGif;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

@Mixin(DummyTarget.class)
public class FeatureAnimatedIcon {

    @Mixin(ServerData.class)
    public static class MixinServerData implements IAnimatedServerData {

        @Unique
        private boolean isAnimatedIcon = false;

        @Unique
        private GifUtil.GifAnimationData gifAnimationData;

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
        public GifUtil.GifAnimationData getGifAnimationData() {
            return this.gifAnimationData;
        }

        @Override
        public void setGifAnimationData(GifUtil.GifAnimationData data) {
            this.gifAnimationData = data;
        }
    }

    @Mixin(targets = "net.minecraft.client.network.OldServerPinger$1") // This is the anonymous class
    public static class MixinOldServerPinger {

        @Shadow(aliases = "val$p_147224_1_")
        @Final
        ServerData val$server;

        @Redirect(
            method = "handleServerInfo",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"))
        private boolean interceptStartsWith(String iconData, String prefix) {
            FentLib.LOG.info("[mixin] interceptStartsWith");
            System.out.println("val$server: " + val$server);
            System.out.println(val$server.serverIP);
            if (iconData.startsWith("data:image/gif;base64,")) {
                handleGifIcon(iconData, val$server);
                return false;
            }
            return iconData.startsWith(prefix);
        }

        private static void handleGifIcon(String gifData, ServerData serverData) {
            String base64Data = gifData.substring("data:image/gif;base64,".length());

            System.out.println(
                "Detected GIF server icon: " + base64Data.substring(0, Math.min(50, base64Data.length())) + "...");
            serverData.func_147407_a(base64Data);
            ((IAnimatedServerData) serverData).setIsAnimatedIcon(true);
        }
    }

    @Mixin(MinecraftServer.class)
    public static abstract class MixinMinecraftServer {

        @Shadow
        abstract File getFile(String fileName);

        @Shadow
        @Final
        static Logger logger;

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

                        // ✅ Validate with your custom GifUtil loader
                        AnimatedGif gifData = GifUtil.bytesToGif(gifBytes);
                        if (gifData.getFrameCount() == 0) {
                            logger.error("Couldn't load animated server icon GIF, no frames found");
                            return;
                        }

                        // If validation passes, encode the original bytes
                        String base64 = Base64.getEncoder()
                            .encodeToString(gifBytes);
                        response.func_151320_a("data:image/gif;base64," + base64);
                    } catch (Exception e) {
                        logger.error("Couldn't load animated server icon GIF", e);
                    }
                }
            }
        }
    }

    @Mixin(ServerList.class)
    public static class MixinServerList {

        @Shadow
        private List<ServerData> servers;

        @Shadow
        private net.minecraft.client.Minecraft mc;

        @Overwrite
        public void saveServerList() {
            try {
                File file = new File(this.mc.mcDataDir, "servers.json");

                Gson gson = new GsonBuilder().setPrettyPrinting()
                    .create();
                FileWriter writer = new FileWriter(file);

                gson.toJson(this.servers, writer);
                writer.close();
            } catch (Exception e) {
                FentLib.LOG.error("Couldn't save server list (JSON)", e);
            }
        }

        @Overwrite
        public void loadServerList() {
            try {
                this.servers.clear();
                File file = new File(this.mc.mcDataDir, "servers.json");

                if (!file.exists()) return;

                Gson gson = new Gson();
                FileReader reader = new FileReader(file);

                Type listType = new TypeToken<List<ServerData>>() {}.getType();
                List<ServerData> loaded = gson.fromJson(reader, listType);
                reader.close();

                if (loaded != null) {
                    this.servers.addAll(loaded);
                }
                for (ServerData serverData : this.servers) {
                    serverData.pingToServer = -1L;
                    serverData.serverMOTD = "";
                    serverData.populationInfo = "";
                    serverData.gameVersion = "";
                    // serverData. = "";
                    serverData.field_78841_f = false;
                    // serverData.field_78843_h = false;
                }
            } catch (Exception e) {
                FentLib.LOG.error("Couldn't load server list (JSON)", e);
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
            try {
                // Step 1: Decode Base64
                byte[] decoded = Base64.getDecoder()
                    .decode(field_148301_e.getBase64EncodedIconData());

                // Step 2: Load gif data (should return your GifAnimationData)
                GifUtil.GifAnimationData gif = GifUtil.loadGifFromBytes(decoded, 32, 32);

                // Step 3: Store it somewhere (can cache on serverData or static map if needed)
                ((IAnimatedServerData) this.field_148301_e).setGifAnimationData(gif); // Use a Map if you support
                                                                                      // multiple
                // icons

                // Step 4: Set the dynamic texture
                this.field_148305_h = gif.dynamicTexture;

                // Step 5: Set the texture location
                this.field_148306_i = field_148300_d.getTextureManager()
                    .getDynamicTextureLocation("server_icon_" + field_148301_e.serverIP, gif.dynamicTexture);

            } catch (Exception e) {
                FentLib.LOG.error("Failed to decode animated gif icon", e);
                field_148301_e.func_147407_a(null);
            }
        }

        /*
         * @Inject(method = "drawEntry", at = @At("HEAD"))
         * private void onDrawEntry(int p_148279_1_, int p_148279_2_, int p_148279_3_, int p_148279_4_, int p_148279_5_,
         * Tessellator p_148279_6_, int p_148279_7_, int p_148279_8_, boolean p_148279_9_, CallbackInfo ci) {
         * // TODO: this won't work, we need to patch elsewhere
         * }
         */

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
            GifUtil.GifAnimationData gif = ((IAnimatedServerData) field_148301_e).getGifAnimationData();

            if (gif != null) {
                // Animated icon: calculate frame UVs
                long time = Minecraft.getSystemTime();
                int frameIndex = (int) ((time / gif.frameDelayMs) % gif.frameCount);
                int uPixels = frameIndex * gif.frameWidth;

                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(field_148306_i);

                Gui.func_146110_a(
                    x,
                    y,
                    (float) uPixels,
                    0.0f,
                    gif.frameWidth,
                    gif.frameHeight,
                    gif.frameWidth * gif.frameCount,
                    gif.frameHeight);
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(Gui.icons);
            }
        }
    }
}
