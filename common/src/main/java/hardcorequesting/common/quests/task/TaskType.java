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

import java.lang.reflect.Constructor;

public enum TaskType {
    CONSUME(ConsumeItemTask.class, "consume"),
    CRAFT(CraftingTask.class, "craft"),
    LOCATION(VisitLocationTask.class, "location"),
    CONSUME_QDS(ConsumeItemQDSTask.class, "consumeQDS"),
    DETECT(DetectItemTask.class, "detect"),
    KILL(KillMobsTask.class, "kill"),
    TAME(TameMobsTask.class, "tame"),
    DEATH(DeathTask.class, "death"),
    REPUTATION(HaveReputationTask.class, "reputation"),
    REPUTATION_KILL(KillReputationTask.class, "reputationKill"),
    ADVANCEMENT(GetAdvancementTask.class, "advancement"),
    COMPLETION(CompleteQuestTask.class, "completion"),
    BLOCK_BREAK(BreakBlockTask.class, "break"),
    BLOCK_PLACE(PlaceBlockTask.class, "place");
    
    private final Class<? extends QuestTask<?>> clazz;
    private final String id;
    
    TaskType(Class<? extends QuestTask<?>> clazz, String id) {
        this.clazz = clazz;
        this.id = id;
    }
    
    public static TaskType getType(Class<?> clazz) {
        for (TaskType type : TaskType.values()) {
            if (type.clazz == clazz) return type;
        }
        return CONSUME;
    }
    
    public QuestTask<?> addTask(Quest quest) {
        try {
            Constructor<? extends QuestTask<?>> ex = clazz.getConstructor(Quest.class, String.class, String.class);
            QuestTask<?> task = ex.newInstance(quest, getName(), getDescription());
            task.updateId(quest.getTasks().size());
            quest.getTasks().add(task);
            SaveHelper.add(EditType.TASK_CREATE);
            return task;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getLangKeyDescription() {
        return "hqm.taskType." + id + ".desc";
    }
    
    public String getLangKeyName() {
        return "hqm.taskType." + id + ".title";
    }
    
    public String getDescription() {
        return Translator.get(getLangKeyDescription());
    }
    
    public String getName() {
        return Translator.get(getLangKeyName());
    }
}
