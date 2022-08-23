package hardcorequesting.common.commands;

import com.google.gson.reflect.TypeToken;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.reputation.Reputation;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandSave extends CommandBase {
    
    public CommandSave() {
        super("save", "all", "bags");
    }
    
    private static boolean stringsMatch(String[] sub, String[] search) {
        for (int i = 0; i < sub.length; i++) {
            if (!sub[i].equalsIgnoreCase(search[i])) return false;
        }
        return true;
    }
    
    private static void save(ICommandSender sender, Object object, Type type, String name) throws CommandException {
        try {
            File file = SaveHandler.save(SaveHandler.getExportFile(name), object, type);
            sender.sendMessage(Component.literalTranslation(CommandStrings.SAVE_SUCCESS, file.getPath().substring(HardcoreQuesting.configDir.getParentFile().getParent().length())));
        } catch (IOException e) {
            throw new CommandException(CommandStrings.SAVE_FAILED, name);
        }
    }
    
    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) throws CommandException {
        if (arguments.length == 1 && arguments[0].equals("all")) {
            try {
                save(sender, Reputation.getReputations(), new TypeToken<List<Reputation>>() {
                }.getType(), "reputations");
                save(sender, GroupTier.getTiers(), new TypeToken<List<GroupTier>>() {
                }.getType(), "bags");
            } catch (CommandException ignored) {
            }
            for (QuestSet set : Quest.getQuestSets()) {
                try {
                    save(sender, set, new TypeToken<QuestSet>() {
                    }.getType(), set.getFilename());
                } catch (CommandException ignored) {
                }
            }
            try {
                SaveHandler.saveQuestSetList(Quest.getQuestSets(), SaveHandler.getExportFile("sets"));
            } catch (IOException ignored) {
            }
        } else if (arguments.length == 1 && arguments[0].equals("bags")) {
            save(sender, GroupTier.getTiers(), new TypeToken<List<GroupTier>>() {
            }.getType(), "bags");
        } else if (arguments.length > 0) {
            for (QuestSet set : Quest.getQuestSets()) {
                String[] name = set.getName().split(" ");
                if (name.length < arguments.length && stringsMatch(name, arguments)) {
                    String fileName = "";
                    for (String subName : Arrays.copyOfRange(arguments, name.length, arguments.length)) {
                        fileName += subName + " ";
                    }
                    fileName = fileName.substring(0, fileName.length() - 1);
                    save(sender, set, new TypeToken<QuestSet>() {
                    }.getType(), fileName);
                    return;
                } else if (name.length == arguments.length && stringsMatch(name, arguments)) {
                    save(sender, set, new TypeToken<QuestSet>() {
                    }.getType(), set.getName());
                    return;
                }
            }
            String arg = "";
            for (String subName : arguments) {
                arg += subName + " ";
            }
            arg = arg.substring(0, arg.length() - 1);
            throw new CommandException(CommandStrings.QUEST_NOT_FOUND, arg);
        }
        
    }
    
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        String text = getCombinedArgs(args);
        Pattern pattern = Pattern.compile("^" + Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        List<String> results = super.addTabCompletionOptions(sender, args);
        for (QuestSet set : Quest.getQuestSets()) {
            if (pattern.matcher(set.getName()).find()) results.add(set.getName());
        }
        return results;
    }
    
    @Override
    public int[] getSyntaxOptions(ICommandSender sender) {
        return new int[]{0, 1, 2};
    }
}
