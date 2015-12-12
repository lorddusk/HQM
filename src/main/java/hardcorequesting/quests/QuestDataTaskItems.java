package hardcorequesting.quests;


public class QuestDataTaskItems extends QuestDataTask {
    public int[] progress;

    public QuestDataTaskItems(QuestTask task) {
        super(task);
        this.progress = new int[((QuestTaskItems) task).items.length];
    }
}
