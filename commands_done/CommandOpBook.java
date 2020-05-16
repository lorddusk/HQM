package hardcorequesting.commands;

import hardcorequesting.items.ItemQuestBook;
import hardcorequesting.quests.QuestingData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;

public class CommandOpBook extends CommandBase {
    
    public CommandOpBook() {
        super("op");
    }
    
    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        if (sender instanceof PlayerEntity && isPlayerOp(sender)) {
            PlayerEntity player = (PlayerEntity) sender;
            if (arguments.length == 1)
                player = sender.getEntityWorld().getPlayerEntityByName(arguments[0]);
            if (QuestingData.hasData(player)) {
                player.inventory.addItemStackToInventory(ItemQuestBook.getOPBook(player));
            } else {
                sendChat(player, "hqm.message.noPlayer");
            }
        }
    }
}
