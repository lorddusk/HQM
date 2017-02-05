package hardcorequesting.quests.task;

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

import java.util.Arrays;

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
    protected void doCompletionCheck(QuestDataTaskItems data, String playerName) {
        boolean isDone = true;
        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.required > data.progress[i]) {
                data.progress[i] = 0; //Clear unfinished ones
                isDone = false;
            }
        }

        if (isDone) {
            completeTask(playerName);
        }
        parent.sendUpdatedDataToTeam(playerName);
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        countItems(player, null);
    }

    @Override
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        onCrafting(event.player, event.crafting, event.craftMatrix);
    }

    @Override
    public void onItemPickUp(EntityItemPickupEvent event) {
        if (event.getEntityPlayer().inventory.inventoryChanged) {
            countItems(event.getEntityPlayer(), event.getItem().getEntityItem());
        }
    }

    @Override
    public void onOpenBook(EventHandler.BookOpeningEvent event) {
        if (event.isRealName()) {
            countItems(event.getPlayer(), null);
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
        if (player.world.isRemote) return;

        NonNullList<ItemStack> items;
        if (stack.isEmpty()) {
            items = player.inventory.mainInventory;
        } else {
            items = NonNullList.create();
            NonNullList<ItemStack> mainInventory = player.inventory.mainInventory;
            for (int i = 0; i < mainInventory.size(); i++) {
                items.set(i, mainInventory.get(i));
            }
            items.set(items.size() - 1, stack);
        }
        countItems(items, (QuestDataTaskItems) getData(player), QuestingData.getUserUUID(player));
    }

    public void countItems(NonNullList<ItemStack> itemsToCount, QuestDataTaskItems data, String playerName) {
        if (!parent.isAvailable(playerName)) return;


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
            doCompletionCheck(data, playerName);
        }
    }


}
