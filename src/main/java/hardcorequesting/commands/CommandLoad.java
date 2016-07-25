package hardcorequesting.commands;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.Lang;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.io.adapter.QuestAdapter;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.reputation.Reputation;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class CommandLoad extends CommandBase {


    public CommandLoad() {
        super("load", "all");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) throws CommandException {
        try {
            if (arguments.length == 1 && arguments[0].equals("all")) {
                loadReputation(sender, SaveHandler.getExportFile("reputations"));
                for (File file : getPossibleFiles(SaveHandler.QUEST_SET_FILTER)) {
                    loadSet(sender, file);
                }
                QuestAdapter.postLoad();
                QuestSet.orderAll();
            } else if (arguments.length == 1 && arguments[0].equals("bags")) {
                loadBags(sender, SaveHandler.getExportFile("bags"));
            } else if (arguments.length > 0) {
                String file = getCombinedArgs(arguments);
                loadSet(sender, SaveHandler.getExportFile(file));
                QuestAdapter.postLoad();
            }
        } catch (IOException e) {
            throw new CommandException(e.getMessage());
        }
    }

    private File[] getPossibleFiles(FileFilter filter) {
        return SaveHandler.getExportFolder().listFiles(filter);
    }

    private void loadSet(ICommandSender sender, File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(Lang.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof EntityPlayer)
                HardcoreQuesting.setPlayer((EntityPlayer) sender);
            QuestSet set = SaveHandler.loadQuestSet(file);
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
            List<Reputation> reputations = SaveHandler.loadReputations(file);
            Reputation.clear();
            for (Reputation reputation : reputations) {
                if (reputation != null) {
                    Reputation.addReputation(reputation);
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
            List<GroupTier> bags = SaveHandler.loadBags(file);
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
        for (File file : getPossibleFiles(SaveHandler.QUEST_SET_FILTER)) {
            if (pattern.matcher(file.getName()).find()) results.add(file.getName().replace(".json", ""));
        }
        return results;
    }

    @Override
    public boolean isVisible(ICommandSender sender) {
        return Quest.isEditing && sender instanceof EntityPlayer && QuestingData.hasData(((EntityPlayer) sender)) && super.isVisible(sender);
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender) {
        return new int[]{0, 1, 2};
    }
}
