package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class QuestTaskBlockPlace extends QuestTaskItems {
    public QuestTaskBlockPlace(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.ITEM_USED);
    }
    
    public GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.BLOCK_PLACE_TASK;
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
    }
    
    @Override
    public void onItemUsed(PlayerEntity playerEntity, World world, Hand hand) {
        handleRightClick(hand, playerEntity, playerEntity.getStackInHand(hand));
    }
    
    @Override
    public void onBlockUsed(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult hitResult) {
        handleRightClick(hand, playerEntity, playerEntity.getStackInHand(hand));
    }
    
    private void handleRightClick(Hand hand, PlayerEntity player, ItemStack itemStack) {
        if (hand != Hand.MAIN_HAND) return;
        
        DefaultedList<ItemStack> consume = DefaultedList.ofSize(1, itemStack);
        increaseItems(consume, (QuestDataTaskItems) getData(player), player.getUuid());
    }
}

