package hardcorequesting.common.quests.task.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.TaskType;
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
    
    public BlockRequirementTask(TaskType<? extends BlockRequirementTask> type, Quest parent) {
        super(type, parent);
    }
    
    @Override
    public void onUpdate(Player player) {
    }
    
    public void checkProgress(BlockState state, Player player) {
        ItemStack drop = new ItemStack(state.getBlock());
        NonNullList<ItemStack> consume = NonNullList.withSize(1, drop);
        increaseItems(consume, player.getUUID());
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(BLOCKS, parts.write(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, BLOCKS, new JsonArray()), QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER);
    }
}

