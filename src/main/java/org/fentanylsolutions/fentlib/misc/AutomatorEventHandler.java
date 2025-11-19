package org.fentanylsolutions.fentlib.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiOpenEvent;

import org.fentanylsolutions.fentlib.FentLib;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AutomatorEventHandler {

    private static boolean clientTriggered = false;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiOpen(GuiOpenEvent event) {
        if ((event.gui instanceof GuiMainMenu) && !clientTriggered) {
            clientTriggered = true;
            FentLib.LOG.warn("Minecraft Loaded");
            WorldSettings settings = new WorldSettings(
                System.currentTimeMillis(),
                WorldSettings.GameType.CREATIVE,
                true, // structures
                false, // hardcore
                WorldType.FLAT);
            settings.enableCommands();

            Minecraft.getMinecraft().loadingScreen = new net.minecraft.client.LoadingScreenRenderer(
                Minecraft.getMinecraft());
            Minecraft.getMinecraft()
                .launchIntegratedServer("AutoWorld", "AutoWorld", settings);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        FentLib.debug("Player joined: " + event.player.getCommandSenderName());

        String commands = System.getenv("AUTOSTART_COMMANDS");
        if (commands != null && !commands.isEmpty()) {
            String[] cmdArray = commands.split(";");

            new Thread(() -> {
                try {
                    Thread.sleep(3000);

                    for (String cmd : cmdArray) {
                        cmd = cmd.trim();
                        if (!cmd.isEmpty()) {
                            FentLib.LOG.info("Running command /" + cmd);
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/" + cmd);
                            Thread.sleep(500);
                        }
                    }
                } catch (InterruptedException ex) {
                    FentLib.LOG.error(ex);
                }
            }).start();
        }
    }

    private static final String[] LOG_FILES = { "logs/fml-client-latest.log", "logs/latest.log" };

    private static final long POLL_INTERVAL_MS = 1000; // Check files every second
    private static final String OUTPUT_FILE = "intercepted.txt";

    private static BufferedWriter outputWriter = null;
    private static Pattern pattern = null;

    /**
     * Starts tailing log files in background threads.
     * Monitors multiple log files and matches against AUTOSTOP environment variable.
     * Call this from your mod's post-init to start tailing without blocking.
     */
    public static void tailLog() {
        String wildcardPattern = System.getenv("AUTOSTOP");

        if (wildcardPattern == null || wildcardPattern.isEmpty()) {
            FentLib.debug("AUTOSTOP environment variable not set. Log tailer disabled.");
            return;
        }
        FentLib.LOG.info("AUTOSTOP environment variable set. Enabling log tailer.");

        File file = new File(OUTPUT_FILE);
        file.delete();

        pattern = compileWildcardPattern(wildcardPattern);

        FentLib.LOG.info("Starting log tailer with pattern: {}", wildcardPattern);
        FentLib.LOG.info("Monitoring files:");
        for (String logFile : LOG_FILES) {
            FentLib.LOG.info("  - " + logFile);
        }
        if (FentLib.isDebugMode()) {
            FentLib.LOG.info("Writing to: " + OUTPUT_FILE);
        }
        FentLib.LOG.info("---");

        // Start a thread for each log file
        for (int i = 0; i < LOG_FILES.length; i++) {
            final String logFile = LOG_FILES[i];
            final int fileIndex = i;

            Thread tailThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    tailLogFile(logFile, fileIndex);
                }
            }, "LogTailer-" + fileIndex);

            tailThread.setDaemon(true);
            tailThread.start();
        }
    }

    private static void tailLogFile(String logFilePath, int fileIndex) {
        RandomAccessFile file = null;

        try {
            file = new RandomAccessFile(logFilePath, "r");

            long filePointer = file.length();

            FentLib.LOG.info("[Tailer-" + fileIndex + "] Started monitoring: " + logFilePath);

            while (true) {
                try {
                    long fileLength = file.length();

                    if (fileLength > filePointer) {
                        file.seek(filePointer);
                        String line;

                        while ((line = file.readLine()) != null) {
                            if (FentLib.isDebugMode()) {
                                writeToOutput(logFilePath, line);
                            }

                            // Check if line matches pattern
                            if (pattern != null && pattern.matcher(line)
                                .matches()) {
                                FentLib.LOG.info("[MATCH FOUND in " + logFilePath + "] " + line);
                                if (FentLib.isDebugMode()) {
                                    closeOutput();
                                }

                                // Shutdown Minecraft
                                net.minecraft.client.Minecraft.getMinecraft()
                                    .shutdown();
                                return;
                            }
                        }

                        filePointer = file.getFilePointer();
                    } else if (fileLength < filePointer) {
                        // File was truncated or rotated
                        filePointer = 0;
                        file.seek(0);
                    }

                    Thread.sleep(POLL_INTERVAL_MS);

                } catch (IOException e) {
                    Thread.sleep(POLL_INTERVAL_MS);
                }
            }

        } catch (IOException e) {
            FentLib.LOG.error("[Tailer-{}] Error reading {}: {}", fileIndex, logFilePath, e.getMessage());
        } catch (InterruptedException e) {
            FentLib.LOG.error("[Tailer-{}] Log tailing interrupted for {}", fileIndex, logFilePath);
            Thread.currentThread()
                .interrupt();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Thread-safe write to output file.
     */
    private static synchronized void writeToOutput(String source, String line) {
        try {
            if (outputWriter == null) {
                outputWriter = new BufferedWriter(new FileWriter(OUTPUT_FILE, true));
            }

            outputWriter.write("[" + source + "] " + line);
            outputWriter.newLine();
            outputWriter.flush();

        } catch (IOException e) {
            FentLib.LOG.error("Error writing to output file: {}", e.getMessage());
        }
    }

    /**
     * Close output writer.
     */
    private static synchronized void closeOutput() {
        if (outputWriter != null) {
            try {
                outputWriter.flush();
                outputWriter.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    /**
     * Converts wildcard pattern to regex pattern.
     * * matches any sequence of characters
     */
    private static Pattern compileWildcardPattern(String wildcard) {
        String regex = wildcard.replace("\\", "\\\\")
            .replace(".", "\\.")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("+", "\\+")
            .replace("?", "\\?")
            .replace("^", "\\^")
            .replace("$", "\\$")
            .replace("|", "\\|")
            .replace("*", ".*"); // Convert * to .*

        return Pattern.compile(regex);
    }
}
