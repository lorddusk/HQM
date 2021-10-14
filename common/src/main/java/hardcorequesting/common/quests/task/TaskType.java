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
    public static final TaskType<ConsumeItemTask> CONSUME = new TaskType<>(ConsumeItemTask.class, "consume", ConsumeItemTask::new);
    public static final TaskType<CraftingTask> CRAFT = new TaskType<>(CraftingTask.class, "craft", CraftingTask::new);
    public static final TaskType<VisitLocationTask> LOCATION = new TaskType<>(VisitLocationTask.class, "location", VisitLocationTask::new);
    public static final TaskType<ConsumeItemQDSTask> CONSUME_QDS = new TaskType<>(ConsumeItemQDSTask.class, "consumeQDS", ConsumeItemQDSTask::new);
    public static final TaskType<DetectItemTask> DETECT = new TaskType<>(DetectItemTask.class, "detect", DetectItemTask::new);
    public static final TaskType<KillMobsTask> KILL = new TaskType<>(KillMobsTask.class, "kill", KillMobsTask::new);
    public static final TaskType<TameMobsTask> TAME = new TaskType<>(TameMobsTask.class, "tame", TameMobsTask::new);
    public static final TaskType<DeathTask> DEATH = new TaskType<>(DeathTask.class, "death", DeathTask::new);
    public static final TaskType<HaveReputationTask> REPUTATION = new TaskType<>(HaveReputationTask.class, "reputation", HaveReputationTask::new);
    public static final TaskType<KillReputationTask> REPUTATION_KILL = new TaskType<>(KillReputationTask.class, "reputationKill", KillReputationTask::new);
    public static final TaskType<GetAdvancementTask> ADVANCEMENT = new TaskType<>(GetAdvancementTask.class, "advancement", GetAdvancementTask::new);
    public static final TaskType<CompleteQuestTask> COMPLETION = new TaskType<>(CompleteQuestTask.class, "completion", CompleteQuestTask::new);
    public static final TaskType<BreakBlockTask> BLOCK_BREAK = new TaskType<>(BreakBlockTask.class, "break", BreakBlockTask::new);
    public static final TaskType<PlaceBlockTask> BLOCK_PLACE = new TaskType<>(PlaceBlockTask.class, "place", PlaceBlockTask::new);
    
    private static final Map<String, TaskType<?>> TYPES = ImmutableMap.<String, TaskType<?>>builder().put("consume", CONSUME).put("craft", CRAFT)
            .put("location", LOCATION).put("consume_qds", CONSUME_QDS).put("detect", DETECT).put("kill", KILL).put("tame", TAME).put("death", DEATH)
            .put("reputation", REPUTATION).put("reputation_kill", REPUTATION_KILL).put("advancement", ADVANCEMENT).put("completion", COMPLETION)
            .put("block_break", BLOCK_BREAK).put("block_place", BLOCK_PLACE).build();
    
    private final Class<T> clazz;
    private final String id;
    private final TaskConstructor<T> constructor;
    
    private TaskType(Class<T> clazz, String id, TaskConstructor<T> constructor) {
        this.clazz = clazz;
        this.id = id;
        this.constructor = constructor;
    }
    
    public static Collection<TaskType<?>> values() {
        return TYPES.values();
    }
    
    public static TaskType<?> getType(Class<?> clazz) {
        for (TaskType<?> type : values()) {
            if (type.clazz == clazz) return type;
        }
        throw new IllegalArgumentException(clazz.getName() + " does not have a valid task type");
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
        else throw new IllegalArgumentException(str + "is not a valid task type name");
    }
    
    public T addTask(Quest quest) {
        T task = constructor.create(quest, getLangKeyName(), getLangKeyDescription());
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
        T create(Quest quest, String name, String description);
    }
}
