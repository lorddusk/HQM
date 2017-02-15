package hardcorequesting.commands;

import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.Translator;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandBase implements ISubCommand {


    protected int permissionLevel = 3;
    private String name;
    private List<String> subCommands = new ArrayList<>();

    public CommandBase(String name, String... subCommands) {
        this.name = name;
        this.subCommands = Arrays.asList(subCommands);
    }

    @Override
    public int getPermissionLevel() {
        return permissionLevel;
    }

    @Override
    public String getCommandName() {
        return name;
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
        sender.addChatMessage(new TextComponentString(Translator.translate(plural, key, args)));
    }

    protected boolean isPlayerOp(ICommandSender sender) {
        return CommandHandler.isOwnerOrOp(sender);
    }

    protected void currentLives(EntityPlayer player) {
        sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live(s) left.");
    }
}
