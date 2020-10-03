package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuItem;
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
import java.util.ArrayList;
import java.util.List;

public abstract class QuestTaskBlock extends QuestTaskItems {
    public static Method getSilkTouchDrop = null;
    public static final String NULL_NAME = "item.null.name";
    private static final String BLOCKS = "blocks";
    
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
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (ItemRequirement item : getItems()) {
            array.add(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER.toJsonTree(item));
        }
        builder.add(BLOCKS, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        List<ItemRequirement> list = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, BLOCKS, new JsonArray())) {
            list.add(QuestTaskAdapter.ITEM_REQUIREMENT_ADAPTER.fromJsonTree(element));
        }
        setItems(list.toArray(new ItemRequirement[0]));
    }
}

