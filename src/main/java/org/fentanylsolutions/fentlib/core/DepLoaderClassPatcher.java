package org.fentanylsolutions.fentlib.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class DepLoaderClassPatcher {

    public static final String TARGET_CLASS = "com.falsepattern.deploader.DependencyLoaderImpl";
    public static final String LAUNCH_WRAPPER_STUB_SUFFIX = "deploader.DeploaderStub$LaunchWrapperUtil";
    private static final String DEPLOADER_TRANSFORMER_EXCLUSION = "com_falsepattern_deploader_";
    private static final String BOOTSTRAP_TRANSFORMER_EXCLUSION = "com_falsepattern_deploader_Bootstrap";
    private static final String LOAD_METHOD = "executeArtifactLoading";
    private static final String LOAD_METHOD_DESC = "(Ljava/util/List;Z)Ljava/util/Set;";
    private static final String VIZ_METHOD = "getVizThread";
    private static final String VIZ_METHOD_DESC = "(Ljava/util/concurrent/atomic/AtomicBoolean;Ljava/util/Map;"
        + "Ljavax/swing/JProgressBar;Ljavax/swing/JProgressBar;Ljavax/swing/JFrame;)Ljava/lang/Thread;";
    private static final String HOOK_OWNER = "org/fentanylsolutions/fentlib/core/DepLoaderTerminalProgress";
    private static final String HOOK_METHOD = "createThread";
    private static final String HOOK_METHOD_DESC = "(Ljava/util/concurrent/atomic/AtomicBoolean;Ljava/util/Map;"
        + "Ljavax/swing/JFrame;)Ljava/lang/Thread;";
    private static final String NOTICE_METHOD = "printNotice";
    private static final String GUARD_METHOD = "shouldCreateThread";
    private static final String GUARD_METHOD_DESC = "(Ljavax/swing/JFrame;Z)Z";
    private static final String DOWNLOAD_MESSAGE = "FalsePatternLib is downloading dependencies. Please wait...";
    private static final String SEPARATOR = "-----------------------------------------------------------";
    private static final String MAC_MESSAGE = "MacOS detected, not creating progress window (your OS is buggy)";

    private DepLoaderClassPatcher() {}

    public static boolean isLaunchWrapperStub(String className) {
        return className != null && className.replace('/', '.')
            .endsWith(LAUNCH_WRAPPER_STUB_SUFFIX);
    }

    public static boolean narrowLaunchWrapperTransformerExclusion(ClassNode classNode) {
        // The stub must load Bootstrap without transformers, but its package-wide exclusion also hides the
        // DependencyLoaderImpl class that contains the graphical progress renderer.
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction
                != null; instruction = instruction.getNext()) {
                if (instruction instanceof LdcInsnNode
                    && DEPLOADER_TRANSFORMER_EXCLUSION.equals(((LdcInsnNode) instruction).cst)) {
                    ((LdcInsnNode) instruction).cst = BOOTSTRAP_TRANSFORMER_EXCLUSION;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean patch(ClassNode classNode) {
        MethodNode loadMethod = null;
        MethodNode visualizerMethod = null;
        for (MethodNode method : classNode.methods) {
            if (LOAD_METHOD.equals(method.name) && LOAD_METHOD_DESC.equals(method.desc)) {
                loadMethod = method;
            } else if (VIZ_METHOD.equals(method.name) && VIZ_METHOD_DESC.equals(method.desc)) {
                visualizerMethod = method;
            }
        }
        if (loadMethod == null || visualizerMethod == null || !patchLoadMethod(loadMethod)) {
            return false;
        }
        if (callsHook(visualizerMethod, HOOK_METHOD)) {
            return true;
        }

        InsnList replacement = new InsnList();
        replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
        replacement.add(new VarInsnNode(Opcodes.ALOAD, 1));
        replacement.add(new VarInsnNode(Opcodes.ALOAD, 4));
        replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER, HOOK_METHOD, HOOK_METHOD_DESC, false));
        replacement.add(new InsnNode(Opcodes.ARETURN));

        visualizerMethod.instructions.clear();
        visualizerMethod.instructions.add(replacement);
        visualizerMethod.tryCatchBlocks.clear();
        visualizerMethod.localVariables = null;
        visualizerMethod.visibleLocalVariableAnnotations = null;
        visualizerMethod.invisibleLocalVariableAnnotations = null;
        visualizerMethod.maxStack = 3;
        visualizerMethod.maxLocals = 5;
        return true;
    }

    private static boolean patchLoadMethod(MethodNode method) {
        if (callsHook(method, NOTICE_METHOD)) {
            return true;
        }

        MethodInsnNode bannerEnd = null;
        AbstractInsnNode macLogField = null;
        LdcInsnNode macMessage = null;
        MethodInsnNode macLogCall = null;
        JumpInsnNode frameNullGuard = null;
        boolean sawDownloadMessage = false;
        boolean sawTrailingSeparator = false;

        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction
            != null; instruction = instruction.getNext()) {
            if (instruction instanceof LdcInsnNode) {
                Object constant = ((LdcInsnNode) instruction).cst;
                if (DOWNLOAD_MESSAGE.equals(constant)) {
                    sawDownloadMessage = true;
                } else if (sawDownloadMessage && SEPARATOR.equals(constant)) {
                    sawTrailingSeparator = true;
                } else if (MAC_MESSAGE.equals(constant)) {
                    macMessage = (LdcInsnNode) instruction;
                }
            } else if (sawTrailingSeparator && instruction instanceof MethodInsnNode
                && "info".equals(((MethodInsnNode) instruction).name)) {
                    bannerEnd = (MethodInsnNode) instruction;
                    sawTrailingSeparator = false;
                } else
                if (instruction instanceof MethodInsnNode && VIZ_METHOD.equals(((MethodInsnNode) instruction).name)
                    && VIZ_METHOD_DESC.equals(((MethodInsnNode) instruction).desc)) {
                        for (AbstractInsnNode previous = previousMeaningful(instruction); previous
                            != null; previous = previousMeaningful(previous)) {
                            if (previous instanceof JumpInsnNode && previous.getOpcode() == Opcodes.IFNULL) {
                                frameNullGuard = (JumpInsnNode) previous;
                                break;
                            }
                        }
                    }
        }

        if (macMessage != null) {
            macLogField = previousMeaningful(macMessage);
            AbstractInsnNode call = nextMeaningful(macMessage);
            if (call instanceof MethodInsnNode && "info".equals(((MethodInsnNode) call).name)) {
                macLogCall = (MethodInsnNode) call;
            }
        }
        if (bannerEnd == null || macLogField == null
            || macLogField.getOpcode() != Opcodes.GETSTATIC
            || macLogCall == null
            || frameNullGuard == null) {
            return false;
        }

        method.instructions
            .insert(bannerEnd, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER, NOTICE_METHOD, "()V", false));
        method.instructions.remove(macLogField);
        method.instructions.remove(macMessage);
        method.instructions.remove(macLogCall);

        InsnList macFallback = new InsnList();
        macFallback.add(new VarInsnNode(Opcodes.ILOAD, 1));
        macFallback.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER, GUARD_METHOD, GUARD_METHOD_DESC, false));
        method.instructions.insertBefore(frameNullGuard, macFallback);
        frameNullGuard.setOpcode(Opcodes.IFEQ);
        return true;
    }

    private static boolean callsHook(MethodNode method, String hookMethod) {
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction
            != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode && HOOK_OWNER.equals(((MethodInsnNode) instruction).owner)
                && hookMethod.equals(((MethodInsnNode) instruction).name)) {
                return true;
            }
        }
        return false;
    }

    private static AbstractInsnNode previousMeaningful(AbstractInsnNode instruction) {
        AbstractInsnNode previous = instruction.getPrevious();
        while (previous != null && previous.getOpcode() < 0) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    private static AbstractInsnNode nextMeaningful(AbstractInsnNode instruction) {
        AbstractInsnNode next = instruction.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
    }
}
