package hardcorequesting.quests;


public class QuestDataTaskMob extends QuestDataTask {
    public int[] killed;

    public QuestDataTaskMob(QuestTask task) {
        super(task);
        this.killed = new int[((QuestTaskMob) task).mobs.length];
    }
}
