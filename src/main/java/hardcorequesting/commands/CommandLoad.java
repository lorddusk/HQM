package hardcorequesting.commands;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.Lang;
import hardcorequesting.QuestingData;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.parsing.QuestAdapter;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.reputation.Reputation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class CommandLoad extends CommandBase {
    private static final Pattern JSON = Pattern.compile(".*\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BAGS = Pattern.compile(".*bags\\.json$", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPUTATIONS = Pattern.compile(".*reputations\\.json$", Pattern.CASE_INSENSITIVE);
    private static final FileFilter QUEST_SET_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return JSON.matcher(pathname.getName()).find() && !REPUTATIONS.matcher(pathname.getName()).find() && !BAGS.matcher(pathname.getName()).find();
        }
    };

    public CommandLoad() {
        super("load", "all");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) throws CommandException {
        try {
            if (arguments.length == 1 && arguments[0].equals("all")) {
                loadReputation(sender, getFile("reputations"));
                for (File file : getPossibleFiles(QUEST_SET_FILTER)) {
                    loadSet(sender, file);
                }
                QuestAdapter.postLoad();
            } else if (arguments.length == 1 && arguments[0].equals("bags")) {
                loadBags(sender, getFile("bags"));
            } else if (arguments.length > 0) {
                String file = getCombinedArgs(arguments);
                loadSet(sender, getFile(file));
                QuestAdapter.postLoad();
            }
            Quest.FILE_HELPER.saveData(null);
        } catch (IOException e) {
            throw new CommandException(e.getMessage());
        }
    }

    private File[] getPossibleFiles(FileFilter filter) {
        return new File(HardcoreQuesting.configDir + File.separator + "QuestFiles").listFiles(filter);
    }

    private void loadSet(ICommandSender sender, File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(Lang.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof EntityPlayer)
                HardcoreQuesting.setPlayer((EntityPlayer) sender);
            JsonReader reader = new JsonReader(new FileReader(file));
            QuestSet set = GSON.fromJson(reader, QuestSet.class);
            reader.close();
            if (set != null) {
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(Lang.LOAD_SUCCESS, set.getName())));
            } else {
                throw new CommandException(Lang.LOAD_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(Lang.LOAD_FAILED);
        }
    }

    private void loadReputation(ICommandSender sender, File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(Lang.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof EntityPlayer)
                HardcoreQuesting.setPlayer((EntityPlayer) sender);
            JsonReader reader = new JsonReader(new FileReader(file));
            List<Reputation> reputations = GSON.fromJson(reader, Reputation.getReputationList().getClass());
            reader.close();
            Reputation.getReputationList().clear();
            for (Reputation reputation : reputations) {
                if (reputation != null) {
                    Reputation.getReputationList().add(reputation);
                    sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(Lang.LOAD_SUCCESS, "Reputation: " + reputation.getName())));
                } else {
                    throw new CommandException(Lang.LOAD_FAILED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(Lang.LOAD_FAILED);
        }
    }

    private void loadBags(ICommandSender sender, File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(Lang.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof EntityPlayer)
                HardcoreQuesting.setPlayer((EntityPlayer) sender);
            JsonReader reader = new JsonReader(new FileReader(file));
            List<GroupTier> bags = GSON.fromJson(reader, new TypeToken<List<GroupTier>>() {
            }.getType());
            reader.close();
            if (bags != null) {
                GroupTier.getTiers().clear();
                GroupTier.getTiers().addAll(bags);
                sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocalFormatted(Lang.LOAD_SUCCESS, "Bags")));
            } else {
                throw new CommandException(Lang.LOAD_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(Lang.LOAD_FAILED);
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        String text = getCombinedArgs(args);
        Pattern pattern = Pattern.compile("^" + Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        List<String> results = super.addTabCompletionOptions(sender, args);
        for (File file : getPossibleFiles(QUEST_SET_FILTER)) {
            if (pattern.matcher(file.getName()).find()) results.add(file.getName().replace(".json", ""));
        }
        return results;
    }

    @Override
    public boolean isVisible(ICommandSender sender) {
        return Quest.isEditing && QuestingData.hasData(sender.getName()) && super.isVisible(sender);
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender) {
        return new int[]{0, 1, 2};
    }
}
