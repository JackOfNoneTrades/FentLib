package org.fentanylsolutions.fentlib.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fentanylsolutions.fentlib.FentLib;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IEarlyMixinLoader, IFMLLoadingPlugin {

    /*
     * import java.io.File;
     * import java.io.FileInputStream;
     * import cpw.mods.fml.common.Loader;
     * import cpw.mods.fml.relauncher.FMLLaunchHandler;
     * import org.spongepowered.asm.lib.AnnotationVisitor;
     * import org.spongepowered.asm.lib.ClassReader;
     * import org.spongepowered.asm.lib.ClassVisitor;
     * import org.spongepowered.asm.lib.Opcodes;
     * import java.util.zip.ZipEntry;
     * import java.util.zip.ZipInputStream;
     */

    private final List<String> specialIds = Arrays.asList("fml", "mcp", "minecraft", "minecraftforge");

    @Override
    public String getMixinConfig() {
        return "mixins." + FentLib.MODID + ".early.json";
    }

    /*
     * @Override
     * public List<String> getMixins(Set<String> loadedCoreMods) {
     * final List<String> mixins = new ArrayList<>();
     * Reflections reflections = new Reflections(
     * new ConfigurationBuilder().setUrls(ClasspathHelper.forClassLoader())
     * .setScanners(Scanners.TypesAnnotated));
     * Set<Class<?>> mixinClasses = reflections.getTypesAnnotatedWith(MixinTargetAnnotation.MixinTarget.class);
     * for (Class<?> clazz : mixinClasses) {
     * MixinTargetAnnotation.MixinTarget target = clazz.getAnnotation(MixinTargetAnnotation.MixinTarget.class);
     * if (target.phase() != MixinTargetAnnotation.Phase.EARLY) continue;
     * boolean isClient = FMLLaunchHandler.side()
     * .isClient();
     * if (target.side() == MixinTargetAnnotation.Side.CLIENT && !isClient) continue;
     * if (target.side() == MixinTargetAnnotation.Side.SERVER && isClient) continue;
     * if (!specialIds.contains(target.modid()) && !Loader.isModLoaded(target.modid())) continue;
     * mixins.add(target.modid() + "." + clazz.getSimpleName());
     * }
     * return mixins;
     * }
     */

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<>();

        mixins.add("minecraftforge.AccessorDimensionManager");

        return mixins;

        /*
         * String DEFAULT_MODID = "minecraft";
         * String DEFAULT_PHASE = "LATE";
         * String DEFAULT_SIDE = "BOTH";
         * String annotationDesc = "Lorg/fentanylsolutions/fentlib/util/MixinTargetAnnotation$MixinTarget;";
         * String matchPackage = "early.";
         * try {
         * String[] classpathEntries = System.getProperty("java.class.path")
         * .split(File.pathSeparator);
         * for (String path : classpathEntries) {
         * File jarFile = new File(path);
         * if (!jarFile.isFile() || !jarFile.getName()
         * .endsWith(".jar")) continue;
         * try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
         * ZipEntry entry;
         * while ((entry = zip.getNextEntry()) != null) {
         * if (!entry.getName()
         * .endsWith(".class")) continue;
         * ClassReader reader = new ClassReader(zip);
         * reader.accept(new ClassVisitor(Opcodes.ASM9) {
         * String className;
         * @Override
         * public void visit(int version, int access, String name, String signature, String superName,
         * String[] interfaces) {
         * className = name.replace('/', '.');
         * }
         * @Override
         * public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         * if (!desc
         * .equals("Lorg/fentanylsolutions/fentlib/util/MixinTargetAnnotation$MixinTarget;"))
         * return null;
         * return new AnnotationVisitor(Opcodes.ASM9) {
         * String modid = DEFAULT_MODID;
         * String phase = DEFAULT_PHASE;
         * String side = DEFAULT_SIDE;
         * @Override
         * public void visit(String name, Object value) {
         * if ("modid".equals(name)) {
         * modid = (String) value;
         * }
         * }
         * @Override
         * public void visitEnum(String name, String descriptor, String value) {
         * switch (name) {
         * case "phase" -> phase = value;
         * case "side" -> side = value;
         * }
         * }
         * @Override
         * public void visitEnd() {
         * if (!"EARLY".equals(phase)) return;
         * boolean isClient = FMLLaunchHandler.side()
         * .isClient();
         * if ("CLIENT".equals(side) && !isClient) return;
         * if ("SERVER".equals(side) && isClient) return;
         * if (!specialIds.contains(modid) && !Loader.isModLoaded(modid)) return;
         * int earlyIndex = className.indexOf("early.");
         * if (earlyIndex != -1) {
         * String relativeName = className.substring(earlyIndex + "early.".length());
         * mixins.add(relativeName);
         * } else {
         * FentLib.LOG
         * .error("Could not extract relative mixin name from: {}", className);
         * }
         * }
         * };
         * }
         * }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
         * }
         * }
         * }
         * } catch (Exception e) {
         * FentLib.LOG.error("Failed to scan for mixins via ASM: {}", String.valueOf(e));
         * }
         * return mixins;
         */
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
