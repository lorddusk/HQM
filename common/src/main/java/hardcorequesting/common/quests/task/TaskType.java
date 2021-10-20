package hardcorequesting.common.quests.task;

import com.google.common.collect.ImmutableMap;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import hardcorequesting.common.quests.task.item.*;
import hardcorequesting.common.quests.task.reputation.HaveReputationTask;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

import java.util.Collection;
import java.util.Map;

public final class TaskType<T extends QuestTask<?>> {
    public static final TaskType<ConsumeItemTask> CONSUME = new TaskType<>("consume", ConsumeItemTask::new);
    public static final TaskType<CraftingTask> CRAFT = new TaskType<>("craft", CraftingTask::new);
    public static final TaskType<VisitLocationTask> LOCATION = new TaskType<>("location", VisitLocationTask::new);
    public static final TaskType<ConsumeItemQDSTask> CONSUME_QDS = new TaskType<>("consumeQDS", ConsumeItemQDSTask::new);
    public static final TaskType<DetectItemTask> DETECT = new TaskType<>("detect", DetectItemTask::new);
    public static final TaskType<KillMobsTask> KILL = new TaskType<>("kill", KillMobsTask::new);
    public static final TaskType<TameMobsTask> TAME = new TaskType<>("tame", TameMobsTask::new);
    public static final TaskType<DeathTask> DEATH = new TaskType<>("death", DeathTask::new);
    public static final TaskType<HaveReputationTask> REPUTATION = new TaskType<>("reputation", HaveReputationTask::new);
    public static final TaskType<KillReputationTask> REPUTATION_KILL = new TaskType<>("reputationKill", KillReputationTask::new);
    public static final TaskType<GetAdvancementTask> ADVANCEMENT = new TaskType<>("advancement", GetAdvancementTask::new);
    public static final TaskType<CompleteQuestTask> COMPLETION = new TaskType<>("completion", CompleteQuestTask::new);
    public static final TaskType<BreakBlockTask> BLOCK_BREAK = new TaskType<>("break", BreakBlockTask::new);
    public static final TaskType<PlaceBlockTask> BLOCK_PLACE = new TaskType<>("place", PlaceBlockTask::new);
    
    private static final Map<String, TaskType<?>> TYPES = ImmutableMap.<String, TaskType<?>>builder().put("CONSUME", CONSUME).put("CRAFT", CRAFT)
            .put("LOCATION", LOCATION).put("CONSUME_QDS", CONSUME_QDS).put("DETECT", DETECT).put("KILL", KILL).put("TAME", TAME).put("DEATH", DEATH)
            .put("REPUTATION", REPUTATION).put("REPUTATION_KILL", REPUTATION_KILL).put("ADVANCEMENT", ADVANCEMENT).put("COMPLETION", COMPLETION)
            .put("BLOCK_BREAK", BLOCK_BREAK).put("BLOCK_PLACE", BLOCK_PLACE).build();
    
    private final String id;
    private final TaskConstructor<T> constructor;
    
    private TaskType(String id, TaskConstructor<T> constructor) {
        this.id = id;
        this.constructor = constructor;
    }
    
    public static Collection<TaskType<?>> values() {
        return TYPES.values();
    }
    
    public String toDataName() {
        for (Map.Entry<String, TaskType<?>> entry : TYPES.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }
        throw new IllegalStateException(id + " is not registered as a task type");
    }
    
    public static TaskType<?> fromDataName(String str) {
        TaskType<?> type = TYPES.get(str);
        if (type != null)
            return type;
        else throw new IllegalArgumentException(str + " is not a valid task type name");
    }
    
    public T addTask(Quest quest) {
        T task = constructor.create(quest);
        task.updateId(quest.getTasks().size());
        quest.getTasks().add(task);
        SaveHelper.add(EditType.TASK_CREATE);
        return task;
    }
    
    public String getLangKeyDescription() {
        return "hqm.taskType." + id + ".desc";
    }
    
    public String getLangKeyName() {
        return "hqm.taskType." + id + ".title";
    }
    
    public FormattedText getDescription() {
        return Translator.translatable(getLangKeyDescription());
    }
    
    public FormattedText getName() {
        return Translator.translatable(getLangKeyName());
    }
    
    public interface TaskConstructor<T extends QuestTask<?>> {
        T create(Quest quest);
    }
}
