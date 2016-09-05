package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.*;
import hardcorequesting.quests.task.*;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationMarker;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static hardcorequesting.io.adapter.QuestAdapter.QUEST;

public class QuestTaskAdapter {
    public static QuestTask TASK;
    protected static Map<QuestTaskReputation, List<ReputationSettingConstructor>> taskReputationListMap = new HashMap<>();

    protected static final TypeAdapter<QuestTaskItems.ItemRequirement> ITEM_REQUIREMENT_ADAPTER = new TypeAdapter<QuestTaskItems.ItemRequirement>() {
        private final String ITEM = "item";
        private final String FLUID = "fluid";
        private final String REQUIRED = "required";
        private final String PRECISION = "precision";

        @Override
        public void write(JsonWriter out, QuestTaskItems.ItemRequirement value) throws IOException {
            ItemStack item = value.getItem();
            Fluid fluid = value.fluid;
            int required = value.required;
            ItemPrecision precision = value.getPrecision();
            out.beginObject();
            if (item != null) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ITEM), item);
            } else if (fluid != null) {
                MinecraftAdapter.FLUID.write(out.name(FLUID), fluid);
            } else {
                out.nullValue();
                out.endObject();
                return;
            }
            if (required != 1)
                out.name(REQUIRED).value(required);
            if (precision != ItemPrecision.PRECISE)
                out.name(PRECISION).value(ItemPrecision.getUniqueID(precision));
            out.endObject();
        }

        @Override
        public QuestTaskItems.ItemRequirement read(JsonReader in) throws IOException {
            in.beginObject();
            ItemStack item = null;
            Fluid fluid = null;
            int required = 1;
            ItemPrecision precision = ItemPrecision.PRECISE;
            while (in.hasNext()) {
                String next = in.nextName();
                if (next.equalsIgnoreCase(ITEM)) {
                    item = MinecraftAdapter.ITEM_STACK.read(in);
                } else if (next.equalsIgnoreCase(FLUID)) {
                    fluid = MinecraftAdapter.FLUID.read(in);
                } else if (next.equalsIgnoreCase(REQUIRED)) {
                    required = Math.max(in.nextInt(), required);
                } else if (next.equalsIgnoreCase(PRECISION)) {
                    ItemPrecision itemPrecision = ItemPrecision.getPrecisionType(in.nextString());
                    if (itemPrecision != null) {
                        precision = itemPrecision;
                    }
                }
            }
            in.endObject();
            QuestTaskItems.ItemRequirement result = null;
            if (item != null) {
                result = new QuestTaskItems.ItemRequirement(item, required);
            } else if (fluid != null) {
                result = new QuestTaskItems.ItemRequirement(fluid, required);
            } else {
                return null;
            }
            result.setPrecision(precision);
            return result;
        }
    };

    protected static final TypeAdapter<QuestTaskLocation.Location> LOCATION_ADAPTER = new TypeAdapter<QuestTaskLocation.Location>() {
        private final String X = "x";
        private final String Y = "y";
        private final String Z = "z";
        private final String DIM = "dim";
        private final String ICON = "icon";
        private final String RADIUS = "radius";
        private final String VISIBLE = "visible";
        private final String NAME = "name";

        @Override
        public void write(JsonWriter out, QuestTaskLocation.Location value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            ItemStack stack = value.getIcon();
            if (stack != null) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ICON), stack);
            }
            out.name(X).value(value.getX());
            out.name(Y).value(value.getY());
            out.name(Z).value(value.getZ());
            out.name(DIM).value(value.getDimension());
            out.name(RADIUS).value(value.getRadius());
            if (value.getVisible() != QuestTaskLocation.Visibility.LOCATION)
                out.name(VISIBLE).value(value.getVisible().name());
            out.endObject();
        }

        @Override
        public QuestTaskLocation.Location read(JsonReader in) throws IOException {
            in.beginObject();
            QuestTaskLocation.Location result = new QuestTaskLocation.Location();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(NAME)) {
                    result.setName(in.nextString());
                } else if (name.equalsIgnoreCase(X)) {
                    result.setX(in.nextInt());
                } else if (name.equalsIgnoreCase(Y)) {
                    result.setY(in.nextInt());
                } else if (name.equalsIgnoreCase(Z)) {
                    result.setZ(in.nextInt());
                } else if (name.equalsIgnoreCase(DIM)) {
                    result.setDimension(in.nextInt());
                } else if (name.equalsIgnoreCase(RADIUS)) {
                    result.setRadius(in.nextInt());
                } else if (name.equalsIgnoreCase(ICON)) {
                    result.setIcon(MinecraftAdapter.ITEM_STACK.read(in));
                } else if (name.equalsIgnoreCase(VISIBLE)) {
                    result.setVisible(QuestTaskLocation.Visibility.valueOf(in.nextString()));
                }
            }
            in.endObject();
            return result;
        }
    };

    protected static final TypeAdapter<QuestTaskMob.Mob> MOB_ADAPTER = new TypeAdapter<QuestTaskMob.Mob>() {
        private final String KILLS = "kills";
        private final String EXACT = "exact";
        private final String MOB = "mob";
        private final String ICON = "icon";
        private final String NAME = "name";

        @Override
        public void write(JsonWriter out, QuestTaskMob.Mob value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            ItemStack stack = value.getIcon();
            if (stack != null) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ICON), stack);
            }
            out.name(MOB).value(value.getMob());
            out.name(KILLS).value(value.getCount());
            out.name(EXACT).value(value.isExact());
            out.endObject();
        }

        @Override
        public QuestTaskMob.Mob read(JsonReader in) throws IOException {
            in.beginObject();
            QuestTaskMob.Mob result = ((QuestTaskMob) TASK).new Mob();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(NAME)) {
                    result.setName(in.nextString());
                } else if (name.equalsIgnoreCase(ICON)) {
                    result.setIcon(MinecraftAdapter.ITEM_STACK.read(in));
                } else if (name.equalsIgnoreCase(MOB)) {
                    result.setMob(in.nextString());
                } else if (name.equalsIgnoreCase(EXACT)) {
                    result.setExact(in.nextBoolean());
                } else if (name.equalsIgnoreCase(KILLS)) {
                    result.setCount(in.nextInt());
                }
            }
            in.endObject();
            return result;
        }
    };

    protected static final TypeAdapter<QuestTaskReputation.ReputationSetting> REPUTATION_TASK_ADAPTER = new TypeAdapter<QuestTaskReputation.ReputationSetting>() {
        private final String REPUTATION = "reputation";
        private final String LOWER = "lower";
        private final String UPPER = "upper";
        private final String INVERTED = "inverted";

        @Override
        public void write(JsonWriter out, QuestTaskReputation.ReputationSetting value) throws IOException {
            out.beginObject();
            out.name(REPUTATION).value(value.getReputation().getId());
            if (value.getLower() != null) {
                out.name(LOWER).value(value.getLower().getId());
            }
            if (value.getUpper() != null) {
                out.name(UPPER).value(value.getUpper().getId());
            }
            out.name(INVERTED).value(value.isInverted());
            out.endObject();
        }

        @Override
        public QuestTaskReputation.ReputationSetting read(JsonReader in) throws IOException {
            in.beginObject();
            Reputation reputation = null;
            ReputationMarker lower = null, upper = null;
            boolean inverted = false;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case REPUTATION:
                        reputation = Reputation.getReputation(in.nextString());
                        break;
                    case LOWER:
                        if (reputation != null)
                            lower = reputation.getMarker(in.nextInt());
                        break;
                    case UPPER:
                        if (reputation != null)
                            upper = reputation.getMarker(in.nextInt());
                        break;
                    case INVERTED:
                        inverted = in.nextBoolean();
                        break;
                    default:
                        break;
                }
            }
            return new QuestTaskReputation.ReputationSetting(reputation, lower, upper, inverted);
        }
    };

    protected static final TypeAdapter<QuestTask> TASK_ADAPTER = new TypeAdapter<QuestTask>() {
        private final String TYPE = "type";
        private final String DESCRIPTION = "description";
        private final String LONG_DESCRIPTION = "longDescription";
        private final String ITEMS = "items";
        private final String DEATHS = "deaths";
        private final String LOCATIONS = "locations";
        private final String MOBS = "mobs";
        private final String REPUTATION = "reputation";
        private final String KILLS = "kills";

        @Override
        public void write(JsonWriter out, QuestTask value) throws IOException {
            out.beginObject();
            Quest.TaskType type = Quest.TaskType.getType(value.getClass());
            out.name(TYPE).value(type.name());
            if (!value.getDescription().equals(type.getName()))
                out.name(DESCRIPTION).value(value.getDescription());
            if (!value.getLongDescription().equals(type.getDescription()))
                out.name(LONG_DESCRIPTION).value(value.getLongDescription());
            if (value instanceof QuestTaskItems) {
                out.name(ITEMS).beginArray();
                for (QuestTaskItems.ItemRequirement requirement : ((QuestTaskItems) value).getItems()) {
                    ITEM_REQUIREMENT_ADAPTER.write(out, requirement);
                }
                out.endArray();
            } else if (value instanceof QuestTaskDeath) {
                out.name(DEATHS).value(((QuestTaskDeath) value).getDeaths());
            } else if (value instanceof QuestTaskLocation) {
                out.name(LOCATIONS).beginArray();
                for (QuestTaskLocation.Location requirement : ((QuestTaskLocation) value).locations) {
                    LOCATION_ADAPTER.write(out, requirement);
                }
                out.endArray();
            } else if (value instanceof QuestTaskMob) {
                out.name(MOBS).beginArray();
                for (QuestTaskMob.Mob requirement : ((QuestTaskMob) value).mobs) {
                    MOB_ADAPTER.write(out, requirement);
                }
                out.endArray();
            } else if (value instanceof QuestTaskReputation) {
                out.name(REPUTATION).beginArray();
                for (QuestTaskReputation.ReputationSetting requirement : ((QuestTaskReputation) value).getSettings()) {
                    REPUTATION_TASK_ADAPTER.write(out, requirement);
                }
                out.endArray();
                if (value instanceof QuestTaskReputationKill) {
                    out.name(KILLS).value(((QuestTaskReputationKill) value).getKills());
                }
            }
            out.endObject();
        }

        @Override
        public QuestTask read(JsonReader in) throws IOException {
            in.beginObject();
            if (!in.nextName().equalsIgnoreCase(TYPE)) {
                throw new IOException("Tasks *MUST* start with the type");
            }
            String task = in.nextString();
            Quest.TaskType type = Quest.TaskType.valueOf(task);
            if (type == null) {
                throw new IOException("Invalid Task Type: " + task);
            }
            TASK = type.addTask(QUEST);
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(DESCRIPTION)) {
                    TASK.description = in.nextString();
                } else if (name.equalsIgnoreCase(LONG_DESCRIPTION)) {
                    TASK.setLongDescription(in.nextString());
                } else if (TASK instanceof QuestTaskItems && name.equalsIgnoreCase(ITEMS)) {
                    List<QuestTaskItems.ItemRequirement> list = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        QuestTaskItems.ItemRequirement entry = ITEM_REQUIREMENT_ADAPTER.read(in);
                        if (entry != null) list.add(entry);
                    }
                    in.endArray();
                    ((QuestTaskItems) TASK).setItems(list.toArray(new QuestTaskItems.ItemRequirement[list.size()]));
                } else if (TASK instanceof QuestTaskDeath && name.equalsIgnoreCase(DEATHS)) {
                    int death = in.nextInt();
                    ((QuestTaskDeath) TASK).setDeaths(death);
                } else if (TASK instanceof QuestTaskLocation && name.equalsIgnoreCase(LOCATIONS)) {
                    List<QuestTaskLocation.Location> list = new ArrayList<QuestTaskLocation.Location>();
                    in.beginArray();
                    while (in.hasNext()) {
                        QuestTaskLocation.Location entry = LOCATION_ADAPTER.read(in);
                        if (entry != null) list.add(entry);
                    }
                    in.endArray();
                    ((QuestTaskLocation) TASK).locations = list.toArray(new QuestTaskLocation.Location[list.size()]);
                } else if (TASK instanceof QuestTaskMob && name.equalsIgnoreCase(MOBS)) {
                    List<QuestTaskMob.Mob> list = new ArrayList<QuestTaskMob.Mob>();
                    in.beginArray();
                    while (in.hasNext()) {
                        QuestTaskMob.Mob entry = MOB_ADAPTER.read(in);
                        if (entry != null) list.add(entry);
                    }
                    in.endArray();
                    ((QuestTaskMob) TASK).mobs = list.toArray(new QuestTaskMob.Mob[list.size()]);
                } else if (TASK instanceof QuestTaskReputation && name.equalsIgnoreCase(REPUTATION)) {
                    List<ReputationSettingConstructor> list = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        ReputationSettingConstructor constructor = ReputationSettingConstructor.read(in);
                        if (constructor != null) {
                            list.add(constructor);
                        }
                    }
                    in.endArray();
                    taskReputationListMap.put((QuestTaskReputation) TASK, list);
