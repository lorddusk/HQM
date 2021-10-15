package hardcorequesting.common.quests.task;

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

public enum TaskType {
    CONSUME(ConsumeItemTask.class, "consume", ConsumeItemTask::new),
    CRAFT(CraftingTask.class, "craft", CraftingTask::new),
    LOCATION(VisitLocationTask.class, "location", VisitLocationTask::new),
    CONSUME_QDS(ConsumeItemQDSTask.class, "consumeQDS", ConsumeItemQDSTask::new),
    DETECT(DetectItemTask.class, "detect", DetectItemTask::new),
    KILL(KillMobsTask.class, "kill", KillMobsTask::new),
    TAME(TameMobsTask.class, "tame", TameMobsTask::new),
    DEATH(DeathTask.class, "death", DeathTask::new),
    REPUTATION(HaveReputationTask.class, "reputation", HaveReputationTask::new),
    REPUTATION_KILL(KillReputationTask.class, "reputationKill", KillReputationTask::new),
    ADVANCEMENT(GetAdvancementTask.class, "advancement", GetAdvancementTask::new),
    COMPLETION(CompleteQuestTask.class, "completion", CompleteQuestTask::new),
    BLOCK_BREAK(BreakBlockTask.class, "break", BreakBlockTask::new),
    BLOCK_PLACE(PlaceBlockTask.class, "place", PlaceBlockTask::new);
    
    private final Class<? extends QuestTask<?>> clazz;
    private final String id;
    private final TaskConstructor<?> constructor;
    
    <T extends QuestTask<?>> TaskType(Class<T> clazz, String id, TaskConstructor<T> constructor) {
        this.clazz = clazz;
        this.id = id;
        this.constructor = constructor;
    }
    
    public static TaskType getType(Class<?> clazz) {
        for (TaskType type : TaskType.values()) {
            if (type.clazz == clazz) return type;
        }
        return CONSUME;
    }
    
    public QuestTask<?> addTask(Quest quest) {
        QuestTask<?> task = constructor.create(quest, getLangKeyName(), getLangKeyDescription());
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
