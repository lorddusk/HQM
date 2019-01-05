package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class QuestTaskItemsConsume extends QuestTaskItems {

    public QuestTaskItemsConsume(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }

    public boolean increaseFluid(FluidStack fluidStack, QuestDataTaskItems data, UUID playerId) {
        boolean updated = false;

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.fluid == null || item.required == data.progress[i]) {
                continue;
            }

            if (fluidStack != null && fluidStack.getFluid().getName().equals(item.fluid.getName())) {
                //System.out.println(fluidStack.amount);
                int amount = Math.min(item.required - data.progress[i], fluidStack.amount);
                data.progress[i] += amount;
                fluidStack.amount -= amount;
                updated = true;
                break;
            }
        }


        if (updated) {
            doCompletionCheck(data, playerId);
        }

        return updated;
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (increaseItems(player.inventory.mainInventory, (QuestDataTaskItems) getData(player), player.getPersistentID())) {
            player.inventory.markDirty();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CONSUME_TASK;
    }

    @Override
    public boolean allowManual() {
        return true;
    }
}
