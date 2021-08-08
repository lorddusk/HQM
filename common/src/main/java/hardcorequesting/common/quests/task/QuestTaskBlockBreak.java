package hardcorequesting.common.quests.task;

import hardcorequesting.common.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class QuestTaskBlockBreak extends QuestTaskBlock {
    public QuestTaskBlockBreak(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.BLOCK_BROKEN);
    }
    
    public GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.ITEM;
    }
    
    @Override
    public void onUpdate(Player player) {
    }
    
    @Override
    public void onBlockBroken(BlockPos blockPos, BlockState blockState, Player player) {
        checkProgress(blockState, player);
    }
}

