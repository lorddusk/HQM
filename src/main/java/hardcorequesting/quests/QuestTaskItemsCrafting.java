package hardcorequesting.quests;

import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.EventHandler;
import hardcorequesting.QuestingData;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import hardcorequesting.network.DataReader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class QuestTaskItemsCrafting extends QuestTaskItems {
    public QuestTaskItemsCrafting(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventHandler.Type.CRAFTING);
    }

    @Override
    public void onUpdate(EntityPlayer player, DataReader dr) {

    }

    @SideOnly(Side.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CRAFTING_TASK;
    }

    @Override
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        create(event.player, event.crafting);
    }

    private void create(EntityPlayer player, ItemStack item) {
        if (!player.worldObj.isRemote) {
            if (player != null && item != null) {
                //no need for the quest to be active
                //if (parent.isVisible(player) && parent.isEnabled(player) && isVisible(player)) {
                item = item.copy();
                if (item.stackSize == 0) {
                    item.stackSize = 1;
                }
                increaseItems(new ItemStack[]{item}, (QuestDataTaskItems) getData(player), QuestingData.getUserName(player));
                //}
            }
        }
    }


}
