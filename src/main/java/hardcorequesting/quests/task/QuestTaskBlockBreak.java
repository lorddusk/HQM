package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class QuestTaskBlockBreak extends QuestTaskBlock {
    public QuestTaskBlockBreak(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.BLOCK_BROKEN);
    }
    
    public GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.BLOCK_BREAK_TASK;
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
    }
    
    @Override
    public void onBlockBroken(BlockPos blockPos, BlockState blockState, PlayerEntity player) {
        checkProgress(blockState, player);
    }
}

