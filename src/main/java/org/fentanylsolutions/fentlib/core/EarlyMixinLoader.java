package org.fentanylsolutions.fentlib.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IEarlyMixinLoader, IFMLLoadingPlugin {

    private final List<String> specialIds = Arrays.asList("fml", "mcp", "minecraft", "minecraftforge");

    @Override
    public String getMixinConfig() {
        return "mixins." + FentLib.MODID + ".early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return new MixinUtil.MixinBuilder().addMixin("AccessorDimensionManager", MiscUtil.Side.BOTH, "minecraftforge")
            .addMixin("EntityLivingBaseMixin", MiscUtil.Side.BOTH)
            .addMixin("AccessorGuiScreen", MiscUtil.Side.CLIENT)
            .addMixin("FeatureSaveServerDataAsJson$MixinSaveAsJson", MiscUtil.Side.CLIENT)

            .addMixin("MixinJarDiscoverer", MiscUtil.Side.BOTH, "fml")
            .build();
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
