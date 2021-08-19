package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.UUID;

public class DetectItemTask extends ItemRequirementTask {
    
    public DetectItemTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.CRAFTING, EventTrigger.Type.PICK_UP, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected boolean mayUseFluids() {
        return false;   //TODO we could probably include fluids if we check items for fluid content
    }
    
    @Override
    public void doCompletionCheck(QuestDataTaskItems data, UUID playerID) {
        boolean isDone = true;
        for (int i = 0; i < elements.size(); i++) {
            Part item = elements.get(i);
            if (!data.isDone(i, item)) {
                data.setValue(i, 0); //Clear unfinished ones
                isDone = false;
            }
        }
        
        if (isDone) {
            completeTask(playerID);
        }
        parent.sendUpdatedDataToTeam(playerID);
    }
    
    @Override
    public void onUpdate(Player player) {
        countItems(player, ItemStack.EMPTY);
    }
    
    @Override
    public void onItemPickUp(Player playerEntity, ItemStack stack) {
        countItems(playerEntity, stack);
    }
    
    @Override
    public void onOpenBook(EventTrigger.BookOpeningEvent event) {
        if (event.isRealName()) {
            countItems(event.getPlayer(), ItemStack.EMPTY);
        }
    }
    
    @Override
    public void onCrafting(Player player, ItemStack stack) {
        if (player != null) {
            stack = stack.copy();
            if (stack.getCount() == 0) {
                stack.setCount(1);
            }
            countItems(player, stack);
        }
    }
    
    private void countItems(Player player, ItemStack stack) {
        if (!player.getCommandSenderWorld().isClientSide) {
            NonNullList<ItemStack> items = NonNullList.withSize(player.inventory.items.size() + 1, ItemStack.EMPTY);
            Collections.copy(items, player.inventory.items);
            if (!stack.isEmpty()) {
                items.set(items.size() - 1, stack);
            }
            countItems(items, getData(player), player.getUUID());
        }
    }
    
    public void countItems(NonNullList<ItemStack> itemsToCount, QuestDataTaskItems data, UUID playerID) {
        if (!parent.isAvailable(playerID)) return;
        
        
        boolean updated = false;
        
        for (int i = 0; i < elements.size(); i++) {
            Part item = elements.get(i);
            if (!item.hasItem || data.isDone(i, item)) {
                continue;
            }
            
            for (ItemStack stack : itemsToCount) {
                if (item.getPrecision().areItemsSame(stack, item.getStack())) {
                    int amount = Math.min(stack.getCount(), item.required - data.getValue(i));
                    data.setValue(i, data.getValue(i) + amount);
                    updated = true;
                }
            }
        }
        
        
        if (updated) {
            doCompletionCheck(data, playerID);
        }
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
}
