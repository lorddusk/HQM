package hardcorequesting.commands;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandHandler extends CommandBase {
    
    public static Map<String, ISubCommand> commands = new LinkedHashMap<>();
    public static CommandHandler instance = new CommandHandler();
    
    static {
        register(new CommandHelp());
        register(new CommandVersion());
        register(new CommandQuest());
        register(new CommandHardcore());
        register(new CommandLives());
        register(new CommandOpBook());
        register(new CommandEditMode());
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
    
    public static boolean isOwnerOrOp(PlayerEntity sender) {
        GameProfile username = sender.getGameProfile();
        return isCommandsAllowedOrOwner(sender, username);
    }
    
    public static boolean isCommandsAllowedOrOwner(PlayerEntity sender, GameProfile username) {
        return sender.getServer().getPlayerManager().canSendCommands(username) || (sender.getServer().isSinglePlayer() && sender.getServer().getServerOwner().equals(username.getName()));
    }
    
    public static ISubCommand getCommand(String commandName) {
        return commands.get(commandName);
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            String subCommand = args[0];
            List<String> result = new ArrayList<>();
            for (ISubCommand command : commands.values()) {
                if (command.isVisible(sender) && command.getCommandName().startsWith(subCommand))
                    result.add(command.getCommandName());
            }
            return result;
        } else if (commands.containsKey(args[0]) && commands.get(args[0]).isVisible(sender)) {
            return commands.get(args[0]).addTabCompletionOptions(sender, Arrays.copyOfRange(args, 1, args.length));
        }
        return new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "hqm";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + getName() + " help";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            args = new String[]{"help"};
        }
        ISubCommand command = commands.get(args[0]);
        if (command != null) {
            if (command.isVisible(sender) && (sender.canUseCommand(command.getPermissionLevel(), getName() + " " + command.getCommandName())
                                              || (sender instanceof ServerPlayerEntity && command.getPermissionLevel() <= 0))) {
                command.handleCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return;
            }
            throw new CommandException(CommandStrings.NO_PERMISSION);
        }
        throw new CommandNotFoundException(CommandStrings.NOT_FOUND);
    }
}
