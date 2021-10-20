package hardcorequesting.common.client.interfaces.graphic.task;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskGraphics {
    
    private static final Map<TaskType<?>, Constructor<?>> constructors = new HashMap<>();
    
    public static <T extends QuestTask<?>> void register(TaskType<T> type, Constructor<? super T> constructor) {
        constructors.put(type, constructor);
    }
    
    public static <T extends QuestTask<?>> TaskGraphic create(T task, UUID playerId, GuiQuestBook questBook) {
        @SuppressWarnings("unchecked")
        Constructor<? super T> constructor = (Constructor<? super T>) constructors.get(task.getType());
        return constructor.create(task, playerId, questBook);
    }
    
    public interface Constructor<T extends QuestTask<?>> {
        TaskGraphic create(T task, UUID playerId, GuiQuestBook questBook);
    }
}