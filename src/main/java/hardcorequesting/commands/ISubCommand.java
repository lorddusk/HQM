package hardcorequesting.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

import java.util.List;

public interface ISubCommand {
    int getPermissionLevel();

    String getCommandName();

    void handleCommand(ICommandSender sender, String[] arguments) throws CommandException;

    List<String> addTabCompletionOptions(ICommandSender sender, String[] args);

    boolean isVisible(ICommandSender sender);

    int[] getSyntaxOptions(ICommandSender sender);
}
