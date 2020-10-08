package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.*;
import hardcorequesting.common.quests.task.*;
import hardcorequesting.common.quests.task.QuestTaskMob.Mob;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.reputation.ReputationMarker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static hardcorequesting.common.io.adapter.QuestAdapter.QUEST;

public class QuestTaskAdapter {
    
    public static final Adapter<QuestDataTask> QUEST_DATA_TASK_ADAPTER = new Adapter<QuestDataTask>() {
        private static final String TYPE = "type";
        
        @Override
        public JsonElement serialize(QuestDataTask src) {
            JsonObjectBuilder builder = object()
                    .add(TYPE, String.valueOf(src.getDataType()));
            src.write(builder);
            return builder.build();
        }
        
        @Override
        public QuestDataTask deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            return QuestDataType.valueOf(GsonHelper.getAsString(object, TYPE)).construct(object);
        }
    };
    public static final TypeAdapter<QuestTaskItems.ItemRequirement> ITEM_REQUIREMENT_ADAPTER = new TypeAdapter<QuestTaskItems.ItemRequirement>() {
        private static final String ITEM = "item";
        private static final String FLUID = "fluid";
        private static final String REQUIRED = "required";
        private static final String PRECISION = "precision";
        
        @Override
        public void write(JsonWriter out, QuestTaskItems.ItemRequirement value) throws IOException {
            ItemStack stack = value.getStack();
            FluidStack fluid = value.fluid;
            int required = value.required;
            ItemPrecision precision = value.getPrecision();
            out.beginObject();
            if (value.hasItem && !stack.isEmpty()) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ITEM), stack);
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
            ItemStack itemStack = ItemStack.EMPTY;
            FluidStack fluidVolume = null;
            int required = 1;
            ItemPrecision precision = ItemPrecision.PRECISE;
            while (in.hasNext()) {
                String next = in.nextName();
                if (next.equalsIgnoreCase(ITEM)) {
                    itemStack = MinecraftAdapter.ITEM_STACK.read(in);
                } else if (next.equalsIgnoreCase(FLUID)) {
                    fluidVolume = MinecraftAdapter.FLUID.read(in);
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
            QuestTaskItems.ItemRequirement result;
            if (!itemStack.isEmpty()) {
                result = new QuestTaskItems.ItemRequirement(itemStack, required);
            } else if (fluidVolume != null) {
                result = new QuestTaskItems.ItemRequirement(fluidVolume, required);
            } else {
                return null;
            }
            result.setPrecision(precision);
            return result;
        }
    };
    public static final Adapter<QuestTaskLocation.Location> LOCATION_ADAPTER = new Adapter<QuestTaskLocation.Location>() {
        private static final String X = "x";
        private static final String Y = "y";
        private static final String Z = "z";
        private static final String DIM = "dim";
        private static final String ICON = "icon";
        private static final String RADIUS = "radius";
        private static final String VISIBLE = "visible";
        private static final String NAME = "name";
        
        @Override
        public JsonElement serialize(QuestTaskLocation.Location src) {
            return object()
                    .add(NAME, src.getName())
                    .add(X, src.getX())
                    .add(Y, src.getY())
                    .add(Z, src.getZ())
                    .add(DIM, src.getDimension())
                    .add(RADIUS, src.getRadius())
                    .add(VISIBLE, src.getVisibility().name())
                    .use(builder -> {
                        ItemStack stack = src.getIconStack();
                        if (stack != null) {
                            builder.add(ICON, MinecraftAdapter.ITEM_STACK.serialize(stack));
                        } else {
                            builder.add(ICON, MinecraftAdapter.ITEM_STACK.serialize(ItemStack.EMPTY));
                        }
                    })
                    .build();
        }
        
        @Override
        public QuestTaskLocation.Location deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            QuestTaskLocation.Location result = new QuestTaskLocation.Location();
            result.setName(GsonHelper.getAsString(object, NAME));
            result.setX(GsonHelper.getAsInt(object, X));
            result.setY(GsonHelper.getAsInt(object, Y));
            result.setZ(GsonHelper.getAsInt(object, Z));
            result.setDimension(GsonHelper.getAsString(object, DIM));
            result.setRadius(GsonHelper.getAsInt(object, RADIUS));
            result.setVisibility(QuestTaskLocation.Visibility.valueOf(GsonHelper.getAsString(object, VISIBLE, result.getVisibility().name())));
            if (object.has(ICON)) {
                result.setIconStack(MinecraftAdapter.ITEM_STACK.deserialize(object.get(ICON)));
            }
            return result;
        }
    };
    public static final Adapter<QuestTaskReputation.ReputationSetting> REPUTATION_TASK_ADAPTER = new Adapter<QuestTaskReputation.ReputationSetting>() {
        private static final String REPUTATION = "reputation";
        private static final String LOWER = "lower";
        private static final String UPPER = "upper";
        private static final String INVERTED = "inverted";
        
        @Override
        public JsonElement serialize(QuestTaskReputation.ReputationSetting src) {
            JsonObjectBuilder builder = object()
                    .add(REPUTATION, src.getReputation().getId())
                    .add(INVERTED, src.isInverted());
            if (src.getLower() != null)
                builder.add(LOWER, src.getLower().getId());
            if (src.getUpper() != null)
                builder.add(UPPER, src.getUpper().getId());
            return builder.build();
        }
        
        @Override
        public QuestTaskReputation.ReputationSetting deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            Reputation reputation = ReputationManager.getInstance().getReputation(GsonHelper.getAsString(object, REPUTATION, null));
            ReputationMarker lower = null;
            ReputationMarker upper = null;
            if (object.has(LOWER)) lower = reputation.getMarker(GsonHelper.getAsInt(object, LOWER));
            if (object.has(UPPER)) upper = reputation.getMarker(GsonHelper.getAsInt(object, UPPER));
            if (reputation == null) return null;
            return new QuestTaskReputation.ReputationSetting(
                    reputation,
                    lower,
                    upper,
                    GsonHelper.getAsBoolean(object, INVERTED, false)
            );
        }
    };
    
    public static final TypeAdapter<QuestTaskTame.Tame> TAME_ADAPTER = new TypeAdapter<QuestTaskTame.Tame>() {
        private static final String TAMES = "tames";
        private static final String EXACT = "exact";
        private static final String TAME = "tame";
        private static final String ICON = "icon";
        private static final String NAME = "name";
        
        @Override
        public void write(JsonWriter out, QuestTaskTame.Tame value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            ItemStack stack = value.getIconStack();
            if (stack != null) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ICON), stack);
            }
            out.name(TAME).value(value.getTame());
            out.name(TAMES).value(value.getCount());
            out.endObject();
        }
        
        @Override
        public QuestTaskTame.Tame read(JsonReader in) throws IOException {
            in.beginObject();
            QuestTaskTame.Tame result = new QuestTaskTame.Tame();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(NAME)) {
                    result.setName(in.nextString());
                } else if (name.equalsIgnoreCase(ICON)) {
                    ItemStack icon = MinecraftAdapter.ITEM_STACK.read(in);
                    if (!icon.isEmpty()) {
                        result.setIconStack(icon);
                    }
                } else if (name.equalsIgnoreCase(TAME)) {
                    result.setTame(in.nextString());
                } else if (name.equalsIgnoreCase(TAMES)) {
                    result.setCount(in.nextInt());
                }
            }
            in.endObject();
            return result;
        }
    };
    
    public static final TypeAdapter<QuestTaskAdvancement.AdvancementTask> ADVANCEMENT_TASK_ADAPTER = new TypeAdapter<QuestTaskAdvancement.AdvancementTask>() {
        private final String ICON = "icon";
        private final String VISIBLE = "visible";
        private final String NAME = "name";
        private final String ADV_NAME = "adv_name";
        
        @Override
        public void write(JsonWriter out, QuestTaskAdvancement.AdvancementTask value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            ItemStack stack = value.getIconStack();
            if (stack != null) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ICON), stack);
            }
            if (value.getAdvancement() != null) {
                out.name(ADV_NAME).value(value.getAdvancement());
            }
            if (value.getVisible() != QuestTaskAdvancement.Visibility.FULL)
                out.name(VISIBLE).value(value.getVisible().name());
            out.endObject();
        }
        
        @Override
        public QuestTaskAdvancement.AdvancementTask read(JsonReader in) throws IOException {
            in.beginObject();
            QuestTaskAdvancement.AdvancementTask result = new QuestTaskAdvancement.AdvancementTask();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(NAME)) {
                    result.setName(in.nextString());
                } else if (name.equalsIgnoreCase(ICON)) {
                    result.setIconStack(MinecraftAdapter.ITEM_STACK.read(in));
                } else if (name.equalsIgnoreCase(ADV_NAME)) {
                    result.setAdvancement(in.nextString());
                } else if (name.equalsIgnoreCase(VISIBLE)) {
                    result.setVisible(QuestTaskAdvancement.Visibility.valueOf(in.nextString()));
                }
            }
            in.endObject();
            return result;
        }
    };
    
    public static final TypeAdapter<QuestTaskCompleted.CompletedQuestTask> QUEST_COMPLETED_ADAPTER = new TypeAdapter<QuestTaskCompleted.CompletedQuestTask>() {
        private final String VISIBLE = "visible";
        private final String QUEST_UUID = "quest_uuid";
        
        @Override
        public void write(JsonWriter out, QuestTaskCompleted.CompletedQuestTask value) throws IOException {
            out.beginObject();
            if (value.getQuest() != null) {
                out.name(QUEST_UUID).value(value.getQuestId().toString());
            }
            out.endObject();
        }
        
        @Override
        public QuestTaskCompleted.CompletedQuestTask read(JsonReader in) throws IOException {
            in.beginObject();
            QuestTaskCompleted.CompletedQuestTask result = new QuestTaskCompleted.CompletedQuestTask();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(QUEST_UUID)) {
                    result.setQuest(UUID.fromString(in.nextString()));
                }
            }
            in.endObject();
            return result;
        }
    };
    
    public static final Adapter<Mob> MOB_ADAPTER = new Adapter<Mob>() {
        private static final String KILLS = "kills";
        private static final String MOB = "mob";
        private static final String ICON = "icon";
        private static final String NAME = "name";
        
        @Override
        public JsonElement serialize(Mob src) {
            return object()
                    .add(NAME, src.getName())
                    .use(builder -> {
                        ItemStack stack = src.getIconStack();
                        if (stack != null) {
                            builder.add(ICON, MinecraftAdapter.ITEM_STACK.toJsonTree(stack));
                        }
                    })
                    .add(MOB, src.getMob().toString())
                    .add(KILLS, src.getCount())
                    .build();
        }
        
        @Override
        public Mob deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            Mob result = new Mob();
            result.setName(GsonHelper.getAsString(object, NAME, result.getName()));
            result.setMob(new ResourceLocation(GsonHelper.getAsString(object, MOB, result.getMob().toString())));
            result.setCount(GsonHelper.getAsInt(object, KILLS, result.getCount()));
            if (object.has(ICON)) {
                ItemStack icon = MinecraftAdapter.ITEM_STACK.deserialize(GsonHelper.getAsJsonObject(object, ICON));
                if (!icon.isEmpty()) {
                    result.setIconStack(icon);
                }
            }
            return result;
        }
    };
    public static Map<QuestTaskReputation, List<ReputationSettingConstructor>> taskReputationListMap = new HashMap<>();
    protected static final Adapter<QuestTask> TASK_ADAPTER = new Adapter<QuestTask>() {
        private static final String TYPE = "type";
        private static final String DESCRIPTION = "description";
        private static final String LONG_DESCRIPTION = "longDescription";
        
        @Override
        public JsonElement serialize(QuestTask src) {
            Quest.TaskType type = Quest.TaskType.getType(src.getClass());
            
            JsonObjectBuilder builder = object()
                    .add(TYPE, type.name());
            if (!src.getDescription().equals(type.getName()))
                builder.add(DESCRIPTION, src.getDescription());
            if (!src.getLongDescription().equals(type.getDescription()))
                builder.add(LONG_DESCRIPTION, src.getLongDescription());
            src.write(builder);
            return builder.build();
        }
        
        @Override
        public QuestTask deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            Quest.TaskType type = Quest.TaskType.valueOf(GsonHelper.getAsString(object, TYPE));
            QuestTask TASK = type.addTask(QUEST);
            if (object.has(DESCRIPTION)) TASK.setDescription(GsonHelper.getAsString(object, DESCRIPTION));
            if (object.has(LONG_DESCRIPTION)) TASK.setLongDescription(GsonHelper.getAsString(object, LONG_DESCRIPTION));
            TASK.read(object);
            return TASK;
        }
    };
    
    public enum QuestDataType {
        GENERIC(QuestDataTask::construct),
        DEATH(QuestDataTaskDeath::construct),
        ITEMS(QuestDataTaskItems::construct),
        LOCATION(QuestDataTaskLocation::construct),
        MOB(QuestDataTaskMob::construct),
        REPUTATION_KILL(QuestDataTaskReputationKill::construct),
        TAME(QuestDataTaskTame::construct),
        ADVANCEMENT(QuestDataTaskAdvancement::construct),
        COMPLETED(QuestDataTaskCompleted::construct);
        
        private Function<JsonObject, QuestDataTask> func;
        
        QuestDataType(Function<JsonObject, QuestDataTask> func) {
            this.func = func;
        }
        
        public QuestDataTask construct(JsonObject in) {
            return func.apply(in);
        }
    }
    
    public static class ReputationSettingConstructor {
        private static final String REPUTATION = "reputation";
        private static final String LOWER = "lower";
        private static final String UPPER = "upper";
        private static final String INVERTED = "inverted";
        String reputation;
        boolean inverted;
        private int upper, lower;
        
        private ReputationSettingConstructor(String reputation, int lower, int upper, boolean inverted) {
            this.reputation = reputation;
            this.lower = lower;
            this.upper = upper;
            this.inverted = inverted;
        }
        
        public static ReputationSettingConstructor read(JsonElement in) {
            JsonObject object = in.getAsJsonObject();
            String reputation = GsonHelper.getAsString(object, REPUTATION, null);
            if (reputation == null) return null;
            return new ReputationSettingConstructor(
                    reputation,
                    GsonHelper.getAsInt(object, LOWER, Integer.MIN_VALUE),
                    GsonHelper.getAsInt(object, UPPER, Integer.MIN_VALUE),
                    GsonHelper.getAsBoolean(object, INVERTED, false)
            );
        }
        
        public QuestTaskReputation.ReputationSetting constructReputationSetting() {
            Reputation reputation = ReputationManager.getInstance().getReputations().get(this.reputation);
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
    }
}
