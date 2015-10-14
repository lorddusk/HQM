package hardcorequesting.commands;

import com.google.gson.stream.JsonReader;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.Lang;
import hardcorequesting.QuestingData;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestSet;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.regex.Pattern;

public class CommandLoad extends CommandBase
{
    public CommandLoad()
    {
        super("load", "all");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments)
    {
        if (arguments.length == 1 && arguments[0].equals("all"))
        {
            for (File file : getPossibleFiles())
            {
                load(sender, file);
            }
        } else if (arguments.length > 0)
        {
            String file = "";
            for (String arg : arguments)
            {
                file += arg + " ";
            }
            file = file.substring(0, file.length() - 1);
            load(sender, getFile(file));
        }
        Quest.FILE_HELPER.saveData(null);
    }

    private File[] getPossibleFiles()
    {
        return HardcoreQuesting.configDir.listFiles();
    }

    private void load(ICommandSender sender, File file)
    {
        if (!file.exists())
        {
            throw new CommandException(Lang.FILE_NOT_FOUND);
        }
        try
        {
            if (sender instanceof EntityPlayer)
                HardcoreQuesting.setPlayer((EntityPlayer) sender);
            JsonReader reader = new JsonReader(new FileReader(file));
            QuestSet set = GSON.fromJson(reader, QuestSet.class);
            reader.close();
            if (set != null)
            {
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(Lang.LOAD_SUCCESS, set.getName())));
            } else
            {
                throw new CommandException(Lang.LOAD_FAILED);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new CommandException(Lang.LOAD_FAILED);
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        String text = getCombinedArgs(args);
        Pattern pattern = Pattern.compile("^" + Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        List<String> results = super.addTabCompletionOptions(sender, args);
        for (File file : getPossibleFiles())
        {
            if (pattern.matcher(file.getName()).find()) results.add(file.getName().replace(".json", ""));
        }
        return results;
    }

    @Override
    public boolean isVisible(ICommandSender sender)
    {
        return Quest.isEditing && QuestingData.hasData(sender.getCommandSenderName()) && super.isVisible(sender);
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender)
    {
        return new int[]{0, 1};
    }
}
