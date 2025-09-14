package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.NBTTagCompound;

import org.fentanylsolutions.fentlib.mixins.DummyTarget;
import org.fentanylsolutions.fentlib.util.GifUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DummyTarget.class)
public class FeatureAnimatedIcon {

    public static interface IAnimatedServerData {

        boolean getIsAnimatedIcon();

        void setIsAnimatedIcon(boolean val);

        GifUtil.GifAnimationData getGifAnimationData();

        void setGifAnimationData(GifUtil.GifAnimationData data);
    }

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
}
