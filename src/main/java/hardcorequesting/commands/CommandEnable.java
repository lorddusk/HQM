package hardcorequesting.commands;

import hardcorequesting.QuestingData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandEnable extends CommandBase {
    public CommandEnable() {
        super("enable");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        QuestingData.disableVanillaHardcore(sender);
        if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled())
            sendChat(sender, "hqm.message.vanillaHardcoreOn");
        else
            sendChat(sender, QuestingData.isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore");
        sendChat(sender, QuestingData.isQuestActive() ? "hqm.message.questAlreadyActivated" : "hqm.message.questActivated");
        QuestingData.activateHardcore();
        QuestingData.activateQuest();
        if (QuestingData.isHardcoreActive() && sender instanceof EntityPlayer)
            currentLives((EntityPlayer) sender);
    }
}
