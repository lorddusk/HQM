package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class QuestTaskBlockPlace extends QuestTaskBlock {
    public QuestTaskBlockPlace(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventTrigger.Type.ITEM_USED);
    }

    public GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.BLOCK_PLACE_TASK;
    }

    @Override
    public void onUpdate(EntityPlayer player) {
    }

    @Override
    public void onItemUsed(PlayerInteractEvent.RightClickItem event) {
        handleRightClick(event.getHand(), event.getEntityPlayer(), event.getItemStack());
    }

    @Override
    public void onItemUsed(PlayerInteractEvent.RightClickBlock event) {
        handleRightClick(event.getHand(), event.getEntityPlayer(), event.getItemStack());
    }

    private void handleRightClick(EnumHand hand, EntityPlayer player, ItemStack itemStack) {
        if (hand != EnumHand.MAIN_HAND) return;

        NonNullList<ItemStack> consume = NonNullList.withSize(1, itemStack);
        increaseItems(consume, (QuestDataTaskItems) getData(player), player.getPersistentID());
    }
}

