package org.fentanylsolutions.fentlib.core;

import java.util.List;
import java.util.Set;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader extends FentEarlyMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins." + FentLib.MODID + ".early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return new MixinUtil.MixinBuilder().addMixin("AccessorDimensionManager", MiscUtil.Side.BOTH, "minecraftforge")
            // Accessors
            .addMixin("AccessorGuiScreen", MiscUtil.Side.CLIENT)
            .addMixin("AccessorMinecraftServer", MiscUtil.Side.BOTH)
            .addMixin("AccessorS00PacketServerInfo", MiscUtil.Side.BOTH)
            .addMixin("AccessorNetHandlerStatusServer", MiscUtil.Side.BOTH)
            .addMixin("AccessorC00Handshake", MiscUtil.Side.BOTH)
            .addMixin("SessionAccessTokenOverrideMixin", MiscUtil.Side.CLIENT)

            // Rest
            .addMixin("EntityLivingBaseMixin", MiscUtil.Side.BOTH)

            .addMixin("MixinJarDiscoverer", MiscUtil.Side.BOTH, "fml")

            .addMixin("FeatureAnimatedIcon$MixinMinecraftServer", MiscUtil.Side.SERVER)
            .addMixin("FeatureAnimatedIcon$MixinServerData", MiscUtil.Side.CLIENT)
            .addMixin("FeatureAnimatedIcon$MixinOldServerPinger", MiscUtil.Side.CLIENT)
            .addMixin("FeatureAnimatedIcon$MixinServerListEntryNormal", MiscUtil.Side.CLIENT)
            .addMixin("FeatureAnimatedIcon$MixinPacketBuffer", MiscUtil.Side.BOTH)

            .addMixin("FeatureExtraPingData$NetHandlerStatusServerMixin", MiscUtil.Side.BOTH)
            .addMixin("FeatureExtraPingData$MixinServerStatusResponse", MiscUtil.Side.BOTH)
            .addMixin("FeatureExtraPingData$ServerStatusResponseSerializerMixin", MiscUtil.Side.BOTH)
            .addMixin("FeatureExtraPingData$MixinFMLClientHandler", MiscUtil.Side.BOTH)

            .addMixin("FeatureExtraPingData$MixinOldServerPinger", MiscUtil.Side.BOTH)

            .addMixin("FeatureExtraPingData$MixinNetHandlerHandshakeTCP", MiscUtil.Side.BOTH)
            .addMixin("FeatureExtraPingData$MixinNetworkManager", MiscUtil.Side.BOTH)

            .build();
    }
}
