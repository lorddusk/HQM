package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.interfaces.GuiColor;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BagAdapter {
    
    public static final TypeAdapter<Group> GROUP_ADAPTER = new TypeAdapter<Group>() {
        private final String ID = "id";
        private final String ITEMS = "items";
        private final String NAME = "name";
        private final String LIMIT = "limit";
        
        @Override
        public void write(JsonWriter out, Group value) throws IOException {
            out.beginObject();
            out.name(ID).value(value.getId().toString());
            if (value.hasName())
                out.name(NAME).value(value.getName());
            out.name(LIMIT).value(value.getLimit());
            out.name(ITEMS).beginArray();
            for (ItemStack stack : value.getItems())
                MinecraftAdapter.ITEM_STACK.write(out, stack);
            out.endArray();
            out.endObject();
        }
        
        @Override
        public Group read(JsonReader in) throws IOException {
            in.beginObject();
            String name = null, id = null;
            int limit = 0;
            List<ItemStack> items = new ArrayList<>();
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case ID:
                        id = in.nextString();
                        break;
                    case NAME:
                        name = in.nextString();
                        break;
                    case LIMIT:
                        limit = in.nextInt();
                        break;
                    case ITEMS:
                        in.beginArray();
                        while (in.hasNext()) {
                            ItemStack stack = MinecraftAdapter.ITEM_STACK.read(in);
                            if (!stack.isEmpty()) {
                                items.add(stack);
                            }
                        }
                        in.endArray();
                        break;
                }
            }
            in.endObject();
            Group group = new Group(UUID.fromString(id));
            group.setName(name);
            group.setLimit(limit);
            group.getItems().addAll(items);
            if (!Group.getGroups().containsKey(group.getId()))
                Group.add(group);
            return group;
        }
    };
    public static final TypeAdapter<GroupTier> GROUP_TIER_ADAPTER = new TypeAdapter<GroupTier>() {
        private final String NAME = "name";
        private final String COLOUR = "colour";
        private final String WEIGHTS = "weights";
        private final String GROUPS = "groups";
        
        @Override
        public void write(JsonWriter out, GroupTier value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getRawName());
            out.name(COLOUR).value(value.getColor().name());
            out.name(WEIGHTS).beginArray();
            for (int i : value.getWeights()) {
                out.value(i);
            }
            out.endArray();
            out.name(GROUPS).beginArray();
            for (Group group : Group.getGroups().values())
                if (group.getTier() == value)
                    GROUP_ADAPTER.write(out, group);
            out.endArray();
            out.endObject();
        }
        
        @Override
        public GroupTier read(JsonReader in) throws IOException {
            in.beginObject();
            String name = "";
            GuiColor colour = GuiColor.GRAY;
            int[] weights = new int[BagTier.values().length];
            List<Group> groups = new ArrayList<>();
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case NAME:
                        name = in.nextString();
                        break;
                    case COLOUR:
                        colour = GuiColor.valueOf(in.nextString());
                        break;
                    case WEIGHTS:
                        in.beginArray();
                        for (int i = 0; i < weights.length && in.hasNext(); i++) {
                            weights[i] = in.nextInt();
                        }
                        in.endArray();
                        break;
                    case GROUPS:
                        in.beginArray();
                        while (in.hasNext()) {
                            Group group = GROUP_ADAPTER.read(in);
                            if (group != null)
                                groups.add(group);
                        }
                        in.endArray();
                        break;
                }
            }
            in.endObject();
            GroupTier tier = new GroupTier(name, colour, weights);
            for (Group group : groups) {
                group.setTier(tier);
            }
            return tier;
        }
    };
    
    
}
