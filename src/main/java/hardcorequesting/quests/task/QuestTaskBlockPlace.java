package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class QuestTaskBlockPlace extends QuestTaskItems {
    public QuestTaskBlockPlace(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.ITEM_USED);
    }
    
    public GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.BLOCK_PLACE_TASK;
    }
    
    @Override
    public void onUpdate(Player player) {
    }
    
    @Override
    public void onItemUsed(Player playerEntity, Level world, InteractionHand hand) {
        handleRightClick(hand, playerEntity, playerEntity.getItemInHand(hand));
    }
    
    @Override
    public void onBlockUsed(Player playerEntity, Level world, InteractionHand hand, BlockHitResult hitResult) {
        handleRightClick(hand, playerEntity, playerEntity.getItemInHand(hand));
    }
    
    private void handleRightClick(InteractionHand hand, Player player, ItemStack itemStack) {
        if (hand != InteractionHand.MAIN_HAND) return;
        
        NonNullList<ItemStack> consume = NonNullList.withSize(1, itemStack);
        increaseItems(consume, (QuestDataTaskItems) getData(player), player.getUUID());
    }
}

