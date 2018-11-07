package hardcorequesting.commands;

import hardcorequesting.quests.Quest;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandEditMode extends CommandBase {

    public CommandEditMode() {
        super("mode");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        if (sender instanceof EntityPlayer && isPlayerOp(sender)) {
            Quest.isEditing = !Quest.isEditing;
            if (Quest.isEditing) {
                sender.sendMessage(new TextComponentTranslation(("Editing mode is now enabled.").toString()));
            } else {
                sender.sendMessage(new TextComponentTranslation(("Editing mode is now disabled.").toString()));
            }
        }
    }
}
