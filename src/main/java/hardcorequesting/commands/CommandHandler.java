package hardcorequesting.commands;

import com.mojang.authlib.GameProfile;
import hardcorequesting.Lang;
import net.minecraft.command.*;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.util.*;

public class CommandHandler extends CommandBase {
    public static Map<String, ISubCommand> commands = new LinkedHashMap<String, ISubCommand>();
    public static CommandHandler instance = new CommandHandler();

    static {
        register(new CommandHelp());
        register(new CommandVersion());
        register(new CommandQuest());
        register(new CommandHardcore());
        register(new CommandLives());
        register(new CommandEdit());
        register(new CommandEnable());
        register(new CommandSave());
        register(new CommandLoad());
    }

    public static void register(ISubCommand command) {
        commands.put(command.getCommandName(), command);
    }

    public static boolean commandExists(String name) {
        return commands.containsKey(name);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public String getCommandName() {
        return "hqm";
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            String subCommand = args[0];
            List result = new ArrayList();
            for (ISubCommand command : commands.values()) {
                if (command.isVisible(sender) && command.getCommandName().startsWith(subCommand))
                    result.add(command.getCommandName());
            }
            return result;
        } else if (commands.containsKey(args[0]) && commands.get(args[0]).isVisible(sender)) {
            return commands.get(args[0]).addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
        }
        return null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " help";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            args = new String[]{"help"};
        }
        ISubCommand command = commands.get(args[0]);
        if (command != null) {
            if (command.isVisible(sender) && (sender.canCommandSenderUseCommand(command.getPermissionLevel(), getCommandName() + " " + command.getCommandName())
                    || (sender instanceof EntityPlayerMP && command.getPermissionLevel() <= 0))) {
                command.handleCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
            throw new CommandException(Lang.NO_PERMISSION);
        }
        throw new CommandNotFoundException(Lang.NOT_FOUND);
    }

    public static boolean isOwnerOrOp(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            GameProfile username = player.getGameProfile();
            return isCommandsAllowedOrOwner(username);
        } else
            return true;
    }


    public static boolean isCommandsAllowedOrOwner(GameProfile username) {
        return MinecraftServer.getServer().getConfigurationManager().canSendCommands(username) || MinecraftServer.getServer().isSinglePlayer() && MinecraftServer.getServer().getServerOwner().equals(username);
    }

    public static ISubCommand getCommand(String commandName) {
        return commands.get(commandName);
    }
}
