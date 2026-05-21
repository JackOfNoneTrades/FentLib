package org.fentanylsolutions.fentlib.util;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

import org.fentanylsolutions.fentlib.FentLib;
import org.lwjgl.Sys;

public class FileUtil {

    public static void writeFileBytes(File file, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    public static void writeStringToFile(File file, String data) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(data);
        writer.close();
    }

    public static File createFolderIfNotExists(File folder) {
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                FentLib.LOG.error("Failed to create directory {}", folder.getName());
                return null;
            }
        }
        return folder;
    }

    public static File createFolderIfNotExists(String path) {
        return createFolderIfNotExists(new File(path));
    }

    public static File createFolderIfNotExists(String path1, String path2) {
        return createFolderIfNotExists(
            Paths.get(path1, path2)
                .toString());
    }

    public static File createFolderIfNotExists(File path1, String path2) {
        return createFolderIfNotExists(
            Paths.get(path1.getPath(), path2)
                .toString());
    }

    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    public static String hashStringBlob(String blob) {
        // Simple hash - SHA-1 of the string bytes
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(blob.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // Fallback to simple hashCode if SHA-1 fails
            return Integer.toHexString(blob.hashCode());
        }
    }

    public static File getMinecraftDir() {
        if (MiscUtil.isServer()) {
            MinecraftServer server = MinecraftServer.getServer();
            if (server != null) {
                File serverBaseDir = server.getFile("");
                if (serverBaseDir != null) {
                    return serverBaseDir;
                }
            }
            if (Launch.minecraftHome != null) {
                return Launch.minecraftHome;
            }
            String userDir = System.getProperty("user.dir");
            if (userDir != null && !userDir.isEmpty()) {
                return new File(userDir);
            }
            return new File(".");
        } else {
            return Minecraft.getMinecraft().mcDataDir;
        }
    }

    public static boolean openFolder(File folder) {
        if (folder == null) {
            return false;
        }
        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }

        String absolutePath = folder.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX) {
            try {
                Runtime.getRuntime()
                    .exec(new String[] { "/usr/bin/open", absolutePath });
                return true;
            } catch (IOException ioexception) {
                FentLib.LOG.error("Problem opening folder", ioexception);
            }
        } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            String openCommand = String.format("cmd.exe /C start \"Open file\" \"%s\"", absolutePath);
            try {
                Runtime.getRuntime()
                    .exec(openCommand);
                return true;
            } catch (IOException ioexception) {
                FentLib.LOG.error("Problem opening folder", ioexception);
            }
        }

        if (openFolderWithAwtDesktop(folder)) {
            return true;
        }

        FentLib.LOG.info("Opening folder via system class fallback");
        try {
            Class<?> sysX = Class.forName("org.lwjglx.Sys");
            Object ok = sysX.getMethod("openURL", String.class)
                .invoke(null, "file://" + absolutePath);
            if (ok instanceof Boolean) {
                return (Boolean) ok;
            }
            return true;
        } catch (Throwable ignored) {
            try {
                Sys.openURL("file://" + absolutePath);
                return true;
            } catch (Throwable t) {
                FentLib.LOG.error("Failed to open folder via Sys fallback", t);
            }
        }

        return false;
    }

    public static final class FilePickerResult {

        public enum Status {
            SELECTED,
            CANCELLED,
            UNAVAILABLE,
            ERROR
        }

        private final Status status;
        private final File file;
        private final String message;

        private FilePickerResult(Status status, File file, String message) {
            this.status = status;
            this.file = file;
            this.message = message;
        }

        public static FilePickerResult selected(File file) {
            return new FilePickerResult(Status.SELECTED, file, null);
        }

        public static FilePickerResult cancelled() {
            return new FilePickerResult(Status.CANCELLED, null, null);
        }

        public static FilePickerResult unavailable(String message) {
            return new FilePickerResult(Status.UNAVAILABLE, null, message);
        }

        public static FilePickerResult error(String message) {
            return new FilePickerResult(Status.ERROR, null, message);
        }

        public Status getStatus() {
            return status;
        }

        public File getFile() {
            return file;
        }

        public String getMessage() {
            return message;
        }
    }

    public static boolean openUri(URI uri) {
        if (uri == null) return false;

        if (openWithAwtDesktop(uri)) return true;
        if (openWithLwjgl3ifyDesktop(uri)) return true;
        if (openWithSys("org.lwjglx.Sys", uri.toString())) return true;
        return openWithSys("org.lwjgl.Sys", uri.toString());
    }

    public static FilePickerResult pickFile(String title, File initialDirectory, String... extensions) {
        if (extensions == null || extensions.length == 0) {
            extensions = new String[] { "png", "gif" };
        }

        if (isMacOs()) {
            FilePickerResult macResult = pickWithMacOsaScript(title, initialDirectory, extensions);
            if (macResult.getStatus() != FilePickerResult.Status.UNAVAILABLE) {
                return validateSelection(macResult, extensions);
            }
        }

        if (!isWindows()) {
            FilePickerResult tinyfdResult = pickWithTinyfd(title, initialDirectory, extensions);
            if (tinyfdResult.getStatus() != FilePickerResult.Status.UNAVAILABLE) {
                return validateSelection(tinyfdResult, extensions);
            }
        }

        FilePickerResult awtResult = pickWithAwt(title, initialDirectory, extensions);
        if (awtResult.getStatus() != FilePickerResult.Status.UNAVAILABLE) {
            return validateSelection(awtResult, extensions);
        }

        return FilePickerResult.unavailable(GuiText.tr("fentlib.gui.file_picker.unavailable"));
    }

    public static File getDefaultFileSelectionDirectory() {
        String userHome = System.getProperty("user.home", "");
        if (userHome != null && !userHome.trim()
            .isEmpty()) {
            File home = new File(userHome);
            if (home.isDirectory()) {
                return home;
            }
        }
        return new File(".");
    }

    public static FilePickerResult pickWithAwt(String title, File initialDirectory, String[] extensions) {
        try {
            FileDialog dialog = new FileDialog((Frame) null, safeTitle(title), FileDialog.LOAD);

            dialog.setDirectory(defaultPickerPath(initialDirectory));
            dialog.setFile(buildAwtFilter(extensions));
            dialog.setVisible(true);

            String directory = dialog.getDirectory();
            String file = dialog.getFile();

            if (file == null || file.isEmpty()) {
                return FilePickerResult.cancelled();
            }

            File selectedFile = new File(directory, file);
            return FilePickerResult.selected(selectedFile);
        } catch (Throwable t) {
            FentLib.LOG.error("Native file picker failed in AWT path", t);
            String message = t.getMessage() != null ? t.getMessage()
                : t.getClass()
                    .getSimpleName();
            return FilePickerResult.error(GuiText.tr("fentlib.gui.common.failed_message", message));
        }
    }

    private static FilePickerResult pickWithTinyfd(String title, File initialDirectory, String[] extensions) {
        try {
            Class<?> pointerBufferClass = Class.forName("org.lwjgl.PointerBuffer");
            Class<?> tinyDialogsClass = Class.forName("org.lwjgl.util.tinyfd.TinyFileDialogs");
            Object filters = createTinyfdFilters(pointerBufferClass, extensions);
            Object selectedPath = tinyDialogsClass
                .getMethod(
                    "tinyfd_openFileDialog",
                    CharSequence.class,
                    CharSequence.class,
                    pointerBufferClass,
                    CharSequence.class,
                    boolean.class)
                .invoke(
                    null,
                    safeTitle(title),
                    defaultPickerPath(initialDirectory),
                    filters,
                    buildFilterLabel(extensions),
                    false);

            if (selectedPath == null) {
                return FilePickerResult.cancelled();
            }
            String path = selectedPath.toString()
                .trim();
            if (path.isEmpty()) {
                return FilePickerResult.cancelled();
            }
            return FilePickerResult.selected(new File(path));
        } catch (ClassNotFoundException e) {
            return FilePickerResult.unavailable(GuiText.tr("fentlib.gui.file_picker.tinyfd_missing"));
        } catch (Throwable t) {
            FentLib.LOG.error("Native file picker failed in tinyfd path", t);
            String message = t.getMessage() != null ? t.getMessage()
                : t.getClass()
                    .getSimpleName();
            return FilePickerResult.error(GuiText.tr("fentlib.gui.common.failed_message", message));
        }
    }

    private static Object createTinyfdFilters(Class<?> pointerBufferClass, String[] extensions) {
        try {
            Class<?> bufferUtilsClass = Class.forName("org.lwjgl.BufferUtils");
            Class<?> memoryUtilClass = Class.forName("org.lwjgl.system.MemoryUtil");
            Object filters = bufferUtilsClass.getMethod("createPointerBuffer", int.class)
                .invoke(null, extensions.length);
            for (String ext : extensions) {
                Object pattern = memoryUtilClass.getMethod("memUTF8", CharSequence.class)
                    .invoke(null, "*." + ext);
                pointerBufferClass.getMethod("put", java.nio.ByteBuffer.class)
                    .invoke(filters, pattern);
            }
            pointerBufferClass.getMethod("flip")
                .invoke(filters);
            return filters;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static FilePickerResult pickWithMacOsaScript(String title, File initialDirectory, String[] extensions) {
        if (!isMacOs()) {
            return FilePickerResult.unavailable(GuiText.tr("fentlib.gui.file_picker.not_macos"));
        }

        String typeList = buildAppleScriptTypeList(extensions);
        String initialPath = initialDirectory != null ? initialDirectory.getAbsolutePath() : "";
        List<String> command = new ArrayList<>();
        command.add("osascript");
        command.add("-e");
        command.add("try");
        command.add("-e");
        if (!initialPath.isEmpty()) {
            command.add(
                "set _picked to choose file with prompt \"" + escapeAppleScript(safeTitle(title))
                    + "\" default location POSIX file \""
                    + escapeAppleScript(initialPath)
                    + "\" of type {"
                    + typeList
                    + "}");
        } else {
            command.add(
                "set _picked to choose file with prompt \"" + escapeAppleScript(safeTitle(title))
                    + "\" of type {"
                    + typeList
                    + "}");
        }
        command.add("-e");
        command.add("return POSIX path of _picked");
        command.add("-e");
        command.add("on error number -128");
        command.add("-e");
        command.add("return \"\"");
        command.add("-e");
        command.add("end try");

        Process process = null;
        try {
            process = new ProcessBuilder(command).start();
            String output = readAll(process.getInputStream());
            int code = process.waitFor();
            if (code != 0) {
                String err = readAll(process.getErrorStream());
                if (err != null && !err.trim()
                    .isEmpty()) {
                    return FilePickerResult.error(GuiText.tr("fentlib.gui.common.failed_message", err.trim()));
                }
                return FilePickerResult.error(GuiText.tr("fentlib.gui.file_picker.macos_failed"));
            }

            String path = output != null ? output.trim() : "";
            if (path.isEmpty()) {
                return FilePickerResult.cancelled();
            }
            return FilePickerResult.selected(new File(path));
        } catch (IOException e) {
            return FilePickerResult.unavailable(GuiText.tr("fentlib.gui.file_picker.osascript_missing"));
        } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
            return FilePickerResult.error(GuiText.tr("fentlib.gui.file_picker.interrupted"));
        } catch (Throwable t) {
            FentLib.LOG.error("Native file picker failed in macOS osascript path", t);
            String message = t.getMessage() != null ? t.getMessage()
                : t.getClass()
                    .getSimpleName();
            return FilePickerResult.error(GuiText.tr("fentlib.gui.common.failed_message", message));
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static FilePickerResult validateSelection(FilePickerResult result, String[] extensions) {
        if (result == null) {
            return FilePickerResult.error(GuiText.tr("fentlib.gui.file_picker.unknown_error"));
        }

        if (result.getStatus() != FilePickerResult.Status.SELECTED) {
            return result;
        }

        File file = result.getFile();
        if (file == null) {
            return FilePickerResult.error(GuiText.tr("fentlib.gui.file_picker.no_file"));
        }
        if (!file.isFile()) {
            return FilePickerResult.error(GuiText.tr("fentlib.gui.file_picker.not_a_file"));
        }
        if (!file.canRead()) {
            return FilePickerResult.error(GuiText.tr("fentlib.gui.file_picker.not_readable"));
        }
        if (!hasSupportedExtension(file.getName(), extensions)) {
            return FilePickerResult
                .error(GuiText.tr("fentlib.gui.file_picker.unsupported_type", buildExtensionList(extensions)));
        }
        return result;
    }

    private static boolean hasSupportedExtension(String fileName, String[] extensions) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase();
        for (String ext : extensions) {
            if (lower.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static String buildAwtFilter(String[] extensions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < extensions.length; i++) {
            if (i > 0) sb.append(';');
            sb.append("*.")
                .append(extensions[i]);
        }
        return sb.toString();
    }

    private static String buildFilterLabel(String[] extensions) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < extensions.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("*.")
                .append(extensions[i]);
        }
        sb.append(")");
        return sb.toString();
    }

    private static String buildExtensionList(String[] extensions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < extensions.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(".")
                .append(extensions[i]);
        }
        return sb.toString();
    }

    private static String buildAppleScriptTypeList(String[] extensions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < extensions.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"")
                .append(extensions[i])
                .append("\"");
        }
        return sb.toString();
    }

    private static String defaultPickerPath(File initialDirectory) {
        if (initialDirectory == null) {
            return "";
        }
        File target = initialDirectory;
        while (target != null && !target.exists()) {
            target = target.getParentFile();
        }
        if (target == null) {
            return "";
        }
        if (target.isFile()) {
            File parent = target.getParentFile();
            return parent != null ? parent.getAbsolutePath() : "";
        }
        return target.getAbsolutePath();
    }

    private static String safeTitle(String title) {
        return title != null && !title.trim()
            .isEmpty() ? title.trim() : GuiText.tr("fentlib.gui.file_picker.select_file");
    }

    private static boolean isMacOs() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase()
            .contains("mac");
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase()
            .contains("win");
    }

    private static String escapeAppleScript(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private static String readAll(java.io.InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder out = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (out.length() > 0) {
                out.append('\n');
            }
            out.append(line);
        }
        return out.toString();
    }

    private static boolean openWithAwtDesktop(URI uri) {
        try {
            Class<?> desktopCls = Class.forName("java.awt.Desktop");
            Object desktop = desktopCls.getMethod("getDesktop")
                .invoke(null);
            desktopCls.getMethod("browse", URI.class)
                .invoke(desktop, uri);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean openFolderWithAwtDesktop(File folder) {
        try {
            Class<?> desktopCls = Class.forName("java.awt.Desktop");
            Boolean desktopSupported = (Boolean) desktopCls.getMethod("isDesktopSupported")
                .invoke(null);
            if (!desktopSupported) {
                return false;
            }

            Object desktop = desktopCls.getMethod("getDesktop")
                .invoke(null);
            Class<?> actionCls = Class.forName("java.awt.Desktop$Action");
            Object openAction = Enum.valueOf((Class<Enum>) actionCls.asSubclass(Enum.class), "OPEN");
            if ((Boolean) desktopCls.getMethod("isSupported", actionCls)
                .invoke(desktop, openAction)) {
                desktopCls.getMethod("open", File.class)
                    .invoke(desktop, folder);
                return true;
            }

            Object browseAction = Enum.valueOf((Class<Enum>) actionCls.asSubclass(Enum.class), "BROWSE");
            if ((Boolean) desktopCls.getMethod("isSupported", actionCls)
                .invoke(desktop, browseAction)) {
                desktopCls.getMethod("browse", URI.class)
                    .invoke(desktop, folder.toURI());
                return true;
            }
        } catch (UnsupportedOperationException ignored) {
            return false;
        } catch (Throwable throwable) {
            FentLib.LOG.debug("AWT Desktop could not open folder", throwable);
        }
        return false;
    }

    private static boolean openWithLwjgl3ifyDesktop(URI uri) {
        try {
            Class<?> desktopCls = Class.forName("me.eigenraven.lwjgl3ify.redirects.Desktop");
            Object desktop = desktopCls.getMethod("getDesktop")
                .invoke(null);
            desktopCls.getMethod("browse", URI.class)
                .invoke(desktop, uri);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean openWithSys(String className, String url) {
        try {
            Class<?> sysCls = Class.forName(className);
            Object result = sysCls.getMethod("openURL", String.class)
                .invoke(null, url);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
