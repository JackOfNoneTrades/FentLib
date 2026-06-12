package org.fentanylsolutions.fentlib.core;

import org.fentanylsolutions.fentlib.util.MiscUtil.Side;
import org.fentanylsolutions.fentlib.util.MixinUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil.Phase;

public class Mixins extends FentMixins {

    private static final Mixins INSTANCE = new Mixins();

    @Override
    protected void registerMixins(MixinUtil.Registry registry) {
        // Minecraft Accessors
        registry.mixin("AccessorDimensionManager")
            .modid("minecraftforge")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("AccessorGuiScreen")
            .side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("AccessorMinecraftServer")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("AccessorS00PacketServerInfo")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("AccessorNetHandlerStatusServer")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("AccessorC00Handshake")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("AccessorNetworkSystem")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();
        registry.mixin("SessionAccessTokenOverrideMixin")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();
        registry.mixin("MixinMinecraftGuiOpenLogger")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();
        registry.mixin("MixinSoundManager")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();

        // Minecraft Mixins
        registry.mixin("EntityLivingBaseMixin")
            .phase(Phase.EARLY)
            .build();
        if (EarlyMixinConfig.enableJarDiscovererMixin()) {
            registry.mixin("MixinJarDiscoverer")
                .modid("fml")
                .phase(Phase.EARLY)
                .build();
        }
        registry.mixin("FeatureAnimatedIcon$MixinMinecraftServer")
            .side(Side.SERVER)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureAnimatedIcon$MixinServerData")
            .side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureAnimatedIcon$MixinOldServerPinger")
            .side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureAnimatedIcon$MixinServerListEntryNormal")
            .side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureServerListReorder$MixinGuiMultiplayer")
            .side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureServerListReorder$MixinServerListEntryNormal")
            .side(Side.CLIENT)
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureAnimatedIcon$MixinPacketBuffer")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$NetHandlerStatusServerMixin")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$ServerStatusResponseSerializerMixin")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$MixinServerStatusResponse")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$MixinFMLClientHandler")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$MixinOldServerPinger")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$MixinNetHandlerHandshakeTCP")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureExtraPingData$MixinNetworkManager")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureFishingLoot$MixinEntityFishHook")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureFishingLoot$MixinLOTREntityFishHook")
            .modid("lotr")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("FeatureFishingLoot$MixinLOTRFishing")
            .modid("lotr")
            .phase(Phase.EARLY)
            .build();
        registry.mixin("MixinNetworkSystemPortUnification")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();

        // IPv6 support for vanilla IP handling
        registry.mixin("MixinBanListIpv6")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();
        registry.mixin("MixinEntityPlayerMPIpv6")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();
        registry.mixin("MixinNetHandlerLoginServerIpv6")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();
        registry.mixin("MixinCommandBanIpIpv6")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();
        registry.mixin("MixinCommandPardonIpIpv6")
            .phase(Phase.EARLY)
            .side(Side.SERVER)
            .build();
        registry.mixin("FeatureIpv6Lan$MixinThreadLanServerPing")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();
        registry.mixin("FeatureIpv6Lan$MixinThreadLanServerFind")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();
        registry.mixin("FeatureIpv6Lan$MixinLanServerList")
            .phase(Phase.EARLY)
            .side(Side.CLIENT)
            .build();

        // Other Accessors
        registry.mixin("AccessorWidget")
            .side(Side.CLIENT)
            .modid("modularui2")
            .phase(Phase.LATE)
            .build();

        // Other mixins
        registry.mixin("MixinGuiEnhancedModList")
            .side(Side.CLIENT)
            .modid("enderio")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinRemoveInfoButton")
            .side(Side.CLIENT)
            .modid("fml")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinModularPanel")
            .side(Side.CLIENT)
            .modid("modularui2")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinCatalogueModListScreenBackground")
            .side(Side.CLIENT)
            .modid("catalogue")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinVisualManager")
            .side(Side.CLIENT)
            .modid("sonicvisuals")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinEntityPlayerPreview")
            .side(Side.CLIENT)
            .modid("betterquesting")
            .extraModid("BetterQuesting")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinPanelPlayerPortrait")
            .side(Side.CLIENT)
            .modid("betterquesting")
            .extraModid("BetterQuesting")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinLOTRGuiMap")
            .side(Side.CLIENT)
            .modid("lotr")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinModernTabRenderer")
            .side(Side.CLIENT)
            .modid("serverutilities")
            .extraModid("ServerUtilities")
            .phase(Phase.LATE)
            .build();
        registry.mixin("MixinVoiceHandler")
            .side(Side.BOTH)
            .modid("ServerTools")
            .phase(Phase.LATE)
            .build();
    }

    public static java.util.List<String> getEarlyMixinsForLoader() {
        return INSTANCE.getEarlyMixins();
    }

    public static java.util.List<String> getLateMixinsForLoader(java.util.Set<String> loadedCoreMods) {
        return INSTANCE.getLateMixins(loadedCoreMods);
    }
}
