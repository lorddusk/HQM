package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class QuestTaskBlock extends QuestTaskItems {
    public QuestTaskBlock(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskItems.class;
    }

    public abstract GuiEditMenuItem.Type getMenuTypeId();

    @Override
    public void onUpdate(EntityPlayer player) {
    }

    public boolean compareProperties (IBlockState stateFound, IBlockState stateWanted) {
        if (stateFound.getBlock() != stateWanted.getBlock()) return false;

        for(IProperty<?> key : stateWanted.getProperties().keySet()) {
            if (key.getName().equals("facing")) continue;

            if (!stateWanted.getValue(key).equals(stateFound.getValue(key))) return false;
        }

        return true;
    }

    public void checkProgress(IBlockState state, EntityPlayer player) {
        NonNullList<ItemStack> consume = NonNullList.create();

        for (ItemRequirement requirement : items) {
            ItemStack stack = requirement.getStack();
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                ItemBlock iblock = (ItemBlock) stack.getItem();
                Block block = iblock.getBlock();
                if (compareProperties(state, block.getStateFromMeta(stack.getMetadata()))) {
                    ItemStack newStack = stack.copy();
                    newStack.setCount(1);
                    consume.add(newStack);
                }
            }
        }

        if (consume.size() != 0) {
            increaseItems(consume, (QuestDataTaskItems) getData(player), player.getPersistentID());
        }
    }
}

