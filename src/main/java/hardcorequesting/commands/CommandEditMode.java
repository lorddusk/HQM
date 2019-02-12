package hardcorequesting.commands;

import hardcorequesting.config.HQMConfig;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.HQMUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 * @author nooby
 */
public class CommandEditMode extends CommandBase {

    public CommandEditMode() {
        super("edit");
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        if (sender instanceof EntityPlayer && isPlayerOp(sender)) {
            if (!HQMConfig.Message.OP_REMINDER && !HQMUtil.isGameSingleplayer()) {
                sender.sendMessage(new TextComponentTranslation("hqm.command.editMode.useOP").setStyle(new Style().setColor(TextFormatting.GREEN)));
            }

            if (HQMUtil.isGameSingleplayer() && QuestLine.doServerSync) {
                sender.sendMessage(new TextComponentTranslation("hqm.command.editMode.disableSync").setStyle(new Style().setColor(TextFormatting.RED).setBold(true)));
                Quest.setEditMode(false);
            } else if(HQMUtil.isGameSingleplayer()){
                boolean newEditModeState = !Quest.canQuestsBeEdited();
                Quest.setEditMode(newEditModeState);
                if (newEditModeState) {
                    sender.sendMessage(new TextComponentTranslation("hqm.command.editMode.enabled"));
                } else {
                    sender.sendMessage(new TextComponentTranslation("hqm.command.editMode.disabled"));
                }
                EntityPlayerSP player;

            } else {
                sender.sendMessage(new TextComponentTranslation("hqm.command.editMode.server"));
                Quest.setEditMode(false);
            }
        }
    }
}
