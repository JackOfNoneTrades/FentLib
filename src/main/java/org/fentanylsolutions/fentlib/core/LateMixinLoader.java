package org.fentanylsolutions.fentlib.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fentanylsolutions.fentlib.Config;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@LateMixin
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.fentlib.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        if (Loader.isModLoaded("carbonconfig")) {
            if (Config.carbonConfigFixes) {
                mixins.add("carbonconfig.HelpersMixin");
                if (FMLLaunchHandler.side()
                    .isClient()) {
                    mixins.add("carbonconfig.ArrayElementAccessor");
                    mixins.add("carbonconfig.AlignOffsetAccessor");
                    mixins.add("carbonconfig.ConfigElementAccessor");
                    mixins.add("carbonconfig.ElementAccessor");
                    mixins.add("carbonconfig.ConfigElementMixin");
                    mixins.add("carbonconfig.CarbonConfigMixin");
                }
            }

            if (FMLLaunchHandler.side()
                .isClient()) {
                mixins.add("carbonconfig.ListScreenMixin");
                mixins.add("carbonconfig.ConfigSelectorScreenAccessor");
                mixins.add("carbonconfig.ConfigScreenAccessor");
            }

            mixins.add("carbonconfig.ConfigMixin");
        }
        return mixins;
    }
}
