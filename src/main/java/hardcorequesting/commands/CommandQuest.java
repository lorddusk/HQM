package hardcorequesting.commands;

import hardcorequesting.QuestingData;
import net.minecraft.command.ICommandSender;

public class CommandQuest extends CommandBase {
    public CommandQuest() {
        super("quest");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        sendChat(sender, QuestingData.isQuestActive() ? "hqm.message.questAlreadyActivated" : "hqm.message.questActivated");
        QuestingData.activateQuest();
    }
}
