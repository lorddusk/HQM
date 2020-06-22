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

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class QuestTaskItemsDetect extends QuestTaskItems {
    
    public QuestTaskItemsDetect(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.CRAFTING, EventTrigger.Type.PICK_UP, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CRAFTING_TASK;
    }
    
    @Override
    public void doCompletionCheck(QuestDataTaskItems data, UUID playerID) {
        boolean isDone = true;
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.required > data.progress[i]) {
                data.progress[i] = 0; //Clear unfinished ones
                isDone = false;
            }
        }
        
        if (isDone) {
            completeTask(playerID);
        }
        parent.sendUpdatedDataToTeam(playerID);
    }
    
    @Override
    public void onUpdate(PlayerEntity player) {
        countItems(player, ItemStack.EMPTY);
    }
    
    @Override
    public void onCrafting(PlayerEntity player, ItemStack stack, CraftingInventory craftingInv) {
        onCrafting(player, stack);
    }
    
    @Override
    public void onItemPickUp(PlayerEntity playerEntity, ItemStack stack) {
        countItems(playerEntity, stack);
    }
    
    @Override
    public void onOpenBook(EventTrigger.BookOpeningEvent event) {
        if (event.isRealName()) {
            countItems(event.getPlayer(), ItemStack.EMPTY);
        }
    }
    
    public void onCrafting(PlayerEntity player, ItemStack stack) {
        if (player != null) {
            stack = stack.copy();
            if (stack.getCount() == 0) {
                stack.setCount(1);
            }
            countItems(player, stack);
        }
    }
    
    private void countItems(PlayerEntity player, ItemStack stack) {
        if (!player.getEntityWorld().isClient) {
            DefaultedList<ItemStack> items = DefaultedList.ofSize(player.inventory.main.size() + 1, ItemStack.EMPTY);
            Collections.copy(items, player.inventory.main);
            if (!stack.isEmpty()) {
                items.set(items.size() - 1, stack);
            }
            countItems(items, (QuestDataTaskItems) getData(player), player.getUuid());
        }
    }
    
    public void countItems(DefaultedList<ItemStack> itemsToCount, QuestDataTaskItems data, UUID playerID) {
        if (!parent.isAvailable(playerID)) return;
        
        
        boolean updated = false;
        
        if (data.progress.length < items.length)
            data.progress = Arrays.copyOf(data.progress, items.length);
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (!item.hasItem || item.required == data.progress[i]) {
                continue;
            }
            
            for (ItemStack stack : itemsToCount) {
                if (item.getPrecision().areItemsSame(stack, item.getStack())) {
                    int amount = Math.min(stack.getCount(), item.required - data.progress[i]);
                    data.progress[i] += amount;
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
