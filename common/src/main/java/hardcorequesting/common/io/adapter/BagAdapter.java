package hardcorequesting.common.io.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.client.interfaces.GuiColor;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class BagAdapter {
    
    public static final Adapter<Group> GROUP_ADAPTER = new Adapter<Group>() {
        private static final String ID = "id";
        private static final String ITEMS = "items";
        private static final String NAME = "name";
        private static final String LIMIT = "limit";
        
        @Override
        public JsonElement serialize(Group src) {
            return object()
                    .add(ID, src.getId().toString())
                    .use(builder -> {
                        if (src.hasName())
                            builder.add(NAME, src.getName());
                    })
                    .add(LIMIT, src.getLimit())
                    .add(ITEMS, array()
                            .use(builder -> {
                                for (ItemStack stack : src.getItems())
                                    builder.add(MinecraftAdapter.ITEM_STACK.serialize(stack));
                            })
                            .build())
                    .build();
        }
        
        @Override
        public Group deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            
            Group group = new Group(UUID.fromString(GsonHelper.getAsString(object, ID)));
            group.setName(GsonHelper.getAsString(object, NAME, null));
            group.setLimit(GsonHelper.getAsInt(object, LIMIT));
            for (JsonElement element : GsonHelper.getAsJsonArray(object, ITEMS, new JsonArray())) {
                ItemStack stack = MinecraftAdapter.ITEM_STACK.deserialize(element);
                if (!stack.isEmpty()) {
                    group.getItems().add(stack);
                }
            }
            if (!Group.getGroups().containsKey(group.getId()))
                Group.add(group);
            return group;
        }
    };
    public static final Adapter<GroupTier> GROUP_TIER_ADAPTER = new Adapter<GroupTier>() {
        private static final String NAME = "name";
        private static final String COLOUR = "colour";
        private static final String WEIGHTS = "weights";
        private static final String GROUPS = "groups";
        
        @Override
        public JsonElement serialize(GroupTier src) {
            JsonArrayBuilder weights = array();
            for (int weight : src.getWeights()) {
                weights.add(weight);
            }
            JsonArrayBuilder groups = array();
            for (Group group : Group.getGroups().values())
                if (group.getTier() == src)
                    groups.add(GROUP_ADAPTER.serialize(group));
            
            return object()
                    .add(NAME, src.getRawName())
                    .add(COLOUR, src.getColor().name())
                    .add(WEIGHTS, weights.build())
                    .add(GROUPS, groups.build())
                    .build();
        }
        
        @Override
        public GroupTier deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            int[] weights = new int[BagTier.values().length];
            
            JsonArray weightsArray = GsonHelper.getAsJsonArray(object, WEIGHTS, null);
            if (weightsArray != null) {
                for (int i = 0; i < weights.length && i < weightsArray.size(); i++) {
                    weights[i] = weightsArray.get(i).getAsInt();
                }
            }
            
            GroupTier tier = new GroupTier(
                    GsonHelper.getAsString(object, NAME, ""),
                    GuiColor.valueOf(GsonHelper.getAsString(object, COLOUR, "GRAY")),
                    weights
            );
            
            JsonArray groupsArray = GsonHelper.getAsJsonArray(object, GROUPS, null);
            if (groupsArray != null) {
                for (JsonElement element : groupsArray) {
                    Group group = GROUP_ADAPTER.deserialize(element);
                    if (group != null) {
                        group.setTier(tier);
                    }
                }
            }
            
            return tier;
        }
    };
    
    
}
