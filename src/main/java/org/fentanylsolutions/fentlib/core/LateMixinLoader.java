package org.fentanylsolutions.fentlib.core;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.fentanylsolutions.fentlib.util.MiscUtil;
import org.fentanylsolutions.fentlib.util.MixinUtil;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@LateMixin
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LateMixinLoader implements ILateMixinLoader {

    private final List<String> specialIds = Arrays.asList("fml", "mcp", "minecraft", "minecraftforge");

    @Override
    public String getMixinConfig() {
        return "mixins.fentlib.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return new MixinUtil.MixinBuilder().addMixin("MixinGuiEnhancedModList", MiscUtil.Side.CLIENT, "enderio")
            .addMixin("MixinRemoveInfoButton", MiscUtil.Side.CLIENT, "fml")
            .build();
    }
}
