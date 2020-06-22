package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class QuestTaskItemsCrafting extends QuestTaskItems {
    
    public QuestTaskItemsCrafting(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.CRAFTING);
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
        
    }
    
    @Override
    public void onCrafting(PlayerEntity player, ItemStack stack, CraftingInventory craftingInv) {
        create(player, stack);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CRAFTING_TASK;
    }
    
    private void create(PlayerEntity player, ItemStack stack) {
        if (!player.getEntityWorld().isClient) {
            if (!stack.isEmpty()) {
                //no need for the quest to be active
                //if (parent.isVisible(player) && parent.isEnabled(player) && isVisible(player)) {
                stack = stack.copy();
                if (stack.getCount() == 0) {
                    stack.setCount(1);
                }
                DefaultedList<ItemStack> list = DefaultedList.of();
                list.add(stack);
                increaseItems(list, (QuestDataTaskItems) getData(player), player.getUuid());
                //}
            }
        }
    }
    
    
}
