package hardcorequesting.commands;

import hardcorequesting.Lang;
import hardcorequesting.Translator;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

public class CommandHelp extends CommandBase {

    public static final String PREFIX = "\u00A7";//§
    public static final String YELLOW = PREFIX + "e";
    public static final String WHITE = PREFIX + "f";

    public CommandHelp() {
        super("help");
        permissionLevel = -1;
    }

    @Override
    public int getPermissionLevel() {
        return -1;
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) throws CommandException {
        switch (arguments.length) {
            case 0:
                StringBuilder output = new StringBuilder(Translator.translate(Lang.HELP_START) + " ");
                List<String> commands = new ArrayList<>();
                for (ISubCommand command : CommandHandler.commands.values()) {
                    if (command.isVisible(sender)) commands.add(command.getCommandName());
                }

                for (int i = 0; i < commands.size() - 1; i++) {
                    output.append("/").append(CommandHandler.instance.getCommandName()).append(" ").append(YELLOW).append(commands.get(i)).append(WHITE).append(", ");
                }
                output.delete(output.length() - 2, output.length());
                output.append(" and /").append(CommandHandler.instance.getCommandName()).append(" ").append(YELLOW).append(commands.get(commands.size() - 1)).append(WHITE).append(".");
                sender.addChatMessage(new ChatComponentText(output.toString()));
                break;
            case 1:
                String commandName = arguments[0];

                if (!CommandHandler.commandExists(commandName)) {
                    throw new CommandNotFoundException(Lang.NOT_FOUND);
                }
                ISubCommand command = CommandHandler.getCommand(commandName);
                if (command.isVisible(sender)) {
                    for (int i : command.getSyntaxOptions(sender))
                        sender.addChatMessage(new ChatComponentText(YELLOW + Translator.translate(Lang.COMMAND_PREFIX + commandName + Lang.SYNTAX_SUFFIX + i)
                                + WHITE + " - " + Translator.translate(Lang.COMMAND_PREFIX + commandName + Lang.INFO_SUFFIX + i)));
                } else {
                    throw new CommandException(Lang.NO_PERMISSION);
                }
                break;
            default:
                throw new WrongUsageException(Lang.COMMAND_PREFIX + getCommandName() + Lang.SYNTAX_SUFFIX);
        }
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return CommandHandler.instance.addTabCompletionOptions(sender, new String[]{args[1]},new BlockPos(0,0,0));
        }
        return null;
    }

    @Override
    public boolean isVisible(ICommandSender sender) {
        return true;
    }

}
