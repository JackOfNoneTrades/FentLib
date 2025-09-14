package org.fentanylsolutions.fentlib.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
        final List<String> mixins = new ArrayList<>();

        /*
         * Reflections reflections = new Reflections(
         * new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader())
         * .setScanners(Scanners.TypesAnnotated));
         * Set<Class<?>> mixinClasses = reflections.getTypesAnnotatedWith(MixinTargetAnnotation.MixinTarget.class);
         * for (Class<?> clazz : mixinClasses) {
         * MixinTargetAnnotation.MixinTarget target = clazz.getAnnotation(MixinTargetAnnotation.MixinTarget.class);
         * if (target.phase() != MixinTargetAnnotation.Phase.LATE) continue;
         * boolean isClient = FMLLaunchHandler.side()
         * .isClient();
         * if (target.side() == MixinTargetAnnotation.Side.CLIENT && !isClient) continue;
         * if (target.side() == MixinTargetAnnotation.Side.SERVER && isClient) continue;
         * if (!specialIds.contains(target.modid()) && !Loader.isModLoaded(target.modid())) continue;
         * mixins.add(target.modid() + "." + clazz.getSimpleName());
         * }
         */

        return mixins;
    }
}
