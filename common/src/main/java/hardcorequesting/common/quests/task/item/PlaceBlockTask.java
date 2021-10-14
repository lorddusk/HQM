package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.TaskType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PlaceBlockTask extends ItemRequirementTask {
    public PlaceBlockTask(Quest parent, String description, String longDescription) {
        super(TaskType.BLOCK_PLACE, parent, description, longDescription);
        
        register(EventTrigger.Type.ITEM_USED);
    }
    
    public boolean mayUseFluids() {
        return false;   //TODO perhaps this could be done with fluids as well? check if an appropriate hook could be made
    }
    
    @Override
    public void onUpdate(Player player) {
    }
    
    @Override
    public void onItemUsed(Player playerEntity, Level world, InteractionHand hand) {
        handleRightClick(hand, playerEntity, playerEntity.getItemInHand(hand));
    }
    
    @Override
    public void onBlockUsed(Player playerEntity, Level world, InteractionHand hand) {
        handleRightClick(hand, playerEntity, playerEntity.getItemInHand(hand));
    }
    
    private void handleRightClick(InteractionHand hand, Player player, ItemStack itemStack) {
        if (hand != InteractionHand.MAIN_HAND) return;
        
        ItemStack placed = itemStack.copy();
        placed.setCount(1);
        NonNullList<ItemStack> consume = NonNullList.withSize(1, placed);
        increaseItems(consume, player.getUUID());
    }
}

