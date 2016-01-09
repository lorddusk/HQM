package hardcorequesting.quests;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.EventHandler;
import hardcorequesting.QuestingData;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import hardcorequesting.network.DataReader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

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
    public void onOpenBook(EventHandler.BookOpeningEvent event) {
        if (event.isRealName()) {
            countItems(event.getPlayer(), null);
        }
    }

    @Override
    public void onUpdate(EntityPlayer player, DataReader dr) {
        countItems(player, null);
    }

    @Override
    public void onItemPickUp(EntityItemPickupEvent event) {
        if (event.entityPlayer.inventory.inventoryChanged) {
            countItems(event.entityPlayer, event.item.getEntityItem());
        }
    }

    @Override
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        onCrafting(event.player, event.crafting, event.craftMatrix);
    }

    public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) {
        if (player != null) {
            item = item.copy();
            if (item.stackSize == 0) {
                item.stackSize = 1;
            }
            countItems(player, item);
        }
    }

    private void countItems(EntityPlayer player, ItemStack item) {
        if (player.worldObj.isRemote) return;

        ItemStack[] items;
        if (item == null) {
            items = player.inventory.mainInventory;
        } else {
            items = new ItemStack[player.inventory.mainInventory.length + 1];
            ItemStack[] mainInventory = player.inventory.mainInventory;
            for (int i = 0; i < mainInventory.length; i++) {
                items[i] = mainInventory[i];
            }
            items[items.length - 1] = item;
        }
        countItems(items, (QuestDataTaskItems) getData(player), QuestingData.getUserName(player));
    }


    public void countItems(ItemStack[] itemsToCount, QuestDataTaskItems data, String playerName) {
        if (!parent.isAvailable(playerName)) return;


        boolean updated = false;

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (!item.hasItem || item.required == data.progress[i]) {
                continue;
            }

            for (ItemStack itemStack : itemsToCount) {
                if (item.getPrecision().areItemsSame(itemStack, item.getItem())) {
                    int amount = Math.min(itemStack.stackSize, item.required - data.progress[i]);
                    data.progress[i] += amount;
                    updated = true;
                }
            }
        }


        if (updated) {
            doCompletionCheck(data, playerName);
        }
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


}
