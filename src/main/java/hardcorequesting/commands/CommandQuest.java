package hardcorequesting.commands;

import hardcorequesting.QuestingData;
import net.minecraft.command.ICommandSender;

public class CommandQuest extends CommandBase
{
    public CommandQuest()
    {
        super("quest");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments)
    {
        if (!QuestingData.isQuestActive()) {
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateQuest();
            } else {
                sendChat(sender, "Questing mode is already activated.");
            }
    }
}
