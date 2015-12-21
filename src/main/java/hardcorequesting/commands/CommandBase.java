package hardcorequesting.commands;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.QuestingData;
import hardcorequesting.Translator;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.parsing.BagAdapter;
import hardcorequesting.parsing.QuestAdapter;
import hardcorequesting.parsing.ReputationAdapter;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.reputation.Reputation;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandBase implements ISubCommand {
    protected static Gson GSON;

    static {
        GSON = new GsonBuilder().registerTypeAdapter(Reputation.getReputationList().getClass(), ReputationAdapter.REPUTATION_ADAPTER).registerTypeAdapter(QuestSet.class, QuestAdapter.QUEST_SET_ADAPTER).registerTypeAdapter(GroupTier.class, BagAdapter.GROUP_TIER_ADAPTER)
                .setPrettyPrinting().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES).create();
    }

    private String name;
    private List<String> subCommands = new ArrayList<>();
    protected int permissionLevel = 3;

    public CommandBase(String name, String... subCommands) {
        this.name = name;
        this.subCommands = Arrays.asList(subCommands);
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public int getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0])) {
                    results.add(subCommand);
                }
            }
        }
        return results;
    }

    @Override
    public boolean isVisible(ICommandSender sender) {
        return getPermissionLevel() <= 0 || isPlayerOp(sender);
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender) {
        return new int[]{0};
    }

    public static File getFile(String name) {
        return new File(HardcoreQuesting.configDir + File.separator + "QuestFiles" + File.separator, name + ".json");
    }

    public String getCombinedArgs(String[] args) {
        String text = "";
        for (String arg : args) {
            text += arg + " ";
        }
        return text.substring(0, text.length() - 1);
    }

    protected void sendChat(ICommandSender sender, String key, Object... args) {
        sendChat(sender, false, key, args);
    }

    protected void sendChat(ICommandSender sender, boolean plural, String key, Object... args) {
        sender.addChatMessage(new ChatComponentText(Translator.translate(plural, key, args)));
    }

    protected boolean isPlayerOp(ICommandSender sender) {
        return CommandHandler.isOwnerOrOp(sender);
    }

    protected void currentLives(EntityPlayer player) {
        sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live(s) left.");
    }
}
