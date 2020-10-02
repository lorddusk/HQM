package hardcorequesting.common.quests.task;

import hardcorequesting.common.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

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
    public void onUpdate(Player player) {
    }
    
    public void checkProgress(BlockState state, Player player) {
        ItemStack drop = new ItemStack(state.getBlock());
        NonNullList<ItemStack> consume = NonNullList.withSize(1, drop);
        increaseItems(consume, (QuestDataTaskItems) getData(player), player.getUUID());
    }
}

