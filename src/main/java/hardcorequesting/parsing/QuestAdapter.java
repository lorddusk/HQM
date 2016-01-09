package hardcorequesting.parsing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.SaveHelper;
import hardcorequesting.quests.*;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationBar;
import hardcorequesting.reputation.ReputationMarker;
import hardcorequesting.reward.ReputationReward;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestAdapter {
    public static Quest QUEST;
    public static QuestTask TASK;
    public static int QUEST_ID;
    private static List<ReputationBar> reputationBarList = new ArrayList<>();
    private static Map<Quest, List<Integer>> requirementMapping = new HashMap<>();
    private static Map<Quest, List<String>> prerequisiteMapping = new HashMap<>();
    private static Map<Quest, List<Integer>> optionMapping = new HashMap<>();
    private static Map<Quest, List<String>> optionLinkMapping = new HashMap<>();
    private static Map<ReputationReward, Integer> reputationRewardMapping = new HashMap<>();
    private static Map<QuestTaskReputation, List<ReputationSettingConstructor>> taskReputationListMap = new HashMap<>();

    private static final TypeAdapter<QuestTaskItems.ItemRequirement> ITEM_REQUIREMENT_ADAPTER = new TypeAdapter<QuestTaskItems.ItemRequirement>() {
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

    private static final TypeAdapter<QuestTaskLocation.Location> LOCATION_ADAPTER = new TypeAdapter<QuestTaskLocation.Location>() {
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

    private static final TypeAdapter<QuestTaskMob.Mob> MOB_ADAPTER = new TypeAdapter<QuestTaskMob.Mob>() {
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

    private static final TypeAdapter<QuestTaskReputation.ReputationSetting> REPUTATION_TASK_ADAPTER = new TypeAdapter<QuestTaskReputation.ReputationSetting>() {
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
            return null;
        }
    };

    private static final TypeAdapter<QuestTask> TASK_ADAPTER = new TypeAdapter<QuestTask>() {
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

    private static class ReputationSettingConstructor {
        private int upper, lower, reputation;
        boolean inverted;

        private ReputationSettingConstructor(int reputation, int lower, int upper, boolean inverted) {
            this.reputation = reputation;
            this.lower = lower;
            this.upper = upper;
            this.inverted = inverted;
        }

        public QuestTaskReputation.ReputationSetting constructReuptationSetting() {
            if (reputation >= 0 && reputation < Reputation.getReputationList().size()) {
                Reputation reputation = Reputation.getReputationList().get(this.reputation);
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
            int low = Integer.MIN_VALUE, high = Integer.MIN_VALUE, reputation = -1;
            boolean inverted = false;
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(REPUTATION)) {
                    reputation = in.nextInt();
                } else if (name.equalsIgnoreCase(UPPER)) {
                    high = in.nextInt();
                } else if (name.equalsIgnoreCase(LOWER)) {
                    low = in.nextInt();
                } else if (name.equalsIgnoreCase(INVERTED)) {
                    inverted = in.nextBoolean();
                }
            }
            if (reputation < 0) {
                return null;
            }
            in.endObject();
            return new ReputationSettingConstructor(reputation, low, high, inverted);
        }
    }

    private static final TypeAdapter<RepeatInfo> REPEAT_INFO_ADAPTER = new TypeAdapter<RepeatInfo>() {
        private final String TYPE = "type";
        private final String HOURS = "hours";
        private final String DAYS = "days";

        @Override
        public void write(JsonWriter out, RepeatInfo value) throws IOException {
            out.beginObject();
            out.name(TYPE).value(value.getType().name());
            if (value.getType().isUseTime()) {
                out.name(DAYS).value(value.getDays());
                out.name(HOURS).value(value.getHours());
            }
            out.endObject();
        }

        @Override
        public RepeatInfo read(JsonReader in) throws IOException {
            RepeatType type = RepeatType.NONE;
            int days = 0, hours = 0;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case TYPE:
                        type = RepeatType.valueOf(in.nextString());
                        break;
                    case HOURS:
                        hours = in.nextInt();
                        break;
                    case DAYS:
                        days = in.nextInt();
                }
            }
            in.endObject();
            return new RepeatInfo(type, days, hours);
        }
    };

    private static final TypeAdapter<ReputationReward> REPUTATION_REWARD_ADAPTER = new TypeAdapter<ReputationReward>() {
        private final String REPUTATION = "reputation";
        private final String VALUE = "value";

        @Override
        public void write(JsonWriter out, ReputationReward value) throws IOException {
            out.beginObject();
            out.name(REPUTATION).value(value.getReward().getId());
            out.name(VALUE).value(value.getValue());
            out.endObject();
        }

        @Override
        public ReputationReward read(JsonReader in) throws IOException {
            in.beginObject();
            int rep = 0, val = 0;
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case REPUTATION:
                        rep = in.nextInt();
                        break;
                    case VALUE:
                        val = in.nextInt();
                        break;
                }
            }
            in.endObject();
            ReputationReward result = new ReputationReward(null, val);
            reputationRewardMapping.put(result, rep);
            return result;
        }
    };

    public static final TypeAdapter<Quest> QUEST_ADAPTER = new TypeAdapter<Quest>() {
        private final String NAME = "name";
        private final String DESCRIPTION = "description";
        private final String X = "x";
        private final String Y = "y";
        private final String ICON = "icon";
        private final String BIG_ICON = "bigicon";
        private final String REQUIREMENTS = "requirements";
        private final String PREREQUISITES = "prerequisites";
        private final String OPTIONS = "options";
        private final String OPTIONLINKS = "optionlinks";
        private final String REPEAT = "repeat";
        private final String TRIGGER = "trigger";
        private final String TRIGGER_TASKS = "triggertasks";
        private final String PARENT_REQUIREMENT = "parentrequirement";
        private final String TASKS = "tasks";
        private final String REWARDS = "reward";
        private final String REWARDS_CHOICE = "rewardchoice";
        private final String REWARDS_REPUTATION = "reputationrewards";
        private final String REWARDS_COMMAND = "commandrewards";

        @Override
        public void write(JsonWriter out, Quest value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            if (!value.getDescription().equals("Unnamed quest"))
                out.name(DESCRIPTION).value(value.getDescription());
            out.name(X).value(value.getGuiX());
            out.name(Y).value(value.getGuiY());
            if (value.useBigIcon()) {
                out.name(BIG_ICON).value(true);
            }
            if (value.getIcon() != null) {
                MinecraftAdapter.ITEM_STACK.write(out.name(ICON), value.getIcon());
            }
            writeQuestList(out, value.getRequirement(), value.getQuestSet(), PREREQUISITES);
            writeQuestList(out, value.getOptionLinks(), value.getQuestSet(), OPTIONLINKS);
            if (value.getRepeatInfo().getType() != RepeatType.NONE) {
                REPEAT_INFO_ADAPTER.write(out.name(REPEAT), value.getRepeatInfo());
            }
            if (value.getTriggerType() != TriggerType.NONE) {
                out.name(TRIGGER).value(value.getTriggerType().name());
            }
            if (value.getTriggerType().isUseTaskCount()) {
                out.name(TRIGGER_TASKS).value(value.getTriggerTasks());
            }
            if (value.getUseModifiedParentRequirement()) {
                out.name(PARENT_REQUIREMENT).value(value.getParentRequirementCount());
            }
            if (!value.getTasks().isEmpty()) {
                out.name(TASKS).beginArray();
                for (QuestTask task : value.getTasks()) {
                    TASK_ADAPTER.write(out, task);
                }
                out.endArray();
            }

            writeItemStackArray(out, value.getReward(), REWARDS);
            writeItemStackArray(out, value.getRewardChoice(), REWARDS_CHOICE);
            writeStringArray(out, value.getCommandRewardsAsStrings(), REWARDS_COMMAND);

            if (value.getReputationRewards() != null && !value.getReputationRewards().isEmpty()) {
                out.name(REWARDS_REPUTATION).beginArray();
                for (ReputationReward reward : value.getReputationRewards()) {
                    REPUTATION_REWARD_ADAPTER.write(out, reward);
                }
                out.endArray();
            }
            out.endObject();
        }

        private String getQuestSaveString(Quest quest, QuestSet set) {
            if (quest.getQuestSet() == set)
                return quest.getName();
            else
                return "{" + quest.getQuestSet().getName() + "}[" + quest.getName() + "]";
        }

        private void writeQuestList(JsonWriter out, List<Quest> quests, QuestSet set, String name) throws IOException {
            if (!quests.isEmpty()) {
                out.name(name).beginArray();
                for (Quest quest : quests) {
                    out.value(getQuestSaveString(quest, set));
                }
                out.endArray();
            }
        }

        private void writeItemStackArray(JsonWriter out, ItemStack[] stacks, String name) throws IOException {
            if (stacks != null) {
                out.name(name).beginArray();
                for (ItemStack stack : stacks) {
                    if (stack != null) {
                        MinecraftAdapter.ITEM_STACK.write(out, stack);
                    }
                }
                out.endArray();
            }
        }

        private void writeStringArray(JsonWriter out, String[] list, String name) throws IOException {
            if(list != null) {
                out.name(name).beginArray();
                for (String s : list) {
                    if (s != null) {
                        out.value(s);
                    }
                }
                out.endArray();
            }
        }

        @Override
        public Quest read(JsonReader in) throws IOException {
            int ID_OFFSET = Quest.size();
            QUEST = new Quest(ID_OFFSET, "", "Unnamed quest", 0, 0, false);
            List<Integer> requirement = new ArrayList<>(), options = new ArrayList<>();
            List<String> prerequisites = new ArrayList<>(), optionLinks = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case NAME:
                        QUEST.setName(in.nextString());
                        break;
                    case DESCRIPTION:
                        QUEST.setDescription(in.nextString());
                        break;
                    case X:
                        QUEST.setX(in.nextInt());
                        break;
                    case Y:
                        QUEST.setY(in.nextInt());
                        break;
                    case TRIGGER_TASKS:
                        QUEST.setTriggerTasks(in.nextInt());
                        break;
                    case PARENT_REQUIREMENT:
                        QUEST.setParentRequirementCount(in.nextInt());
                        break;
                    case BIG_ICON:
                        QUEST.setBigIcon(in.nextBoolean());
                        break;
                    case ICON:
                        QUEST.setIcon(MinecraftAdapter.ITEM_STACK.read(in));
                        break;
                    case REQUIREMENTS:
                        readIntArray(requirement, in);
                        break;
                    case OPTIONS:
                        readIntArray(options, in);
                        break;
                    case PREREQUISITES:
                        readStringArray(prerequisites, in);
                        break;
                    case OPTIONLINKS:
                        readStringArray(optionLinks, in);
                        break;
                    case REPEAT:
                        QUEST.setRepeatInfo(REPEAT_INFO_ADAPTER.read(in));
                        break;
                    case TRIGGER:
                        QUEST.setTriggerType(TriggerType.valueOf(in.nextString()));
                        break;
                    case TASKS:
                        in.beginArray();
                        while (in.hasNext()) {
                            QuestTask task = TASK_ADAPTER.read(in);
                            if (task != null) {
                                QUEST.getTasks().add(task);

                            }
                        }
                        in.endArray();
                        break;
                    case REWARDS:
                        QUEST.setReward(readItemStackArray(in));
                        break;
                    case REWARDS_CHOICE:
                        QUEST.setRewardChoice(readItemStackArray(in));
                        break;
                    case REWARDS_REPUTATION:
                        in.beginArray();
                        List<ReputationReward> reputationRewards = new ArrayList<>();
                        while (in.hasNext()) {
                            ReputationReward reward = REPUTATION_REWARD_ADAPTER.read(in);
                            if (reward != null)
                                reputationRewards.add(reward);
                        }
                        QUEST.setReputationRewards(reputationRewards);
                        in.endArray();
                        break;
                    case REWARDS_COMMAND:
                            List<String> commands = new ArrayList<>();
                            readStringArray(commands, in);
                            QUEST.setCommandRewards(commands.toArray(new String[commands.size()]));
                        break;
                    default:
                        QuestLine.getActiveQuestLine().quests.remove(QUEST.getId());
                        return null;
                }
            }
            in.endObject();
            if (!QUEST.getName().isEmpty()) {
                optionalAdd(requirementMapping, requirement);
                optionalAdd(optionMapping, options);
                optionalAdd(prerequisiteMapping, prerequisites);
                optionalAdd(optionLinkMapping, optionLinks);
                try {
                    if (HardcoreQuesting.getPlayer() != null) {
                        QUEST.addTaskData(QUEST.getQuestData(HardcoreQuesting.getPlayer()));
                    } else {
                        QUEST.addTaskData(QUEST.getQuestData("lorddusk"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return QUEST;
            }
            QuestLine.getActiveQuestLine().quests.remove(QUEST.getId());
            return null;
        }

        private void optionalAdd(Map map, List list) {
            if (!list.isEmpty()) {
                map.put(QUEST, list);
            }
        }

        private void readIntArray(List<Integer> list, JsonReader in) throws IOException {
            in.beginArray();
            while (in.hasNext()) {
                list.add(in.nextInt() + QUEST_ID);
            }
            in.endArray();
        }

        private void readStringArray(List<String> list, JsonReader in) throws IOException {
            in.beginArray();
            while (in.hasNext()) {
                list.add(in.nextString());
            }
            in.endArray();
        }

        private ItemStack[] readItemStackArray(JsonReader in) throws IOException {
            List<ItemStack> stacks = new ArrayList<>();
            in.beginArray();
            while (in.hasNext()) {
                ItemStack stack = MinecraftAdapter.ITEM_STACK.read(in);
                if (stack != null)
                    stacks.add(stack);
            }
            in.endArray();
            return stacks.toArray(new ItemStack[stacks.size()]);
        }
    };

    public static final TypeAdapter<QuestSet> QUEST_SET_ADAPTER = new TypeAdapter<QuestSet>() {
        private final String NAME = "name";
        private final String DESCRIPTION = "description";
        private final String QUESTS = "quests";
        private final String REPUTATION_BAR = "reputationBar";

        @Override
        public void write(JsonWriter out, QuestSet value) throws IOException {
            reputationBarList.clear();
            out.beginObject();
            out.name(NAME).value(value.getName());
            if (!value.getDescription().equalsIgnoreCase("No description"))
                out.name(DESCRIPTION).value(value.getDescription());
            out.name(QUESTS).beginArray();
            for (Quest quest : value.getQuests()) {
                QUEST_ADAPTER.write(out, quest);
            }
            out.endArray();
            out.name(REPUTATION_BAR).beginArray();
            for (ReputationBar reputationBar : value.getReputationBars()) {
                REPUTATION_BAR_ADAPTER.write(out, reputationBar);
            }
            out.endArray().endObject();
        }

        @Override
        public QuestSet read(JsonReader in) throws IOException {
            String name = null, description = "No description";
            requirementMapping.clear();
            optionMapping.clear();
            optionLinkMapping.clear();
            prerequisiteMapping.clear();
            reputationBarList.clear();
            taskReputationListMap.clear();
            List<Quest> quests = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()) {
                String next = in.nextName();
                if (next.equalsIgnoreCase(NAME)) {
                    name = in.nextString();
                } else if (next.equalsIgnoreCase(DESCRIPTION)) {
                    description = in.nextString();
                } else if (next.equalsIgnoreCase(QUESTS)) {
                    in.beginArray();
                    QUEST_ID = Quest.size();
                    while (in.hasNext()) {
                        Quest quest = QUEST_ADAPTER.read(in);
                        if (quest != null) {
                            quests.add(quest);
                        }
                    }
                    in.endArray();
                } else if (next.equalsIgnoreCase(REPUTATION_BAR)) {
                    in.beginArray();
                    while (in.hasNext()) {
                        reputationBarList.add(REPUTATION_BAR_ADAPTER.read(in));
                    }
                    in.endArray();
                }
            }
            in.endObject();
            QuestSet set = null;
            for (QuestSet existing : Quest.getQuestSets()) {
                if (existing.getName().equals(name)) {
                    set = existing;
                    set.setDescription(description);
                    questReplace(set.getQuests(), quests);
                    break;
                }
            }
            if (name != null && description != null && set == null) {
                set = new QuestSet(name, description);
                Quest.getQuestSets().add(set);
                SaveHelper.add(SaveHelper.EditType.SET_CREATE);
            }
            if (set != null) {
                for (Quest quest : quests) {
                    quest.setQuestSet(set);
                }
                for (Map.Entry<Quest, List<Integer>> entry : requirementMapping.entrySet()) {
                    for (int i : entry.getValue())
                        entry.getKey().addRequirement(i);
                }
                for (Map.Entry<Quest, List<Integer>> entry : optionMapping.entrySet()) {
                    for (int i : entry.getValue())
                        entry.getKey().addOptionLink(i);
                }
                for (ReputationBar reputationBar : reputationBarList) {
                    for (ReputationBar r : new ArrayList<>(set.getReputationBars())) {
                        if (r.sameLocation(reputationBar))
                            set.removeRepBar(r);
                    }
                    set.addRepBar(reputationBar);
                }
                return set;
            }
            return removeQuestsRaw(quests);
        }

        private QuestSet removeQuestsRaw(List<Quest> quests) {
            for (Quest quest : quests) {
                QuestLine.getActiveQuestLine().quests.remove(quest.getId());
            }
            return null;
        }

        private void questReplace(List<Quest> existing, List<Quest> replacements) {
            for (Quest current : new ArrayList<>(existing)) {
                Quest replacement = getReplacement(current, replacements);

                for (Quest requirement : current.getRequirement()) {
                    neatSwap(current, replacement, requirement.getReversedRequirement());
                }
                for (Quest dependent : current.getReversedRequirement()) {
                    neatSwap(current, replacement, dependent.getRequirement());
                }
                for (Quest optionLink : current.getOptionLinks()) {
                    neatSwap(current, replacement, optionLink.getReversedOptionLinks());
                }
                for (Quest optionLink : current.getReversedOptionLinks()) {
                    neatSwap(current, replacement, optionLink.getOptionLinks());
                }

                for (QuestTask task : current.getTasks()) {
                    task.onDelete();
                }
                current.setQuestSet(null);
                QuestLine.getActiveQuestLine().quests.remove(current.getId());
            }
        }

        private void neatSwap(Quest current, Quest replacement, List<Quest> replaceIn) {
            replaceIn.remove(current);
            if (replacement != null) replaceIn.add(replacement);
        }

        private Quest getReplacement(Quest quest, List<Quest> replacements) {
            for (Quest replacement : replacements) {
                if (quest.getName().equalsIgnoreCase(replacement.getName())) {
                    return replacement;
                }
            }
            return null;
        }
    };

    private static final TypeAdapter<ReputationBar> REPUTATION_BAR_ADAPTER = new TypeAdapter<ReputationBar>() {
        private final String X = "x";
        private final String Y = "y";
        private final String REPUTATION_ID = "reputationId";

        @Override
        public void write(JsonWriter out, ReputationBar value) throws IOException {
            out.beginObject();
            out.name(REPUTATION_ID).value(value.getRepId());
            out.name(X).value(value.getX());
            out.name(Y).value(value.getY());
            out.endObject();
        }

        @Override
        public ReputationBar read(JsonReader in) throws IOException {
            int id, x, y;
            id = x = y = -1;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case X:
                        x = in.nextInt();
                        break;
                    case Y:
                        y = in.nextInt();
                        break;
                    case REPUTATION_ID:
                        id = in.nextInt();
                        break;
                }
            }
            in.endObject();
            if (id * x * y < 0) return null;
            return new ReputationBar(id, x, y, -1);
        }
    };

    private static final Pattern OTHER_QUEST_SET = Pattern.compile("^\\{(.*?)\\}\\[(.*)\\]$");

    public static void postLoad() throws IOException {
        for (Map.Entry<Quest, List<String>> entry : prerequisiteMapping.entrySet()) {
            for (String link : entry.getValue()) {
                Quest quest = getQuest(link, entry.getKey().getQuestSet());
                if (quest != null)
                    entry.getKey().addRequirement(quest.getId());
            }
        }
        for (Map.Entry<Quest, List<String>> entry : optionLinkMapping.entrySet()) {
            for (String link : entry.getValue()) {
                Quest quest = getQuest(link, entry.getKey().getQuestSet());
                if (quest != null)
                    entry.getKey().addOptionLink(quest.getId());
            }
        }
        for (Map.Entry<ReputationReward, Integer> entry : reputationRewardMapping.entrySet()) {
            int rep = entry.getValue();
            if (rep >= 0 && rep < Reputation.getReputationList().size()) {
                Reputation reputation = Reputation.getReputationList().get(rep);
                if (reputation == null) {
                    throw new IOException("Failed to load reputation value " + rep);
                }
                entry.getKey().setReward(reputation);
            } else {
                throw new IOException("Missing reputation value " + rep);
            }
        }
        for (Map.Entry<QuestTaskReputation, List<ReputationSettingConstructor>> entry : taskReputationListMap.entrySet()) {
            List<QuestTaskReputation.ReputationSetting> reputationSettingList = new ArrayList<>();
            for (ReputationSettingConstructor constructor : entry.getValue()) {
                QuestTaskReputation.ReputationSetting setting = constructor.constructReuptationSetting();
                if (setting != null) {
                    reputationSettingList.add(setting);
                }
            }
            ReflectionHelper.setPrivateValue(QuestTaskReputation.class, entry.getKey(), reputationSettingList.toArray(new QuestTaskReputation.ReputationSetting[reputationSettingList.size()]), "settings");
        }
    }

    private static Quest getQuest(String string, QuestSet defaultSet) {
        QuestSet set = defaultSet;
        String questName = string;
        Matcher matcher = OTHER_QUEST_SET.matcher(string);
        if (matcher.find()) {
            for (QuestSet questSet : Quest.getQuestSets()) {
                if (questSet.getName().equalsIgnoreCase(matcher.group(1))) {
                    set = questSet;
                    break;
                }
            }
            questName = matcher.group(2);
        }
        if (set != null) {
            for (Quest quest : set.getQuests()) {
                if (quest.getName().equalsIgnoreCase(questName)) {
                    return quest;
                }
            }
        }
        return null;
    }
}
