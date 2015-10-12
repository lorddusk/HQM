package hardcorequesting.commands;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.quests.QuestSet;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by lang2 on 10/12/2015.
 */
public class CommandSave {
    public static void save(ICommandSender sender, QuestSet set, String name) {
        try
        {
            File file = CommandHandler.getFile(name);
            if (!file.exists()) file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            CommandHandler.GSON.toJson(set, fileWriter);
            fileWriter.close();
            sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted("Quests saved to: %s", file.getPath().substring(HardcoreQuesting.configDir.getParentFile().getParent().length()))));
        } catch (IOException e)
        {
            throw new CommandException("Saving %s failed", name);
        }
    }
}
