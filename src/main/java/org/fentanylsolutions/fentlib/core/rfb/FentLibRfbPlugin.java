package org.fentanylsolutions.fentlib.core.rfb;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.Manifest;

import org.apache.logging.log4j.LogManager;
import org.fentanylsolutions.fentlib.core.DepLoaderClassPatcher;
import org.fentanylsolutions.fentlib.core.EarlyMixinConfig;
import org.objectweb.asm.tree.ClassNode;

import com.gtnewhorizons.retrofuturabootstrap.api.ClassNodeHandle;
import com.gtnewhorizons.retrofuturabootstrap.api.ExtensibleClassLoader;
import com.gtnewhorizons.retrofuturabootstrap.api.RetroFuturaBootstrap;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformer;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;

public final class FentLibRfbPlugin implements RfbPlugin {

    @Override
    public RfbClassTransformer[] makeEarlyTransformers() {
        File gameDirectory = RetroFuturaBootstrap.API.gameDirectory()
            .toFile();
        if (!EarlyMixinConfig.terminalDepLoaderProgress(gameDirectory)) {
            return new RfbClassTransformer[0];
        }
        return new RfbClassTransformer[] { new DepLoaderRfbTransformer() };
    }

    private static final class DepLoaderRfbTransformer implements RfbClassTransformer {

        @Override
        public String id() {
            return "deploader-terminal-progress";
        }

        @Override
        public boolean shouldTransformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest,
            String className, ClassNodeHandle classNode) {
            return DepLoaderClassPatcher.TARGET_CLASS.equals(className)
                || DepLoaderClassPatcher.TARGET_CLASS.replace('.', '/')
                    .equals(className);
        }

        @Override
        public void transformClass(ExtensibleClassLoader classLoader, Context context, Manifest manifest,
            String className, ClassNodeHandle classNode) {
            ClassNode node = classNode.getNode();
            if (DepLoaderClassPatcher.patch(node)) {
                classNode.computeMaxs();
                markDirty(classNode, node);
                LogManager.getLogger("FentLib Bootstrap")
                    .info("Redirected FalsePattern DepLoader progress to the terminal.");
            }
        }

        private static void markDirty(ClassNodeHandle classNode, ClassNode node) {
            try {
                ClassNodeHandle.class.getMethod("markDirty")
                    .invoke(classNode);
            } catch (NoSuchMethodException ignored) {
                classNode.setNode(node);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Could not mark the patched FalsePattern DepLoader class as changed", e);
            }
        }
    }
}
