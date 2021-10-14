package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.TaskType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class BreakBlockTask extends BlockRequirementTask {
    public BreakBlockTask(Quest parent, String description, String longDescription) {
        super(TaskType.BLOCK_BREAK, parent, description, longDescription);
        
        register(EventTrigger.Type.BLOCK_BROKEN);
    }
    
    public boolean mayUseFluids() {
        return false;
    }
    
    @Override
    public void onUpdate(Player player) {
    }
    
    @Override
    public void onBlockBroken(BlockPos blockPos, BlockState blockState, Player player) {
        checkProgress(blockState, player);
    }
}

