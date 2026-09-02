package org.fentanylsolutions.fentlib.core;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

public final class DepLoaderLaunchTransformer implements IClassTransformer {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    static void registerEarly() {
        if (!EarlyMixinConfig.terminalDepLoaderProgress() || !REGISTERED.compareAndSet(false, true)) {
            return;
        }
        try {
            Launch.classLoader.addTransformerExclusion("org.fentanylsolutions.fentlib.core.DepLoader");
            Launch.classLoader.registerTransformer(DepLoaderLaunchTransformer.class.getName());
        } catch (RuntimeException e) {
            REGISTERED.set(false);
            FMLRelaunchLog.warning("FentLib could not register its DepLoader terminal progress transformer: %s", e);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return basicClass;
        }

        boolean dependencyLoader = DepLoaderClassPatcher.TARGET_CLASS.equals(name)
            || DepLoaderClassPatcher.TARGET_CLASS.equals(transformedName);
        boolean launchWrapperStub = DepLoaderClassPatcher.isLaunchWrapperStub(name)
            || DepLoaderClassPatcher.isLaunchWrapperStub(transformedName);
        if (!dependencyLoader && !launchWrapperStub) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        if (launchWrapperStub) {
            if (!DepLoaderClassPatcher.narrowLaunchWrapperTransformerExclusion(classNode)) {
                FMLRelaunchLog.warning("FentLib could not find FalsePattern DepLoader's transformer exclusion.");
                return basicClass;
            }
            FMLRelaunchLog.info("FentLib enabled transformations for FalsePattern DepLoader.");
        } else if (!DepLoaderClassPatcher.patch(classNode)) {
            FMLRelaunchLog.warning("FentLib could not find FalsePattern DepLoader's progress visualizer method.");
            return basicClass;
        } else {
            FMLRelaunchLog.info("FentLib redirected FalsePattern DepLoader progress to the terminal.");
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
