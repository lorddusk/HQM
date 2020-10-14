package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.client.interfaces.GuiColor;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.UUID;

public class GroupAdapter {
    
    public static final Adapter<Group> GROUP_ADAPTER = new Adapter<Group>() {
        private static final String ID = "id";
        private static final String ITEMS = "items";
        private static final String NAME = "name";
        private static final String LIMIT = "limit";
        
        @Override
        public JsonElement serialize(Group src){
            return object()
                .add(ID, src.getId().toString())
                .use(builder -> {
                    if(src.hasName()){
                        builder.add(NAME, src.getName());
                    }
                })
                .add(LIMIT, src.getLimit())
                .add(ITEMS, array().use(builder -> src.getItems().stream().map(MinecraftAdapter.ITEM_STACK::serialize).forEach(builder::add)).build())
                .build();
        }
        
        @Override
        public Group deserialize(JsonElement jsonElement){
            if(!jsonElement.isJsonObject()){
                HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonElement for 'Group' is not a JsonObject but '" + jsonElement.getClass().getName() + "'!"));
                return null;
            }
            
            JsonObject json = jsonElement.getAsJsonObject();
            if(!json.has(ID) || !json.get(ID).isJsonPrimitive()){
                HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonElement '" + ID + "' for 'Group' is not present or a valid string value!"));
                return null;
            }
            if(!json.has(LIMIT) || !json.get(LIMIT).isJsonPrimitive() || !json.get(LIMIT).getAsJsonPrimitive().isNumber()){
                HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonElement '" + LIMIT + "' for 'Group' is not present or a valid int value!"));
                return null;
            }
            
            Group group;
            try{
                group = new Group(UUID.fromString(json.get(ID).getAsString()));
            } catch(IllegalArgumentException e){
                HardcoreQuestingCore.LOGGER.error("JsonElement '" + ID + "' for 'Group' can't be parsed to UUID!", e);
                return null;
            }
            group.setName(GsonHelper.getAsString(json, NAME, null));
            group.setLimit(json.get(LIMIT).getAsInt());
            
            if(json.has(ITEMS) && json.get(ITEMS).isJsonArray()){
                for(JsonElement element : json.get(ITEMS).getAsJsonArray()){
                    ItemStack stack = MinecraftAdapter.ITEM_STACK.deserialize(element);
                    if(stack != null && !stack.isEmpty()){
                        group.getItems().add(stack);
                    }
                }
            }
            
            if(!Group.getGroups().containsKey(group.getId())){
                Group.add(group);
            }
            return group;
        }
    };
    
    public static final Adapter<GroupTier> GROUP_TIER_ADAPTER = new Adapter<GroupTier>() {
        private static final String NAME = "name";
        private static final String COLOUR = "colour";
        private static final String WEIGHTS = "weights";
        private static final String GROUPS = "groups";
        
        @Override
        public JsonElement serialize(GroupTier src){
            return object()
                .add(NAME, src.getRawName())
                .add(COLOUR, src.getColor().name())
                .add(WEIGHTS, array().use(builder -> Arrays.stream(src.getWeights()).forEach(builder::add)).build())
                .add(GROUPS, array().use(builder -> Group.getGroups().values().stream().filter(group -> group.getTier().equals(src)).map(GROUP_ADAPTER::serialize).forEach(builder::add)).build())
                .build();
        }
        
        @Override
        public GroupTier deserialize(JsonElement jsonElement){
            if(!jsonElement.isJsonObject()){
                HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonElement for 'Group Tier' is not a JsonObject but '" + jsonElement.getClass().getName() + "'!"));
                return null;
            }
            
            JsonObject json = jsonElement.getAsJsonObject();
            int[] weights = new int[BagTier.values().length];
            String name = "";
            GuiColor color = GuiColor.GRAY;
            
            if(json.has(NAME) && json.get(NAME).isJsonPrimitive()){
                name = json.get(NAME).getAsString();
            }
            if(json.has(COLOUR) && json.get(COLOUR).isJsonPrimitive()){
                try{
                    color = GuiColor.valueOf(json.get(COLOUR).getAsString());
                } catch(IllegalArgumentException e){
                    HardcoreQuestingCore.LOGGER.error("JsonElement '" + COLOUR + "' of 'Group Tier' cannot be mapped to a color!", e);
                }
            }
            if(json.has(WEIGHTS) && json.get(WEIGHTS).isJsonArray()){
                int idx = 0;
                for(JsonElement element : json.get(WEIGHTS).getAsJsonArray()){
                    if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()){
                        weights[idx] = element.getAsJsonPrimitive().getAsInt();
                    } else {
                        HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonArray '" + WEIGHTS + "' of 'Group Tier' does contain a invalid value of type '" + element.getClass().getName() + "'!"));
                    }
                    idx++;
                }
            }
            
            GroupTier tier = new GroupTier(name, color, weights);
            
            if(json.has(GROUPS) && json.get(GROUPS).isJsonArray()){
                for(JsonElement element : json.get(GROUPS).getAsJsonArray()){
                    Group group = GROUP_ADAPTER.deserialize(element);
                    if(group != null){
                        group.setTier(tier);
                    } else {
                        HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonElement of 'Group Tier' can't be parsed as 'Group'!"));
                    }
                }
            }
            
            return tier;
        }
    };
    
}