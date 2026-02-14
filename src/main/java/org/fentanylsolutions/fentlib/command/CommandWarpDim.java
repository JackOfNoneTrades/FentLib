package org.fentanylsolutions.fentlib.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class CommandWarpDim extends CommandBase {

    @Override
    public String getCommandName() {
        return "warpdim";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " [dimensionId]";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            if (player.capabilities.isCreativeMode) {
                return true;
            }
        }
        return super.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        int targetDim = -1;
        if (args.length > 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        if (args.length == 1) {
            targetDim = parseInt(sender, args[0]);
        }

        MinecraftServer server = MinecraftServer.getServer();
        WorldServer targetWorld = server.worldServerForDimension(targetDim);
        if (targetWorld == null) {
            throw new WrongUsageException("Unknown dimension id: " + targetDim);
        }

        int targetX = MathHelperClamp.floor(player.posX);
        int targetY = MathHelperClamp.floor(player.posY);
        int targetZ = MathHelperClamp.floor(player.posZ);

        if (player.dimension == -1 && targetDim == 0) {
            targetX *= 8;
            targetZ *= 8;
        } else if (player.dimension == 0 && targetDim == -1) {
            targetX /= 8;
            targetZ /= 8;
        }

        if (targetDim == 1) {
            targetX = 7;
            targetY = 100;
            targetZ = 33;
        }

        targetX = MathHelperClamp.clamp(targetX, -29999984, 29999984);
        targetZ = MathHelperClamp.clamp(targetZ, -29999984, 29999984);
        targetY = MathHelperClamp.clamp(targetY, 5, targetWorld.getActualHeight() - 5);

        server.getConfigurationManager()
            .transferPlayerToDimension(
                player,
                targetDim,
                new FixedPositionTeleporter(targetWorld, targetX, targetY, targetZ));

        player.playerNetServerHandler
            .setPlayerLocation(targetX + 0.5, targetY, targetZ + 0.5, player.rotationYaw, player.rotationPitch);
        player.addChatMessage(
            new ChatComponentText("Teleported to dim " + targetDim + " at " + targetX + " " + targetY + " " + targetZ));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "-1", "0", "1");
        }
        return null;
    }

    private static class FixedPositionTeleporter extends Teleporter {

        private final int x;
        private final int y;
        private final int z;

        private FixedPositionTeleporter(WorldServer world, int x, int y, int z) {
            super(world);
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void placeInPortal(net.minecraft.entity.Entity entity, double x, double y, double z, float yaw) {
            entity.setLocationAndAngles(this.x + 0.5, this.y, this.z + 0.5, entity.rotationYaw, entity.rotationPitch);
        }

        @Override
        public boolean placeInExistingPortal(net.minecraft.entity.Entity entity, double x, double y, double z,
            float yaw) {
            return false;
        }

        @Override
        public boolean makePortal(net.minecraft.entity.Entity entity) {
            return false;
        }

        @Override
        public void removeStalePortalLocations(long time) {}
    }

    private static class MathHelperClamp {

        private static int floor(double value) {
            int i = (int) value;
            return value < i ? i - 1 : i;
        }

        private static int clamp(int value, int min, int max) {
            return value < min ? min : Math.min(value, max);
        }
    }
}
