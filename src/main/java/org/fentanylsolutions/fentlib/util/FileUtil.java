package org.fentanylsolutions.fentlib.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

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

        boolean awtDesktopFailed = false;

        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0])
                .invoke(null);
            oclass.getMethod("browse", new Class[] { URI.class })
                .invoke(object, folder.toURI());
            return true;
        } catch (Throwable throwable) {
            FentLib.LOG.error("Problem opening folder", throwable);
            awtDesktopFailed = true;
        }

        if (awtDesktopFailed) {
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
        }

        return false;
    }
}
