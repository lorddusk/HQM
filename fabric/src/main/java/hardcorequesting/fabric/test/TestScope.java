package hardcorequesting.fabric.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.bag.LootGroup;
import hardcorequesting.common.io.adapter.QuestAdapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.task.QuestTask;
import net.minecraft.gametest.framework.GameTestHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Runs a test body in isolation from the global state of the mod, and puts everything back afterwards.
 * <p>
 * The mod keeps its state in statics: the active {@link QuestLine}, the tasks registered for events, the loot groups
 * and the edit mode flag. Without this scope a test would leave its quests and tasks behind for the other tests.
 * Inside the body, {@link #newLine()} replaces the active quest line with an empty one.
 */
public final class TestScope {
    private final GameTestHelper helper;
    private final QuestLine previousLine = QuestLine.getActiveQuestLine();
    private final Map<UUID, LootGroup> previousLootGroups = new HashMap<>(LootGroup.getGroups());
    private final boolean previousEditing = Quest.isEditing;
    private final List<QuestLine> createdLines = new ArrayList<>();

    private TestScope(GameTestHelper helper) {
        this.helper = helper;
        clearAdapterState();
    }

    public interface Body {
        void run(TestScope scope) throws Exception;
    }

    /**
     * Runs the body and reports the result to the helper. Failed checks and any exception fail the test.
     */
    public static void run(GameTestHelper helper, Body body) {
        TestScope scope = new TestScope(helper);
        String failure = null;
        try {
            body.run(scope);
        } catch (AssertionError e) {
            failure = e.getMessage();
        } catch (Throwable e) {
            failure = "Unexpected " + e;
            e.printStackTrace();
        } finally {
            scope.close();
        }
        if (failure != null)
            helper.fail(failure);
        else helper.succeed();
    }

    public GameTestHelper helper() {
        return helper;
    }

    /**
     * Replaces the active quest line with a new empty one. It is dropped when the scope ends.
     */
    public QuestLine newLine() {
        QuestLine line = QuestLine.reset();
        createdLines.add(line);
        return line;
    }

    /**
     * Creates a new quest line and loads the given book files into it.
     */
    public QuestLine loadBook(MemoryData book) {
        QuestLine line = newLine();
        line.loadAll(book, new MemoryData());
        return line;
    }

    /**
     * Creates a new quest line and loads the given book and world files into it.
     */
    public QuestLine load(MemoryData book, MemoryData world) {
        QuestLine line = newLine();
        line.loadAll(book, world);
        return line;
    }

    public static MemoryData saveBook(QuestLine line) {
        MemoryData data = new MemoryData();
        line.saveQuests(data);
        return data;
    }

    public static MemoryData saveWorld(QuestLine line) {
        MemoryData data = new MemoryData();
        line.saveData(data);
        return data;
    }

    private void close() {
        for (QuestLine line : createdLines) {
            // The tasks are registered for events globally, so they have to be unregistered one by one
            line.questSetsManager.quests.values().forEach(quest -> quest.getTasks().forEach(QuestTask::onDelete));
        }
        setActiveQuestLine(previousLine);
        clearAdapterState();
        LootGroup.getGroups().clear();
        LootGroup.getGroups().putAll(previousLootGroups);
        Quest.isEditing = previousEditing;
    }

    /**
     * The adapters collect the links between quests, and reputations of rewards and tasks, in static maps while a book is
     * read, and resolve them when it is done. When that fails (e.g. a missing reputation), the maps keep their entries and
     * break the next book that is loaded.
     */
    private static void clearAdapterState() {
        try {
            for (Field field : QuestAdapter.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    ((Map<?, ?>) field.get(null)).clear();
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not reset the quest adapter", e);
        }
        QuestTaskAdapter.taskReputationListMap.clear();
    }
    
    private static void setActiveQuestLine(QuestLine line) {
        try {
            Field field = QuestLine.class.getDeclaredField("activeQuestLine");
            field.setAccessible(true);
            field.set(null, line);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not restore the active quest line", e);
        }
    }

    // Checks, they fail the running test with the message

    public static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
    }

    public static void equal(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual))
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
    }

    /**
     * Checks that everything in {@code expected} is also in {@code actual}. Objects may have more keys in {@code actual},
     * arrays have to have the same length.
     */
    public static void contains(JsonElement expected, JsonElement actual, String path) {
        if (expected.isJsonObject()) {
            if (!actual.isJsonObject())
                throw new AssertionError(path + ": expected an object but was " + actual);
            JsonObject actualObject = actual.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : expected.getAsJsonObject().entrySet()) {
                if (!actualObject.has(entry.getKey()))
                    throw new AssertionError(path + "." + entry.getKey() + " is missing, expected " + entry.getValue());
                contains(entry.getValue(), actualObject.get(entry.getKey()), path + "." + entry.getKey());
            }
        } else if (expected.isJsonArray()) {
            if (!actual.isJsonArray() || actual.getAsJsonArray().size() != expected.getAsJsonArray().size())
                throw new AssertionError(path + ": expected " + expected + " but was " + actual);
            for (int i = 0; i < expected.getAsJsonArray().size(); i++)
                contains(expected.getAsJsonArray().get(i), actual.getAsJsonArray().get(i), path + "[" + i + "]");
        } else if (!expected.equals(actual)) {
            throw new AssertionError(path + ": expected " + expected + " but was " + actual);
        }
    }
}
