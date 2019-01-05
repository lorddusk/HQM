package hardcorequesting.quests.data;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskAdvancement;

  import java.io.IOException;

  public class QuestDataTaskAdvancement extends QuestDataTask {

      private static final String COUNT = "count";
     private static final String ADVANCED = "advanced";
     public boolean[] advanced;

      public QuestDataTaskAdvancement(QuestTask task) {
         super(task);
         this.advanced = new boolean[((QuestTaskAdvancement) task).advancements.length];
     }

      protected QuestDataTaskAdvancement() {
         super();
         this.advanced = new boolean[0];
     }

      public static QuestDataTask construct(JsonReader in) {
         QuestDataTaskAdvancement taskData = new QuestDataTaskAdvancement();
         try {
             int count = 0;
             while (in.hasNext()) {
                 switch (in.nextName()) {
                     case QuestDataTask.COMPLETED:
                         taskData.completed = in.nextBoolean();
                         break;
                     case COUNT:
                         count = in.nextInt();
                         taskData.advanced = new boolean[count];
                         break;
                     case ADVANCED:
                         in.beginArray();
                         for (int i = 0; i < count; i++)
                             taskData.advanced[i] = in.nextBoolean();
                         in.endArray();
                         break;
                     default:
                         break;
                 }
             }
         } catch (IOException ignored) {
         }
         return taskData;
     }

      @Override
     public QuestTaskAdapter.QuestDataType getDataType() {
         return QuestTaskAdapter.QuestDataType.ADVANCEMENT;
     }

      @Override
     public void write(JsonWriter out) throws IOException {
         super.write(out);
         out.name(COUNT).value(advanced.length);
         out.name(ADVANCED).beginArray();
         for (boolean i : advanced)
             out.value(i);
         out.endArray();
     }

      @Override
     public void update(QuestDataTask taskData) {
         super.update(taskData);
         this.advanced = ((QuestDataTaskAdvancement) taskData).advanced;
     }
 }

