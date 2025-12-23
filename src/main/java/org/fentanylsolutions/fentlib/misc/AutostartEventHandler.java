package org.fentanylsolutions.fentlib.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import org.fentanylsolutions.fentlib.FentLib;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AutostartEventHandler {

    private boolean commandsExecuted = false;
    private int clientTickDelay = 0;
    private static final int CLIENT_DELAY_TICKS = 100; // 5 seconds delay

    private final boolean isClientAutostart;
    private final boolean isServerAutostart;

    public AutostartEventHandler() {
        String clientAutostart = System.getenv("CLIENT_AUTOSTART");
        String serverAutostart = System.getenv("SERVER_AUTOSTART");

        this.isClientAutostart = clientAutostart != null && !clientAutostart.isEmpty();
        this.isServerAutostart = serverAutostart != null && !serverAutostart.isEmpty();

        if (this.isClientAutostart || this.isServerAutostart) {
            FentLib.LOG.info(
                "AutostartEventHandler registered (CLIENT: " + this.isClientAutostart
                    + ", SERVER: "
                    + this.isServerAutostart
                    + ")");
            FMLCommonHandler.instance()
                .bus()
                .register(this);
        }
    }

    public static AutostartEventHandler createIfNeeded() {
        String clientAutostart = System.getenv("CLIENT_AUTOSTART");
        String serverAutostart = System.getenv("SERVER_AUTOSTART");

        if ((clientAutostart != null && !clientAutostart.isEmpty())
            || (serverAutostart != null && !serverAutostart.isEmpty())) {
            return new AutostartEventHandler();
        }

        return null;
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        if (isServerAutostart) {
            FentLib.LOG.info("SERVER_AUTOSTART detected, executing commands on server...");
            executeServerCommands();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isClientAutostart || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!commandsExecuted) {
            Minecraft mc = Minecraft.getMinecraft();

            // Wait for the game to be fully loaded and player to exist
            if (mc.theWorld != null && mc.thePlayer != null) {
                clientTickDelay++;

                if (clientTickDelay >= CLIENT_DELAY_TICKS) {
                    FentLib.LOG.info("CLIENT_AUTOSTART detected, executing commands on client...");
                    executeClientCommands();
                    commandsExecuted = true;
                }
            }
        }
    }

    private void executeServerCommands() {
        String commands = System.getenv("AUTOSTART_COMMANDS");

        if (commands == null || commands.isEmpty()) {
            FentLib.LOG.warn("AUTOSTART_COMMANDS not set or empty");
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            FentLib.LOG.error("MinecraftServer is null, cannot execute commands");
            return;
        }

        // Split commands by semicolon
        String[] commandList = commands.split(";");

        for (String command : commandList) {
            command = command.trim();
            if (!command.isEmpty()) {
                FentLib.LOG.info("Executing server command: " + command);
                server.getCommandManager()
                    .executeCommand(server, command);
            }
        }

        FentLib.LOG.info("Finished executing " + commandList.length + " server commands");
    }

    @SideOnly(Side.CLIENT)
    private void executeClientCommands() {
        String commands = System.getenv("AUTOSTART_COMMANDS");

        if (commands == null || commands.isEmpty()) {
            FentLib.LOG.warn("AUTOSTART_COMMANDS not set or empty");
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        // Check if player has OP permissions (in singleplayer, they should)
        if (mc.isSingleplayer() && mc.getIntegratedServer() != null) {
            // Give OP to the player
            String playerName = mc.thePlayer.getCommandSenderName();
            mc.getIntegratedServer()
                .getCommandManager()
                .executeCommand(mc.getIntegratedServer(), "op " + playerName);
            FentLib.LOG.info("Granted OP to player: " + playerName);
        }

        // Split commands by semicolon
        String[] commandList = commands.split(";");

        for (String command : commandList) {
            command = command.trim();
            if (!command.isEmpty()) {
                FentLib.LOG.info("Executing client command: " + command);

                // Execute command as if player typed it
                if (mc.isSingleplayer() && mc.getIntegratedServer() != null) {
                    // Execute on integrated server
                    mc.getIntegratedServer()
                        .getCommandManager()
                        .executeCommand(mc.thePlayer, command);
                } else {
                    // Send as chat message (for multiplayer)
                    mc.thePlayer.sendChatMessage("/" + command);
                }
            }
        }

        FentLib.LOG.info("Finished executing " + commandList.length + " client commands");
    }
}
