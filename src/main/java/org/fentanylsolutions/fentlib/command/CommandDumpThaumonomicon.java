package org.fentanylsolutions.fentlib.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.packet.PacketHandler;
import org.fentanylsolutions.fentlib.packet.packets.ThaumcraftDumpPacket;
import org.fentanylsolutions.fentlib.util.MiscUtil;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class CommandDumpThaumonomicon implements ICommand {

    private final List<String> aliases;

    public CommandDumpThaumonomicon() {
        aliases = new ArrayList<>();
    }

    int getRequiredPermissionLevel() {
        return MiscUtil.PermissionLevel.OP.getLevel();
    }

    @Override
    public String getCommandName() {
        return "dump_thaumonomicon";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " [message]";
    }

    @Override
    public List<String> getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        FentLib.LOG.info("Dumping Thaumonomicon command");
        if (!Loader.isModLoaded("Thaumcraft")) {
            sender.addChatMessage(new ChatComponentText("Please install Thaumcraft to use this command"));
            return;
        }

        String message = "";
        if (args.length > 0) {
            message = String.join(" ", args);
        }

        IMessage msg = new ThaumcraftDumpPacket.SimpleMessage(message);
        PacketHandler.net.sendTo(msg, (EntityPlayerMP) sender);
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
        return getCommandName().compareTo(((ICommand) o).getCommandName());
    }
}
