package hardcorequesting.commands;

import com.google.gson.stream.JsonReader;
import hardcorequesting.quests.QuestSet;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.FileReader;

/**
 * Created by lang2 on 10/12/2015.
 */
public class CommandLoad {


    public static void load(ICommandSender sender, File file) {
        if (!file.exists())
        {
            throw new CommandException("File not found");
        }
        try
        {
            String extension = "";
            int i = file.toString().lastIndexOf('.');
            if(i > 0){
                extension = file.toString().substring(i+1);
                if(extension.equals("json")){
                    JsonReader reader = new JsonReader(new FileReader(file));
                    QuestSet set = CommandHandler.GSON.fromJson(reader, QuestSet.class);
                    reader.close();
                    if (set != null)
                    {
                        sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted("Loaded: %s", set.getName())));
                    } else
                    {
                        throw new CommandException("Loading failed.");
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new CommandException("Loading failed.");
        }
    }

}
