package hardcorequesting.quests.task;

import java.util.Arrays;
import java.util.Collections;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuestTaskItemsDetect extends QuestTaskItems {

    public QuestTaskItemsDetect(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventHandler.Type.CRAFTING, EventHandler.Type.PICK_UP, EventHandler.Type.OPEN_BOOK);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CRAFTING_TASK;
    }

    @Override
    protected void doCompletionCheck(QuestDataTaskItems data, String uuid) {
        boolean isDone = true;
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.required > data.progress[i]) {
                data.progress[i] = 0; //Clear unfinished ones
                isDone = false;
            }
        }

        if (isDone) {
            completeTask(uuid);
        }
        parent.sendUpdatedDataToTeam(uuid);
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        countItems(player, ItemStack.EMPTY);
    }

    @Override
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        onCrafting(event.player, event.crafting, event.craftMatrix);
    }

    @Override
    public void onItemPickUp(EntityItemPickupEvent event) {
        countItems(event.getEntityPlayer(), event.getItem().getItem());
    }

    @Override
    public void onOpenBook(EventHandler.BookOpeningEvent event) {
        if (event.isRealName()) {
            countItems(event.getPlayer(), ItemStack.EMPTY);
        }
    }

    public void onCrafting(EntityPlayer player, ItemStack stack, IInventory craftMatrix) {
        if (player != null) {
            stack = stack.copy();
            if (stack.getCount() == 0) {
                stack.setCount(1);
            }
            countItems(player, stack);
        }
    }

    private void countItems(EntityPlayer player, ItemStack stack) {
        if(!player.getEntityWorld().isRemote){
            NonNullList<ItemStack> items = NonNullList.withSize(player.inventory.mainInventory.size() + 1, ItemStack.EMPTY);
            Collections.copy(items, player.inventory.mainInventory);
            if(!stack.isEmpty()){
                items.set(items.size() - 1, stack);
            }
            countItems(items, (QuestDataTaskItems) getData(player), QuestingData.getUserUUID(player));
        }
    }

    public void countItems(NonNullList<ItemStack> itemsToCount, QuestDataTaskItems data, String uuid) {
        if (!parent.isAvailable(uuid)) return;


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
            doCompletionCheck(data, uuid);
        }
    }


}
