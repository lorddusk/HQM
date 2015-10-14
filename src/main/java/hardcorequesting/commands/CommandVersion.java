package hardcorequesting.commands;

import hardcorequesting.ModInformation;
import net.minecraft.command.ICommandSender;

public class CommandVersion extends CommandBase
{
    public CommandVersion()
    {
        super("version");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments)
    {
        String colour = "\u00A7a";
        sendChat(sender, String.format(colour + "Hardcore Questing Mode - Version : %s", ModInformation.VERSION));
    }
}
