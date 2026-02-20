package org.fentanylsolutions.fentlib.core;

import java.util.List;
import java.util.Set;

import org.fentanylsolutions.fentlib.FentLib;

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
        new Mixins();
        return Mixins.getEarlyMixins();
    }
}
