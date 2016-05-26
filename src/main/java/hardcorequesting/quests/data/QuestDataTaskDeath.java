package hardcorequesting.quests.data;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;

import java.io.IOException;

public class QuestDataTaskDeath extends QuestDataTask {
    public int deaths;
    private static final String DEATHS = "deaths";

    public QuestDataTaskDeath(QuestTask task) {
        super(task);
    }

    protected QuestDataTaskDeath()
    {
        super();
    }

    @Override
    public QuestTaskAdapter.QuestDataType getDataType()
    {
        return QuestTaskAdapter.QuestDataType.DEATH;
    }

    public static QuestDataTask construct(JsonReader in)
    {
        QuestDataTaskDeath taskData = new QuestDataTaskDeath();
        try
        {
            taskData.completed = in.nextBoolean();
            taskData.deaths = in.nextInt();
            return taskData;
        } catch (IOException ignored) {}
        return taskData;
    }

    @Override
    public void write(JsonWriter out) throws IOException
    {
        super.write(out);
        out.name(DEATHS).value(deaths);
    }

    @Override
    public void update(QuestDataTask taskData)
    {
        super.update(taskData);
        this.deaths = ((QuestDataTaskDeath)taskData).deaths;
    }
}
