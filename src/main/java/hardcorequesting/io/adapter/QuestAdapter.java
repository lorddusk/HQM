package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.relauncher.ReflectionHelper;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.quests.*;
import hardcorequesting.quests.reward.ReputationReward;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskReputation;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationBar;
import hardcorequesting.util.SaveHelper;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestAdapter {
    public static Quest QUEST;
    private static Map<String, Quest> nameToQuestMap = new HashMap<>();
    private static List<ReputationBar> reputationBarList = new ArrayList<>();
    private static Map<Quest, List<String>> requirementMapping = new HashMap<>();
    private static Map<Quest, List<String>> prerequisiteMapping = new HashMap<>();
    private static Map<Quest, List<String>> optionMapping = new HashMap<>();
    private static Map<Quest, List<String>> optionLinkMapping = new HashMap<>();
    private static Map<ReputationReward, String> reputationRewardMapping = new HashMap<>();

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
            int val = 0;
            String rep = null;
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case REPUTATION:
                        rep = in.nextString();
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
        private final String UUID = "uuid";
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
            out.name(UUID).value(value.getId());
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
            writeQuestList(out, value.getRequirements(), PREREQUISITES);
            writeQuestList(out, value.getOptionLinks(), OPTIONLINKS);
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
                    QuestTaskAdapter.TASK_ADAPTER.write(out, task);
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

        private void writeQuestList(JsonWriter out, List<Quest> quests, String name) throws IOException {
            if (!quests.isEmpty()) {
                out.name(name).beginArray();
                for (Quest quest : quests) {
                    out.value(quest.getId());
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
            if (list != null) {
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
            QUEST = new Quest("Unnamed", "Unnamed quest", 0, 0, false);
            List<String> requirement = new ArrayList<>(), options = new ArrayList<>();
            List<String> prerequisites = new ArrayList<>(), optionLinks = new ArrayList<>();
            boolean hasUuid = false;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case UUID:
                        QUEST.setId(in.nextString());
                        hasUuid = true;
                        break;
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
                        readStringArray(requirement, in);
                        break;
                    case OPTIONS:
                        readStringArray(options, in);
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
                            QuestTask task = QuestTaskAdapter.TASK_ADAPTER.read(in);
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
            if (!QUEST.getId().isEmpty()) {
                if (!hasUuid)
                    nameToQuestMap.put(QUEST.getName(), QUEST);
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

        private <T> void optionalAdd(Map<Quest, List<T>> map, List<T> list) {
            if (!list.isEmpty()) {
                map.put(QUEST, list);
            }
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
        private final String REPUTATION_BAR_OLD = "reputation";

        @Override
        public void write(JsonWriter out, QuestSet value) throws IOException {
            reputationBarList.clear();
            out.beginObject();
            out.name(NAME).value(value.getName());
            if (!value.getDescription().equalsIgnoreCase("No description"))
                out.name(DESCRIPTION).value(value.getDescription());
            out.name(QUESTS).beginArray();
            for (Quest quest : value.getQuests().values()) {
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
            reputationBarList.clear();
            QuestTaskAdapter.taskReputationListMap.clear();
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
                    while (in.hasNext()) {
                        Quest quest = QUEST_ADAPTER.read(in);
                        if (quest != null) {
                            quests.add(quest);
                        }
                    }
                    in.endArray();
                } else if (next.equalsIgnoreCase(REPUTATION_BAR) || next.equalsIgnoreCase(REPUTATION_BAR_OLD)) {
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
                for (Map.Entry<Quest, List<String>> entry : requirementMapping.entrySet()) {
                    for (String i : entry.getValue())
                        entry.getKey().addRequirement(getUuid(i));
                }
                for (Map.Entry<Quest, List<String>> entry : optionMapping.entrySet()) {
                    for (String i : entry.getValue())
                        entry.getKey().addOptionLink(getUuid(i));
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
            String id = null;
            int x, y;
            x = y = -1;
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
                        id = in.nextString();
                        break;
                }
            }
            in.endObject();
            return new ReputationBar(id, x, y, -1);
        }
    };

    public static void postLoad() throws IOException {
        for (Map.Entry<Quest, List<String>> entry : prerequisiteMapping.entrySet()) {
            for (String link : entry.getValue()) {
                Quest quest = getQuest(link);
                if (quest != null)
                    entry.getKey().addRequirement(quest.getId());
            }
        }
        prerequisiteMapping.clear();
        for (Map.Entry<Quest, List<String>> entry : optionLinkMapping.entrySet()) {
            for (String link : entry.getValue()) {
                Quest quest = getQuest(link);
                if (quest != null)
                    entry.getKey().addOptionLink(quest.getId());
            }
        }
        optionLinkMapping.clear();
        for (Map.Entry<ReputationReward, String> entry : reputationRewardMapping.entrySet()) {
            String rep = entry.getValue();
            Reputation reputation = Reputation.getReputations().get(rep);
            if (reputation == null)
                throw new IOException("Failed to load reputation " + rep);
            entry.getKey().setReward(reputation);
        }
        reputationRewardMapping.clear();
        for (Map.Entry<QuestTaskReputation, List<QuestTaskAdapter.ReputationSettingConstructor>> entry : QuestTaskAdapter.taskReputationListMap.entrySet()) {
            List<QuestTaskReputation.ReputationSetting> reputationSettingList = new ArrayList<>();
            for (QuestTaskAdapter.ReputationSettingConstructor constructor : entry.getValue()) {
                QuestTaskReputation.ReputationSetting setting = constructor.constructReuptationSetting();
                if (setting != null) {
                    reputationSettingList.add(setting);
                }
            }
            ReflectionHelper.setPrivateValue(QuestTaskReputation.class, entry.getKey(), reputationSettingList.toArray(new QuestTaskReputation.ReputationSetting[reputationSettingList.size()]), "settings");
        }
        QuestTaskAdapter.taskReputationListMap.clear();
        nameToQuestMap.clear();
    }

    private static final Pattern OTHER_QUEST_SET = Pattern.compile("^\\{(.*?)\\}\\[(.*)\\]$");

    private static Quest getQuest(String questString) {
        String questId = questString;
        Matcher matcher = OTHER_QUEST_SET.matcher(questString);
        if (matcher.find()) {
            questId = matcher.group(2);
        }
        return Quest.getQuest(getUuid(questId));
    }

    private static String getUuid(String id) {
        return nameToQuestMap.containsKey(id) ? nameToQuestMap.get(id).getId() : id;
    }
}
