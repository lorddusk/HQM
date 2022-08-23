package hardcorequesting.common.commands;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.io.adapter.QuestAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.reputation.Reputation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextComponentTranslation;

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
                QuestSet.orderAll(HardcoreQuesting.loadingSide.isServer());
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
            throw new CommandException(CommandStrings.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof PlayerEntity)
                HardcoreQuesting.setPlayer((PlayerEntity) sender);
            QuestSet set = SaveHandler.loadQuestSet(file);
            if (set != null) {
                
                sender.sendMessage(Component.literalTranslation(CommandStrings.LOAD_SUCCESS, set.getName()));
            } else {
                throw new CommandException(CommandStrings.LOAD_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(CommandStrings.LOAD_FAILED);
        }
    }
    
    private void loadReputation(ICommandSender sender, File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(CommandStrings.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof PlayerEntity)
                HardcoreQuesting.setPlayer((PlayerEntity) sender);
            List<Reputation> reputations = SaveHandler.loadReputations(file);
            Reputation.clear();
            for (Reputation reputation : reputations) {
                if (reputation != null) {
                    Reputation.addReputation(reputation);
                    sender.sendMessage(Component.literalTranslation(CommandStrings.LOAD_SUCCESS, "Reputation: " + reputation.getName()));
                } else {
                    throw new CommandException(CommandStrings.LOAD_FAILED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(CommandStrings.LOAD_FAILED);
        }
    }
    
    private void loadBags(ICommandSender sender, File file) throws CommandException {
        if (!file.exists()) {
            throw new CommandException(CommandStrings.FILE_NOT_FOUND);
        }
        try {
            if (sender instanceof PlayerEntity)
                HardcoreQuesting.setPlayer((PlayerEntity) sender);
            List<GroupTier> bags = SaveHandler.loadBags(file);
            if (bags != null) {
                GroupTier.getTiers().clear();
                GroupTier.getTiers().addAll(bags);
                sender.sendMessage(Component.literalTranslation(CommandStrings.LOAD_SUCCESS, "Bags"));
            } else {
                throw new CommandException(CommandStrings.LOAD_FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException(CommandStrings.LOAD_FAILED);
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
        return sender instanceof PlayerEntity && Quest.canQuestsBeEdited() && QuestingData.hasData(((PlayerEntity) sender)) && super.isVisible(sender);
    }
    
    @Override
    public int[] getSyntaxOptions(ICommandSender sender) {
        return new int[]{0, 1, 2};
    }
}
