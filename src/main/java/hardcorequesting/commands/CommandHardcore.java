package hardcorequesting.commands;

import hardcorequesting.QuestingData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class CommandHardcore extends CommandBase {
    public CommandHardcore() {
        super("hardcore");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("disable")) {
            QuestingData.disableHardcore();
            sendChat(sender, "hqm.message.hardcoreDisabled");
        } else {
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

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        List<String> list = super.addTabCompletionOptions(sender, args);
        if (args[0].isEmpty() || args[0].startsWith("e"))
            list.add("enable");
        if (args[0].isEmpty() || args[0].startsWith("d"))
            list.add("disable");
        return list;
    }
}
