package hardcorequesting.quests.task;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.lang.reflect.Method;

public abstract class QuestTaskBlock extends QuestTaskItems {
    public static Method getSilkTouchDrop = null;
    public static final String NULL_NAME = "item.null.name";
    
    public QuestTaskBlock(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskItems.class;
    }
    
    public abstract GuiEditMenuItem.Type getMenuTypeId();
    
    @Override
    public void onUpdate(PlayerEntity player) {
    }
    
    public void checkProgress(BlockState state, PlayerEntity player) {
        ItemStack drop = new ItemStack(state.getBlock());
        DefaultedList<ItemStack> consume = DefaultedList.ofSize(1, drop);
        increaseItems(consume, (QuestDataTaskItems) getData(player), player.getUuid());
    }
}

