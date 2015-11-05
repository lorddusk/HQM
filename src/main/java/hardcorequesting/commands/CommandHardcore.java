package hardcorequesting.commands;

import hardcorequesting.QuestingData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandHardcore extends CommandBase
{
    public CommandHardcore()
    {
        super("hardcore");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments)
    {
        QuestingData.disableVanillaHardcore(sender);
        if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled())
            sendChat(sender, "hqm.message.vanillaHardcoreOn");
        else
            sendChat(sender, QuestingData.isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore");
        QuestingData.activateHardcore();
        if (sender instanceof EntityPlayer)
            currentLives((EntityPlayer) sender);
    }
}
