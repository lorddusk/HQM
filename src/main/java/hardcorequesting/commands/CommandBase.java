package hardcorequesting.commands;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.QuestingData;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.parsing.BagAdapter;
import hardcorequesting.parsing.QuestAdapter;
import hardcorequesting.quests.QuestSet;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import scala.actors.threadpool.Arrays;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandBase implements ISubCommand
{
    protected static Gson GSON;

    static
    {
        GSON = new GsonBuilder().registerTypeAdapter(QuestSet.class, QuestAdapter.QUEST_SET_ADAPTER).registerTypeAdapter(GroupTier.class, BagAdapter.GROUP_TIER_ADAPTER)
                .setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES).create();
    }

    private String name;
    private List<String> subCommands = new ArrayList<>();
    protected int permissionLevel = 3;

    public CommandBase(String name, String... subCommands)
    {
        this.name = name;
        this.subCommands = Arrays.asList(subCommands);
    }

    @Override
    public String getCommandName()
    {
        return name;
    }

    @Override
    public int getPermissionLevel()
    {
        return permissionLevel;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            List<String> results = new ArrayList<>();
            for (String subCommand : subCommands)
            {
                if (subCommand.startsWith(args[0]))
                {
                    results.add(subCommand);
                }
            }
            return results;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isVisible(ICommandSender sender)
    {
        return getPermissionLevel() <= 0 || isPlayerOp(sender);
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender)
    {
        return new int[]{0};
    }

    public static File getFile(String name)
    {
        return new File(HardcoreQuesting.configDir, name + ".json");
    }

    public String getCombinedArgs(String[] args)
    {
        String text = "";
        for (String arg : args)
        {
            text += arg + " ";
        }
        return text.substring(0, text.length()-1);
    }

    protected void sendChat(ICommandSender sender, String string) {
        sender.addChatMessage(new ChatComponentText(string));
    }

    protected boolean isPlayerOp(ICommandSender sender)
    {
        return CommandHandler.isOwnerOrOp(sender);
    }

    protected void currentLives(EntityPlayer player) {
        sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live(s) left.");
    }
}
