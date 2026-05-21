package org.fentanylsolutions.fentlib.util;

import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;

import org.fentanylsolutions.fentlib.FentLib;

public class FileUtil {

    private static final String CURSEFORGE_LIMITED_MESSAGE = "For enhanced file-related functionality, download FentLib from a different source than CurseForge";

    private static void logCurseForgeLimited() {
        FentLib.LOG.fatal(CURSEFORGE_LIMITED_MESSAGE);
    }

    public static void writeFileBytes(File file, byte[] data) throws IOException {
        logCurseForgeLimited();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    public static void writeStringToFile(File file, String data) throws IOException {
        logCurseForgeLimited();
        FileWriter writer = new FileWriter(file);
        writer.write(data);
        writer.close();
    }

    public static File createFolderIfNotExists(File folder) {
        logCurseForgeLimited();
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
        logCurseForgeLimited();
        return createFolderIfNotExists(new File(path));
    }

    public static File createFolderIfNotExists(String path1, String path2) {
        logCurseForgeLimited();
        return createFolderIfNotExists(
            Paths.get(path1, path2)
                .toString());
    }

    public static File createFolderIfNotExists(File path1, String path2) {
        logCurseForgeLimited();
        return createFolderIfNotExists(
            Paths.get(path1.getPath(), path2)
                .toString());
    }

    public static void deleteDirectory(File directory) {
        logCurseForgeLimited();
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
        logCurseForgeLimited();
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(blob.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(blob.hashCode());
        }
    }

    public static File getMinecraftDir() {
        logCurseForgeLimited();
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
        logCurseForgeLimited();
        if (folder == null) {
            return false;
        }
        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }

        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(folder);
                return true;
            }
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(folder.toURI());
                return true;
            }
        } catch (Throwable throwable) {
            FentLib.LOG.error("Problem opening folder with AWT Desktop", throwable);
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
        logCurseForgeLimited();
        if (uri == null) return false;

        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return false;
            }
            desktop.browse(uri);
            return true;
        } catch (Throwable throwable) {
            FentLib.LOG.error("Problem opening URI with AWT Desktop", throwable);
            return false;
        }
    }

    public static FilePickerResult pickFile(String title, File initialDirectory, String... extensions) {
        logCurseForgeLimited();
        if (extensions == null || extensions.length == 0) {
            extensions = new String[] { "png", "gif" };
        }

        FilePickerResult awtResult = pickWithAwt(title, initialDirectory, extensions);
        if (awtResult.getStatus() != FilePickerResult.Status.UNAVAILABLE) {
            return validateSelection(awtResult, extensions);
        }

        return FilePickerResult.unavailable(GuiText.tr("fentlib.gui.file_picker.unavailable"));
    }

    public static File getDefaultFileSelectionDirectory() {
        logCurseForgeLimited();
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
        logCurseForgeLimited();
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

    private static String buildExtensionList(String[] extensions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < extensions.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(".")
                .append(extensions[i]);
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
}
