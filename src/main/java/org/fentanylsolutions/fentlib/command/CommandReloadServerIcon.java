package org.fentanylsolutions.fentlib.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixins.early.minecraft.AccessorMinecraftServer;
import org.fentanylsolutions.fentlib.util.MiscUtil;

public class CommandReloadServerIcon implements ICommand {

    private final List<String> aliases;

    public CommandReloadServerIcon() {
        aliases = new ArrayList<>();
    }

    int getRequiredPermissionLevel() {
        return MiscUtil.PermissionLevel.OP.getLevel();
    }

    @Override
    public String getCommandName() {
        return "reload_icon";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        FentLib.LOG.info("[/{}] Reloading server icon", getCommandName());
        ((AccessorMinecraftServer) MinecraftServer.getServer())
            .invokeFunc_147138_a(((AccessorMinecraftServer) MinecraftServer.getServer()).getField_147147_p());
        FentLib.LOG.info("[/{}] Reloaded server icon", getCommandName());
        // TODO: localize, if possible. might need an additional feature e.g. localize all strings surrounded with %LOC%
        sender.addChatMessage(new ChatComponentText("Reloaded server icon"));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(getRequiredPermissionLevel(), getCommandName());
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
