package hardcorequesting.common.io.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.quests.*;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskReputation;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuestAdapter {
    
    private static final Adapter<RepeatInfo> REPEAT_INFO_ADAPTER = new Adapter<RepeatInfo>() {
        private static final String TYPE = "type";
        private static final String HOURS = "hours";
        private static final String DAYS = "days";
        
        @Override
        public JsonElement serialize(RepeatInfo src) {
            JsonObjectBuilder object = object()
                    .add(TYPE, src.getType().name());
            if (src.getType().isUseTime()) {
                object.add(DAYS, src.getDays());
                object.add(HOURS, src.getHours());
            }
            return object.build();
        }
        
        @Override
        public RepeatInfo deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            
            return new RepeatInfo(
                    RepeatType.valueOf(GsonHelper.getAsString(object, TYPE)),
                    GsonHelper.getAsInt(object, HOURS, 0),
                    GsonHelper.getAsInt(object, DAYS, 0)
            );
        }
    };
    private static final Adapter<ReputationBar> REPUTATION_BAR_ADAPTER = new Adapter<ReputationBar>() {
        private static final String X = "x";
        private static final String Y = "y";
        private static final String REPUTATION_ID = "reputationId";
        
        @Override
        public JsonElement serialize(ReputationBar src) {
            return object()
                    .add(REPUTATION_ID, src.getRepId())
                    .add(X, src.getX())
                    .add(Y, src.getY())
                    .build();
        }
        
        @Override
        public ReputationBar deserialize(JsonElement json) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            return new ReputationBar(
                    GsonHelper.getAsString(object, REPUTATION_ID),
                    GsonHelper.getAsInt(object, X),
                    GsonHelper.getAsInt(object, Y),
                    -1
            );
        }
    };
    private static final JsonArray EMPTY_ARRAY = new JsonArray();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();
    private static final Pattern OTHER_QUEST_SET = Pattern.compile("^\\{(.*?)\\}\\[(.*)\\]$");
    public static Quest QUEST;
    private static Map<String, Quest> nameToQuestMap = new HashMap<>();
    private static List<ReputationBar> reputationBarList = new ArrayList<>();
    private static Map<Quest, List<UUID>> requirementMapping = new HashMap<>();
    private static Map<Quest, List<UUID>> prerequisiteMapping = new HashMap<>();
    private static Map<Quest, List<UUID>> optionMapping = new HashMap<>();
    private static Map<Quest, List<UUID>> optionLinkMapping = new HashMap<>();
    private static Map<ReputationReward, String> reputationRewardMapping = new HashMap<>();
    private static final Adapter<ReputationReward> REPUTATION_REWARD_ADAPTER = new Adapter<ReputationReward>() {
        private static final String REPUTATION = "reputation";
        private static final String VALUE = "value";
        
        @Override
        public JsonElement serialize(ReputationReward src) {
            return object()
                    .add(REPUTATION, src.getReward().getId())
                    .add(VALUE, src.getValue())
                    .build();
        }
        
        @Override
        public ReputationReward deserialize(JsonElement json) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            
            ReputationReward result = new ReputationReward(null, GsonHelper.getAsInt(object, VALUE));
            reputationRewardMapping.put(result, GsonHelper.getAsString(object, REPUTATION));
            return result;
        }
    };
    public static final Adapter<Quest> QUEST_ADAPTER = new Adapter<Quest>() {
        private static final String UUID = "uuid";
        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String X = "x";
        private static final String Y = "y";
        private static final String ICON = "icon";
        private static final String BIG_ICON = "bigicon";
        private static final String REQUIREMENTS = "requirements";
        private static final String PREREQUISITES = "prerequisites";
        private static final String OPTIONS = "options";
        private static final String OPTIONLINKS = "optionlinks";
        private static final String REPEAT = "repeat";
        private static final String TRIGGER = "trigger";
        private static final String TRIGGER_TASKS = "triggertasks";
        private static final String PARENT_REQUIREMENT = "parentrequirement";
        private static final String TASKS = "tasks";
        private static final String REWARDS = "reward";
        private static final String REWARDS_CHOICE = "rewardchoice";
        private static final String REWARDS_REPUTATION = "reputationrewards";
        private static final String REWARDS_COMMAND = "commandrewards";
        
        private JsonElement writeQuestList(List<Quest> quests) {
            JsonArrayBuilder array = array();
            if (!quests.isEmpty()) {
                for (Quest quest : quests) {
                    array.add(quest.getQuestId().toString());
                }
            }
            return array.build();
        }
        
        private JsonElement writeItemStackArray(ItemStack[] stacks) {
            JsonArrayBuilder array = array();
            if (stacks != null) {
                for (ItemStack stack : stacks) {
                    if (stack != null) {
                        array.add(MinecraftAdapter.ITEM_STACK.serialize(stack));
                    }
                }
            }
            return array.build();
        }
        
        @Override
        public JsonElement serialize(Quest src) {
            return object()
                    .add(UUID, src.getQuestId().toString())
                    .add(NAME, src.getName())
                    .add(DESCRIPTION, src.getDescription())
                    .add(X, src.getGuiX())
                    .add(Y, src.getGuiY())
                    .use(builder -> {
                        if (src.useBigIcon())
                            builder.add(BIG_ICON, true);
                        if (src.getIconStack() != null)
                            builder.add(ICON, MinecraftAdapter.ITEM_STACK.serialize(src.getIconStack()));
                        if (src.getRepeatInfo().getType() != RepeatType.NONE)
                            builder.add(REPEAT, REPEAT_INFO_ADAPTER.serialize(src.getRepeatInfo()));
                        if (src.getTriggerType() != TriggerType.NONE)
                            builder.add(TRIGGER, src.getTriggerType().name());
                        if (src.getTriggerType().isUseTaskCount())
                            builder.add(TRIGGER_TASKS, src.getTriggerTasks());
                        if (src.getUseModifiedParentRequirement())
                            builder.add(PARENT_REQUIREMENT, src.getParentRequirementCount());
                        if (!src.getTasks().isEmpty()) {
                            JsonArrayBuilder array = array();
                            for (QuestTask task : src.getTasks()) {
                                array.add(QuestTaskAdapter.TASK_ADAPTER.serialize(task));
                            }
                            builder.add(TASKS, array.build());
                        }
                        if (src.getReputationRewards() != null && !src.getReputationRewards().isEmpty()) {
                            JsonArrayBuilder array = array();
                            for (ReputationReward reward : src.getReputationRewards()) {
                                array.add(REPUTATION_REWARD_ADAPTER.serialize(reward));
                            }
                            builder.add(REWARDS_REPUTATION, array.build());
                        }
                    })
                    .add(PREREQUISITES, writeQuestList(src.getRequirements()))
                    .add(OPTIONLINKS, writeQuestList(src.getOptionLinks()))
                    .add(REWARDS, writeItemStackArray(src.getReward()))
                    .add(REWARDS_CHOICE, writeItemStackArray(src.getRewardChoice()))
                    .add(REWARDS_COMMAND, SaveHandler.GSON.toJsonTree(src.getCommandRewardsAsStrings()))
                    .build();
        }
        
        @Override
        public Quest deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            List<String> requirement = Collections.emptyList();
            List<String> options = Collections.emptyList();
            List<String> prerequisites = Collections.emptyList();
            List<String> optionLinks = Collections.emptyList();
            QUEST = new Quest(
                    GsonHelper.getAsString(object, NAME, "Unnamed"),
                    GsonHelper.getAsString(object, DESCRIPTION, "Unnamed quest"),
                    GsonHelper.getAsInt(object, X, 0),
                    GsonHelper.getAsInt(object, Y, 0),
                    GsonHelper.getAsBoolean(object, BIG_ICON, false)
            );
            boolean hasUuid = object.has(UUID);
            if (hasUuid)
                QUEST.setId(java.util.UUID.fromString(GsonHelper.getAsString(object, UUID)));
            QUEST.setTriggerTasks(GsonHelper.getAsInt(object, TRIGGER_TASKS, QUEST.getTriggerTasks()));
            QUEST.setParentRequirementCount(GsonHelper.getAsInt(object, PARENT_REQUIREMENT, QUEST.getParentRequirementCount()));
            if (object.has(ICON)) {
                ItemStack icon = MinecraftAdapter.ITEM_STACK.deserialize(object.get(ICON));
                if (!icon.isEmpty()) {
                    QUEST.setIconStack(icon);
                }
            }
            if (object.has(REQUIREMENTS)) requirement = SaveHandler.GSON.fromJson(object.get(REQUIREMENTS), STRING_LIST_TYPE);
            if (object.has(OPTIONS)) options = SaveHandler.GSON.fromJson(object.get(OPTIONS), STRING_LIST_TYPE);
            if (object.has(PREREQUISITES)) prerequisites = SaveHandler.GSON.fromJson(object.get(PREREQUISITES), STRING_LIST_TYPE);
            if (object.has(OPTIONLINKS)) optionLinks = SaveHandler.GSON.fromJson(object.get(OPTIONLINKS), STRING_LIST_TYPE);
            if (object.has(REPEAT)) QUEST.setRepeatInfo(REPEAT_INFO_ADAPTER.deserialize(object.get(REPEAT)));
            if (object.has(TRIGGER)) QUEST.setTriggerType(TriggerType.valueOf(GsonHelper.getAsString(object, TRIGGER)));
            for (JsonElement element : GsonHelper.getAsJsonArray(object, TASKS, EMPTY_ARRAY)) {
                QuestTask task = QuestTaskAdapter.TASK_ADAPTER.deserialize(element);
                if (task != null) {
                    QUEST.getTasks().add(task);
                }
            }
            QUEST.setReward(readItemStackArray(GsonHelper.getAsJsonArray(object, REWARDS, EMPTY_ARRAY)));
            QUEST.setRewardChoice(readItemStackArray(GsonHelper.getAsJsonArray(object, REWARDS_CHOICE, EMPTY_ARRAY)));
            if (object.has(REWARDS_REPUTATION)) {
                List<ReputationReward> reputationRewards = new ArrayList<>();
                for (JsonElement element : GsonHelper.getAsJsonArray(object, REWARDS_REPUTATION, EMPTY_ARRAY)) {
                    ReputationReward reward = REPUTATION_REWARD_ADAPTER.deserialize(element);
                    if (reward != null)
                        reputationRewards.add(reward);
                }
                QUEST.setReputationRewards(reputationRewards);
            }
            if (object.has(REWARDS_COMMAND))
                QUEST.setCommandRewards(SaveHandler.GSON.<List<String>>fromJson(object.get(REWARDS_COMMAND), STRING_LIST_TYPE).toArray(new String[0]));
            if (hasUuid && QUEST.getQuestId() != null) {
                optionalAdd(requirementMapping, requirement.stream().map(java.util.UUID::fromString).collect(Collectors.toList()));
                optionalAdd(optionMapping, options.stream().map(java.util.UUID::fromString).collect(Collectors.toList()));
                optionalAdd(prerequisiteMapping, prerequisites.stream().map(java.util.UUID::fromString).collect(Collectors.toList()));
                optionalAdd(optionLinkMapping, optionLinks.stream().map(java.util.UUID::fromString).collect(Collectors.toList()));
                return QUEST;
            }
            QuestSetsManager.getInstance().quests.remove(QUEST.getQuestId());
            return null;
        }
        
        private <T> void optionalAdd(Map<Quest, List<T>> map, List<T> list) {
            if (!list.isEmpty()) {
                map.put(QUEST, list);
            }
        }
        
        private ItemStack[] readItemStackArray(JsonArray array) {
            List<ItemStack> stacks = new ArrayList<>();
            for (JsonElement element : array) {
                ItemStack stack = MinecraftAdapter.ITEM_STACK.deserialize(element);
                if (!stack.isEmpty())
                    stacks.add(stack);
            }
            return stacks.toArray(new ItemStack[0]);
        }
    };
    public static final Adapter<QuestSet> QUEST_SET_ADAPTER = new Adapter<QuestSet>() {
        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String QUESTS = "quests";
        private static final String REPUTATION_BAR = "reputationBar";
        private static final String REPUTATION_BAR_OLD = "reputation";
        
        private QuestSet removeQuestsRaw(List<Quest> quests) {
            for (Quest quest : quests) {
                QuestSetsManager.getInstance().quests.remove(quest.getQuestId());
            }
            return null;
        }
        
        @Override
        public JsonElement serialize(QuestSet src) {
            reputationBarList.clear();
            return object()
                    .add(NAME, src.getName())
                    .add(QUESTS, array()
                            .use(builder -> {
                                for (Quest quest : src.getQuests().values()) {
                                    builder.add(QUEST_ADAPTER.serialize(quest));
                                }
                            })
                            .build())
                    .add(REPUTATION_BAR, array()
                            .use(builder -> {
                                for (ReputationBar bar : src.getReputationBars()) {
                                    builder.add(REPUTATION_BAR_ADAPTER.serialize(bar));
                                }
                            })
                            .build())
                    .use(builder -> {
                        if (!src.getDescription().equalsIgnoreCase("No description"))
                            builder.add(DESCRIPTION, src.getDescription());
                    })
                    .build();
        }
        
        @Override
        public QuestSet deserialize(JsonElement json) throws JsonParseException {
            requirementMapping.clear();
            optionMapping.clear();
            reputationBarList.clear();
            QuestTaskAdapter.taskReputationListMap.clear();
            List<Quest> quests = new ArrayList<>();
            JsonObject object = json.getAsJsonObject();
            
            String name = GsonHelper.getAsString(object, NAME);
            String description = GsonHelper.getAsString(object, DESCRIPTION, "No description");
            for (JsonElement element : GsonHelper.getAsJsonArray(object, QUESTS)) {
                Quest quest = QUEST_ADAPTER.fromJsonTree(element);
                if (quest != null) {
                    quests.add(quest);
                }
            }
            for (JsonElement element : GsonHelper.getAsJsonArray(object, REPUTATION_BAR)) {
                reputationBarList.add(REPUTATION_BAR_ADAPTER.deserialize(element));
            }
            
            QuestSet set = null;
            for (QuestSet existing : Quest.getQuestSets()) {
                if (existing.getName().equals(name)) {
                    set = existing;
                    set.setDescription(description);
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
                for (Map.Entry<Quest, List<UUID>> entry : requirementMapping.entrySet()) {
                    for (UUID i : entry.getValue())
                        entry.getKey().addRequirement(i);
                }
                for (Map.Entry<Quest, List<UUID>> entry : optionMapping.entrySet()) {
                    for (UUID i : entry.getValue())
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
    };
    
    public static void postLoad() throws IOException {
        for (Map.Entry<Quest, List<UUID>> entry : prerequisiteMapping.entrySet()) {
            for (UUID link : entry.getValue()) {
                entry.getKey().addRequirement(link);
            }
        }
        prerequisiteMapping.clear();
        for (Map.Entry<Quest, List<UUID>> entry : optionLinkMapping.entrySet()) {
            for (UUID link : entry.getValue()) {
                entry.getKey().addOptionLink(link);
            }
        }
        optionLinkMapping.clear();
        for (Map.Entry<ReputationReward, String> entry : reputationRewardMapping.entrySet()) {
            String rep = entry.getValue();
            Reputation reputation = ReputationManager.getInstance().getReputations().get(rep);
            if (reputation == null)
                throw new IOException("Failed to load reputation " + rep);
            entry.getKey().setReward(reputation);
        }
        reputationRewardMapping.clear();
        for (Map.Entry<QuestTaskReputation, List<QuestTaskAdapter.ReputationSettingConstructor>> entry : QuestTaskAdapter.taskReputationListMap.entrySet()) {
            List<QuestTaskReputation.ReputationSetting> reputationSettingList = new ArrayList<>();
            for (QuestTaskAdapter.ReputationSettingConstructor constructor : entry.getValue()) {
                QuestTaskReputation.ReputationSetting setting = constructor.constructReputationSetting();
                if (setting != null) {
                    reputationSettingList.add(setting);
                }
            }
            entry.getKey().settings = reputationSettingList.toArray(new QuestTaskReputation.ReputationSetting[0]);
        }
        QuestTaskAdapter.taskReputationListMap.clear();
        nameToQuestMap.clear();
    }
    
}
