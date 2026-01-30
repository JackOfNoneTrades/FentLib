package org.fentanylsolutions.fentlib.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import org.fentanylsolutions.fentlib.FentLib;

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
            return new File(
                MinecraftServer.getServer()
                    .getFolderName());
        } else {
            return Minecraft.getMinecraft().mcDataDir;
        }
    }
}
