package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.BlockEvent;

public class QuestTaskBlockBreak extends QuestTaskBlock {
    public QuestTaskBlockBreak(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventTrigger.Type.BLOCK_BROKEN);
    }

    public GuiEditMenuItem.Type getMenuTypeId() {
         return GuiEditMenuItem.Type.BLOCK_BREAK_TASK;
    }

    @Override
    public void onUpdate(EntityPlayer player) {
    }

    @Override
    public void onBlockBroken (BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();

        checkProgress(event.getState(), event.getPlayer());
    }
}