//                    ReflectionHelper.setPrivateValue(QuestTaskReputation.class, (QuestTaskReputation) TASK, list.toArray(new QuestTaskReputation.ReputationSetting[list.size()]), "settings");
                } else if (name.equalsIgnoreCase(KILLS) && TASK instanceof QuestTaskReputationKill) {
                    ((QuestTaskReputationKill) TASK).setKills(in.nextInt());
                }
            }
            in.endObject();
            return null;
        }
    };

    protected static class ReputationSettingConstructor {
        private int upper, lower;
        String reputation;
        boolean inverted;

        private ReputationSettingConstructor(String reputation, int lower, int upper, boolean inverted) {
            this.reputation = reputation;
            this.lower = lower;
            this.upper = upper;
            this.inverted = inverted;
        }

        public QuestTaskReputation.ReputationSetting constructReuptationSetting() {
            Reputation reputation = Reputation.getReputations().get(this.reputation);
            if (reputation != null) {
                ReputationMarker lower = null, upper = null;
                if (this.lower >= 0 && this.lower < reputation.getMarkerCount())
                    lower = reputation.getMarker(this.lower);
                if (this.upper >= 0 && this.upper < reputation.getMarkerCount())
                    upper = reputation.getMarker(this.lower);
                return new QuestTaskReputation.ReputationSetting(reputation, lower, upper, inverted);
            }
            return null;
        }

        private static final String REPUTATION = "reputation";
        private static final String LOWER = "lower";
        private static final String UPPER = "upper";
        private static final String INVERTED = "inverted";

        public static ReputationSettingConstructor read(JsonReader in) throws IOException {
            in.beginObject();
            int low = Integer.MIN_VALUE, high = Integer.MIN_VALUE;
            String reputation = null;
            boolean inverted = false;
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(REPUTATION)) {
                    reputation = in.nextString();
                } else if (name.equalsIgnoreCase(UPPER)) {
                    high = in.nextInt();
                } else if (name.equalsIgnoreCase(LOWER)) {
                    low = in.nextInt();
                } else if (name.equalsIgnoreCase(INVERTED)) {
                    inverted = in.nextBoolean();
                }
            }
            in.endObject();
            if (reputation == null) return null;
            return new ReputationSettingConstructor(reputation, low, high, inverted);
        }
    }

    public static final TypeAdapter<QuestDataTask> QUEST_DATA_TASK_ADAPTER = new TypeAdapter<QuestDataTask>() {
        private static final String TYPE = "type";

        @Override
        public void write(JsonWriter out, QuestDataTask value) throws IOException {
            out.beginObject();
            out.name(TYPE).value(String.valueOf(value.getDataType()));
            value.write(out);
            out.endObject();
        }

        @Override
        public QuestDataTask read(JsonReader in) throws IOException {
            in.beginObject();
            QuestDataTask dataTask = null;
            if (in.hasNext() && in.nextName().equals(TYPE))
                dataTask = QuestDataType.valueOf(in.nextString()).construct(in);
            in.endObject();
            return dataTask; // should never be null
        }
    };

    public enum QuestDataType {
        GENERIC(QuestDataTask::construct),
        DEATH(QuestDataTaskDeath::construct),
        ITEMS(QuestDataTaskItems::construct),
        LOCATION(QuestDataTaskLocation::construct),
        MOB(QuestDataTaskMob::construct),
        REPUTATION_KILL(QuestDataTaskReputationKill::construct);

        private Function<JsonReader, QuestDataTask> func;

        QuestDataType(Function<JsonReader, QuestDataTask> func) {
            this.func = func;
        }

        public QuestDataTask construct(JsonReader in) {
            return func.apply(in);
        }
    }
}
