package hardcorequesting.quests;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.QuestingData;
import hardcorequesting.client.interfaces.GuiEditMenuItem;
import hardcorequesting.network.DataReader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fluids.FluidStack;

public class QuestTaskItemsConsume extends QuestTaskItems {
    public QuestTaskItemsConsume(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }

    public boolean increaseFluid(FluidStack fluidStack, QuestDataTaskItems data, String playerName) {
        boolean updated = false;

        for (int i = 0; i < items.length; i++) {
            ItemRequirement item = items[i];
            if (item.fluid == null || item.required == data.progress[i]) {
                continue;
            }

            if (fluidStack != null && fluidStack.fluidID == item.fluid.getID()) {
                //System.out.println(fluidStack.amount);
                int amount = Math.min(item.required - data.progress[i], fluidStack.amount);
                data.progress[i] += amount;
                fluidStack.amount -= amount;
                updated = true;
                break;
            }
        }


        if (updated) {
            doCompletionCheck(data, playerName);
        }

        return updated;
    }

    @Override
    public void onUpdate(EntityPlayer player, DataReader dr) {
        if (increaseItems(player.inventory.mainInventory, (QuestDataTaskItems) getData(player), QuestingData.getUserName(player))) {
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
