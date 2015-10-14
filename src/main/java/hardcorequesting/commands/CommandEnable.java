package hardcorequesting.commands;

import hardcorequesting.QuestingData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandEnable extends CommandBase
{
    public CommandEnable()
    {
        super("enable");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments)
    {
        System.out.println("ENABLE");
            QuestingData.disableHardcore(sender);

            if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled() && !QuestingData.isQuestActive()) {
                sendChat(sender, "Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.");
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateQuest();
            } else if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled() && QuestingData.isQuestActive()) {
                sendChat(sender, "Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.");
                sendChat(sender, "Questing mode is already activated.");
            } else if (!QuestingData.isHardcoreActive() && !QuestingData.isQuestActive()) {
                sendChat(sender, "Hardcore Mode has been activated. Enjoy!");
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateHardcore();
                QuestingData.activateQuest();
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer) sender);
                }
            } else if (!QuestingData.isHardcoreActive() && QuestingData.isQuestActive()) {
                sendChat(sender, "Hardcore Mode has been activated. Enjoy!");
                QuestingData.activateHardcore();
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer) sender);
                }
                sendChat(sender, "Questing mode is already activated.");
            } else if (QuestingData.isHardcoreActive() && !QuestingData.isQuestActive()) {
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateQuest();
                sendChat(sender, "Hardcore Mode is already activated.");
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer) sender);
                }
            } else
            {
                sendChat(sender, "Hardcore Mode is already activated.");
                if (sender instanceof EntityPlayer)
                {
                    currentLives((EntityPlayer) sender);
                }
                sendChat(sender, "Questing mode is already activated.");
            }
    }
}
