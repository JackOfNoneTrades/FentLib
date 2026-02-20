package org.fentanylsolutions.fentlib.core;

import org.fentanylsolutions.fentlib.util.MiscUtil.Side;
import org.fentanylsolutions.fentlib.util.MixinUtil.MixinBuilder;
import org.fentanylsolutions.fentlib.util.MixinUtil.Phase;

public class Mixins extends FentMixins {

    static {
        staticInit();

        // Minecraft Accessors
        new MixinBuilder("AccessorDimensionManager").modid("minecraftforge")
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("AccessorGuiScreen").side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("AccessorMinecraftServer").phase(Phase.EARLY)
            .build();
        new MixinBuilder("AccessorS00PacketServerInfo").phase(Phase.EARLY)
            .build();
        new MixinBuilder("AccessorNetHandlerStatusServer").phase(Phase.EARLY)
            .build();
        new MixinBuilder("AccessorC00Handshake").phase(Phase.EARLY)
            .build();
        new MixinBuilder("SessionAccessTokenOverrideMixin").phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();

        // Minecraft Mixins
        new MixinBuilder("EntityLivingBaseMixin").phase(Phase.EARLY)
            .build();
        new MixinBuilder("MixinJarDiscoverer").modid("fml")
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureAnimatedIcon$MixinMinecraftServer").side(Side.SERVER)
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureAnimatedIcon$MixinServerData").side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureAnimatedIcon$MixinOldServerPinger").side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureAnimatedIcon$MixinServerListEntryNormal").side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureAnimatedIcon$MixinPacketBuffer").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$NetHandlerStatusServerMixin").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$ServerStatusResponseSerializerMixin").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$MixinServerStatusResponse").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$MixinFMLClientHandler").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$MixinOldServerPinger").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$MixinNetHandlerHandshakeTCP").phase(Phase.EARLY)
            .build();
        new MixinBuilder("FeatureExtraPingData$MixinNetworkManager").phase(Phase.EARLY)
            .build();

        // Other Accessors
        new MixinBuilder("AccessorWidget").side(Side.CLIENT)
            .modid("modularui2")
            .phase(Phase.LATE)
            .build();

        // Other mixins
        new MixinBuilder("MixinGuiEnhancedModList").side(Side.CLIENT)
            .modid("enderio")
            .phase(Phase.LATE)
            .build();
        new MixinBuilder("MixinRemoveInfoButton").side(Side.CLIENT)
            .modid("fml")
            .phase(Phase.LATE)
            .build();
        new MixinBuilder("MixinModularPanel").side(Side.CLIENT)
            .modid("modularui2")
            .phase(Phase.LATE)
            .build();
    }
}
