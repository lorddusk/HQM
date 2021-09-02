package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Either;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.ItemPrecision;
import hardcorequesting.common.quests.data.*;
import hardcorequesting.common.quests.task.CompleteQuestTask;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.reputation.ReputationMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static hardcorequesting.common.io.adapter.QuestAdapter.QUEST;

public class QuestTaskAdapter {
    
    public static final Adapter<TaskData> QUEST_DATA_TASK_ADAPTER = new Adapter<TaskData>() {
        private static final String TYPE = "type";
        
        @Override
        public JsonElement serialize(TaskData src) {
            JsonObjectBuilder builder = object()
                    .add(TYPE, String.valueOf(src.getDataType()));
            src.write(builder);
            return builder.build();
        }
        
        @Override
        public TaskData deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            return QuestDataType.valueOf(GsonHelper.getAsString(object, TYPE)).construct(object);
        }
    };
    public static final TypeAdapter<ItemRequirementTask.Part> ITEM_REQUIREMENT_ADAPTER = new TypeAdapter<ItemRequirementTask.Part>() {
        private static final String ITEM = "item";
        private static final String FLUID = "fluid";
        private static final String REQUIRED = "required";
        private static final String PRECISION = "precision";
        
        @Override
        public void write(JsonWriter out, ItemRequirementTask.Part value) throws IOException {
            Optional<ItemStack> stack = value.stack.left();
            Optional<FluidStack> fluid = value.stack.right();
            int required = value.required;
            ItemPrecision precision = value.getPrecision();
            out.beginObject();
            if (stack.isPresent()) {
                // Item stack count doesn't track task requirement; "required" does that.
                // So we are fine to keep the stack count at 1.
                MinecraftAdapter.ICON_ITEM_STACK.write(out.name(ITEM), stack.get());
            } else if (fluid.isPresent()) {
                MinecraftAdapter.FLUID.write(out.name(FLUID), fluid.get());
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
        public ItemRequirementTask.Part read(JsonReader in) throws IOException {
            in.beginObject();
            ItemStack itemStack = ItemStack.EMPTY;
            FluidStack fluidVolume = null;
            int required = 1;
            ItemPrecision precision = ItemPrecision.PRECISE;
            while (in.hasNext()) {
                String next = in.nextName();
                if (next.equalsIgnoreCase(ITEM)) {
                    itemStack = MinecraftAdapter.ICON_ITEM_STACK.read(in);
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
            ItemRequirementTask.Part result;
            if (!itemStack.isEmpty()) {
                result = new ItemRequirementTask.Part(itemStack, required);
            } else if (fluidVolume != null) {
                result = new ItemRequirementTask.Part(fluidVolume, required);
            } else {
                return null;
            }
            result.setPrecision(precision);
            return result;
        }
    };
    public static final Adapter<VisitLocationTask.Part> LOCATION_ADAPTER = new Adapter<>() {
        private static final String X = "x";
        private static final String Y = "y";
        private static final String Z = "z";
        private static final String DIM = "dim";
        private static final String ICON = "icon";
        private static final String FLUID_ICON = "fluid_icon";
        private static final String RADIUS = "radius";
        private static final String VISIBLE = "visible";
        private static final String NAME = "name";
    
        @Override
        public JsonElement serialize(VisitLocationTask.Part src) {
            return object()
                    .add(NAME, src.getName())
                    .add(X, src.getPosition().getX())
                    .add(Y, src.getPosition().getY())
                    .add(Z, src.getPosition().getZ())
                    .add(DIM, src.getDimension())
                    .add(RADIUS, src.getRadius())
                    .add(VISIBLE, src.getVisibility().name())
                    .use(builder -> {
                        Optional<ItemStack> item = src.getIconStack().left();
                        Optional<FluidStack> fluid = src.getIconStack().right();
                        item.ifPresent(itemStack -> builder.add(ICON, MinecraftAdapter.ICON_ITEM_STACK.serialize(itemStack)));
                        fluid.ifPresent(fluidStack -> builder.add(FLUID_ICON, MinecraftAdapter.FLUID.serialize(fluidStack)));
                    })
                    .build();
        }
    
        @Override
        public VisitLocationTask.Part deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            VisitLocationTask.Part result = new VisitLocationTask.Part();
            result.setName(GsonHelper.getAsString(object, NAME));
            result.setPosition(new BlockPos(GsonHelper.getAsInt(object, X), GsonHelper.getAsInt(object, Y), GsonHelper.getAsInt(object, Z)));
            result.setDimension(GsonHelper.getAsString(object, DIM));
            result.setRadius(GsonHelper.getAsInt(object, RADIUS));
            result.setVisibility(VisitLocationTask.Visibility.valueOf(GsonHelper.getAsString(object, VISIBLE, result.getVisibility().name())));
            if (object.has(ICON)) {
                result.setIconStack(Either.left(MinecraftAdapter.ICON_ITEM_STACK.deserialize(object.get(ICON))));
            }
            if(object.has(FLUID_ICON)) {
                result.setIconStack(Either.right(MinecraftAdapter.FLUID.deserialize(object.get(FLUID_ICON))));
            }
            return result;
        }
    };
    public static final Adapter<ReputationTask.Part> REPUTATION_TASK_ADAPTER = new Adapter<>() {
        private static final String REPUTATION = "reputation";
        private static final String LOWER = "lower";
        private static final String UPPER = "upper";
        private static final String INVERTED = "inverted";
    
        @Override
        public JsonElement serialize(ReputationTask.Part src) {
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
        public ReputationTask.Part deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            Reputation reputation = ReputationManager.getInstance().getReputation(GsonHelper.getAsString(object, REPUTATION, null));
            ReputationMarker lower = null;
            ReputationMarker upper = null;
            if (object.has(LOWER)) lower = reputation.getMarker(GsonHelper.getAsInt(object, LOWER));
            if (object.has(UPPER)) upper = reputation.getMarker(GsonHelper.getAsInt(object, UPPER));
            if (reputation == null) return null;
            return new ReputationTask.Part(
                    reputation,
                    lower,
                    upper,
                    GsonHelper.getAsBoolean(object, INVERTED, false)
            );
        }
    };
    
    public static final TypeAdapter<TameMobsTask.Part> TAME_ADAPTER = new TypeAdapter<>() {
        private static final String TAMES = "tames";
        private static final String EXACT = "exact";
        private static final String TAME = "tame";
        private static final String ICON = "icon";
        private static final String FLUID_ICON = "fluid_icon";
        private static final String NAME = "name";
    
        @Override
        public void write(JsonWriter out, TameMobsTask.Part value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            Optional<ItemStack> item = value.getIconStack().left();
            Optional<FluidStack> fluid = value.getIconStack().right();
            if (item.isPresent()) {
                MinecraftAdapter.ICON_ITEM_STACK.write(out.name(ICON), item.get());
            }
            if (fluid.isPresent()) {
                MinecraftAdapter.FLUID.write(out.name(FLUID_ICON), fluid.get());
            }
            out.name(TAME).value(value.getTame());
            out.name(TAMES).value(value.getCount());
            out.endObject();
        }
    
        @Override
        public TameMobsTask.Part read(JsonReader in) throws IOException {
            in.beginObject();
            TameMobsTask.Part result = new TameMobsTask.Part();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(NAME)) {
                    result.setName(in.nextString());
                } else if (name.equalsIgnoreCase(ICON)) {
                    ItemStack icon = MinecraftAdapter.ICON_ITEM_STACK.read(in);
                    if (icon != null) {
                        result.setIconStack(Either.left(icon));
                    }
                } else if(name.equalsIgnoreCase(FLUID_ICON)) {
                    FluidStack fluid = MinecraftAdapter.FLUID.read(in);
                    if (fluid != null) {
                        result.setIconStack(Either.right(fluid));
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
    
    public static final TypeAdapter<GetAdvancementTask.Part> ADVANCEMENT_TASK_ADAPTER = new TypeAdapter<>() {
        private final String ICON = "icon";
        private static final String FLUID_ICON = "fluid_icon";
        private final String VISIBLE = "visible";
        private final String NAME = "name";
        private final String ADV_NAME = "adv_name";
    
        @Override
        public void write(JsonWriter out, GetAdvancementTask.Part value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            Optional<ItemStack> item = value.getIconStack().left();
            Optional<FluidStack> fluid = value.getIconStack().right();
            if (item.isPresent()) {
                MinecraftAdapter.ICON_ITEM_STACK.write(out.name(ICON), item.get());
            }
            if (fluid.isPresent()) {
                MinecraftAdapter.FLUID.write(out.name(FLUID_ICON), fluid.get());
            }
            if (value.getAdvancement() != null) {
                out.name(ADV_NAME).value(value.getAdvancement());
            }
            if (value.getVisible() != GetAdvancementTask.Visibility.FULL)
                out.name(VISIBLE).value(value.getVisible().name());
            out.endObject();
        }
    
        @Override
        public GetAdvancementTask.Part read(JsonReader in) throws IOException {
            in.beginObject();
            GetAdvancementTask.Part result = new GetAdvancementTask.Part();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(NAME)) {
                    result.setName(in.nextString());
                } else if (name.equalsIgnoreCase(ICON)) {
                    ItemStack icon = MinecraftAdapter.ICON_ITEM_STACK.read(in);
                    if (icon != null) {
                        result.setIconStack(Either.left(icon));
                    }
                } else if (name.equalsIgnoreCase(FLUID_ICON)) {
                    FluidStack fluid = MinecraftAdapter.FLUID.read(in);
                    if (fluid != null) {
                        result.setIconStack(Either.right(fluid));
                    }
                } else if (name.equalsIgnoreCase(ADV_NAME)) {
                    result.setAdvancement(in.nextString());
                } else if (name.equalsIgnoreCase(VISIBLE)) {
                    result.setVisible(GetAdvancementTask.Visibility.valueOf(in.nextString()));
                }
            }
            in.endObject();
            return result;
        }
    };
    
    public static final TypeAdapter<CompleteQuestTask.Part> QUEST_COMPLETED_ADAPTER = new TypeAdapter<CompleteQuestTask.Part>() {
        private final String VISIBLE = "visible";
        private final String QUEST_UUID = "quest_uuid";
        
        @Override
        public void write(JsonWriter out, CompleteQuestTask.Part value) throws IOException {
            out.beginObject();
            if (value.getQuest() != null) {
                out.name(QUEST_UUID).value(value.getQuestId().toString());
            }
            out.endObject();
        }
        
        @Override
        public CompleteQuestTask.Part read(JsonReader in) throws IOException {
            in.beginObject();
            CompleteQuestTask.Part result = new CompleteQuestTask.Part();
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
    
    public static final Adapter<KillMobsTask.Part> MOB_ADAPTER = new Adapter<>() {
        private static final String KILLS = "kills";
        private static final String MOB = "mob";
        private static final String ICON = "icon";
        private static final String FLUID_ICON = "fluid_icon";
        private static final String NAME = "name";
    
        @Override
        public JsonElement serialize(KillMobsTask.Part src) {
            return object()
                    .add(NAME, src.getName())
                    .use(builder -> {
                        Optional<ItemStack> item = src.getIconStack().left();
                        Optional<FluidStack> fluid = src.getIconStack().right();
                        item.ifPresent(itemStack -> builder.add(ICON, MinecraftAdapter.ICON_ITEM_STACK.toJsonTree(itemStack)));
                        fluid.ifPresent(fluidStack -> builder.add(FLUID_ICON, MinecraftAdapter.FLUID.toJsonTree(fluidStack)));
                    })
                    .add(MOB, src.getMob().toString())
                    .add(KILLS, src.getCount())
                    .build();
        }
    
        @Override
        public KillMobsTask.Part deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            KillMobsTask.Part result = new KillMobsTask.Part();
            result.setName(GsonHelper.getAsString(object, NAME, result.getName()));
            result.setMob(new ResourceLocation(GsonHelper.getAsString(object, MOB, result.getMob().toString())));
            result.setCount(GsonHelper.getAsInt(object, KILLS, result.getCount()));
            if (object.has(ICON)) {
                ItemStack icon = MinecraftAdapter.ICON_ITEM_STACK.deserialize(GsonHelper.getAsJsonObject(object, ICON));
                result.setIconStack(Either.left(icon));
            }
            if(object.has(FLUID_ICON)) {
                FluidStack fluid = MinecraftAdapter.FLUID.deserialize(GsonHelper.getAsJsonObject(object, FLUID_ICON));
                result.setIconStack(Either.right(fluid));
            }
            return result;
        }
    };
    public static Map<ReputationTask<?>, List<ReputationSettingConstructor>> taskReputationListMap = new HashMap<>();
    protected static final Adapter<QuestTask<?>> TASK_ADAPTER = new Adapter<>() {
        private static final String TYPE = "type";
        private static final String DESCRIPTION = "description";
        private static final String LONG_DESCRIPTION = "longDescription";
    
        @Override
        public JsonElement serialize(QuestTask<?> src) {
            TaskType type = TaskType.getType(src.getClass());
        
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
        public QuestTask<?> deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            TaskType type = TaskType.valueOf(GsonHelper.getAsString(object, TYPE));
            QuestTask<?> TASK = type.addTask(QUEST);
            if (object.has(DESCRIPTION)) TASK.setDescription(GsonHelper.getAsString(object, DESCRIPTION));
            if (object.has(LONG_DESCRIPTION)) TASK.setLongDescription(GsonHelper.getAsString(object, LONG_DESCRIPTION));
            TASK.read(object);
            return TASK;
        }
    };
    
    public enum QuestDataType {
        GENERIC(TaskData::construct),
        DEATH(DeathTaskData::construct),
        ITEMS(ItemsTaskData::construct),
        LOCATION(LocationTaskData::construct),
        MOB(MobTaskData::construct),
        REPUTATION_KILL(ReputationKillTaskData::construct),
        TAME(TameTaskData::construct),
        ADVANCEMENT(AdvancementTaskData::construct),
        COMPLETED(CompleteQuestTaskData::construct);
        
        private Function<JsonObject, TaskData> func;
        
        QuestDataType(Function<JsonObject, TaskData> func) {
            this.func = func;
        }
        
        public TaskData construct(JsonObject in) {
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
        
        public ReputationTask.Part constructReputationSetting() {
            Reputation reputation = ReputationManager.getInstance().getReputations().get(this.reputation);
            if (reputation != null) {
                ReputationMarker lower = null, upper = null;
                if (this.lower >= 0 && this.lower < reputation.getMarkerCount())
                    lower = reputation.getMarker(this.lower);
                if (this.upper >= 0 && this.upper < reputation.getMarkerCount())
                    upper = reputation.getMarker(this.upper);
                return new ReputationTask.Part(reputation, lower, upper, inverted);
            }
            return null;
        }
    }
}
