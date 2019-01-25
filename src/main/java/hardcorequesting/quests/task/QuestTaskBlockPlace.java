package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.BlockEvent;

public class QuestTaskBlockPlace extends QuestTaskBlock {
    public QuestTaskBlockPlace(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventTrigger.Type.BLOCK_PLACED);
    }

    public GuiEditMenuItem.Type getMenuTypeId() {
         return GuiEditMenuItem.Type.BLOCK_PLACE_TASK;
    }

    @Override
    public void onUpdate(EntityPlayer player) {
    }

    @Override
    public void onBlockPlaced (BlockEvent.PlaceEvent event) {
        checkProgress(event, event.getPlacedBlock(), event.getPlayer());
    }
}

