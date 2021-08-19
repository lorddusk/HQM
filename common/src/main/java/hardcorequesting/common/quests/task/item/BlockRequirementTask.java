package hardcorequesting.common.quests.task.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;

public abstract class BlockRequirementTask extends ItemRequirementTask {
    public static Method getSilkTouchDrop = null;
    public static final String NULL_NAME = "item.null.name";
    private static final String BLOCKS = "blocks";
    
    public BlockRequirementTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskItems.class;
    }
    
    @Override
    public void onUpdate(Player player) {
    }
    
    public void checkProgress(BlockState state, Player player) {
        ItemStack drop = new ItemStack(state.getBlock());
        NonNullList<ItemStack> consume = NonNullList.withSize(1, drop);
        increaseItems(consume, (QuestDataTaskItems) getData(player), player.getUUID());
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(BLOCKS, writeElements(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        readElements(GsonHelper.getAsJsonArray(object, BLOCKS, new JsonArray()), QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER);
    }
}

