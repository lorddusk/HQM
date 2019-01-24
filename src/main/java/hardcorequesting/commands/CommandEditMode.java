package hardcorequesting.commands;

import hardcorequesting.quests.Quest;
import hardcorequesting.util.HQMUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author nooby
 */
public class CommandEditMode extends CommandBase {

    public CommandEditMode() {
        super("mode");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        if (sender instanceof EntityPlayer && isPlayerOp(sender)) {
            if(HQMUtil.isGameSingleplayer()){
                boolean newEditModeState = !Quest.canQuestsBeEdited();
                Quest.setEditMode(newEditModeState);
                if (newEditModeState) {
                    sender.sendMessage(new TextComponentTranslation(("Editing mode is now enabled.")));
                } else {
                    sender.sendMessage(new TextComponentTranslation(("Editing mode is now disabled.")));
                }
            } else {
                sender.sendMessage(new TextComponentTranslation(("Editing mode isn't intended for server use it is deactivated!")));
                Quest.setEditMode(false);
            }
        }
    }
}
