package hardcorequesting.quests.task;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;

public class QuestTaskItemsConsume extends QuestTaskItems {
    public QuestTaskItemsConsume(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }

    public boolean increaseFluid(FluidStack fluidStack, QuestDataTaskItems data, String uuid) {
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
            doCompletionCheck(data, uuid);
        }

        return updated;
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (increaseItems(player.inventory.mainInventory, (QuestDataTaskItems) getData(player), QuestingData.getUserUUID(player))) {
            player.inventory.markDirty();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected GuiEditMenuItem.Type getMenuTypeId() {
        return GuiEditMenuItem.Type.CONSUME_TASK;
    }

    public boolean allowManual() {
        return true;
    }
}
