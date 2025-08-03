package org.fentanylsolutions.fentlib.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IEarlyMixinLoader, IFMLLoadingPlugin {

    @Override
    public String getMixinConfig() {
        return "mixins.fentlib.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        if (FMLLaunchHandler.side()
            .isClient()) {
            mixins.add("minecraft.GuiScreenAccessor");

            // Looks like mixins targeting Event Handlers need to be loaded early
            mixins.add("carbonconfig.EventHandlerMixin");
        }

        mixins.add("minecraftforge.DimensionManagerAccessor");
        mixins.add("fml.FMLControlledNamespacedRegistryAccessor");
        mixins.add("fml.RegistryDelegate_DelegateAccessor");

        mixins.add("minecraft.EntityListAccessor");
        mixins.add("minecraft.RegistryNamespacedAccessor");
        mixins.add("minecraft.EntityLivingBaseMixin");
        mixins.add("minecraft.GameProfileAccessor");

        return mixins;
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
