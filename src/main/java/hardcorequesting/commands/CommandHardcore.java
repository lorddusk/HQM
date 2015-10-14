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
        QuestingData.disableHardcore(sender);
        if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
            sendChat(sender, "Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.");
        } else if (!QuestingData.isHardcoreActive()) {
            sendChat(sender, "Hardcore Mode has been activated. Enjoy!");
            QuestingData.activateHardcore();
            if (sender instanceof EntityPlayer) {
                currentLives((EntityPlayer) sender);
            }
        } else {
            sendChat(sender, "Hardcore Mode is already activated.");
            if (sender instanceof EntityPlayer) {
                currentLives((EntityPlayer) sender);
            }
        }
    }
}
