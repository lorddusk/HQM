package hardcorequesting.quests;


public class QuestDataTaskLocation extends QuestDataTask {
    public boolean[] visited;

    public QuestDataTaskLocation(QuestTask task) {
        super(task);
        this.visited = new boolean[((QuestTaskLocation) task).locations.length];
    }
}
