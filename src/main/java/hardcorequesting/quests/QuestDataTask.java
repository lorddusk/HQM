package hardcorequesting.quests;

import java.io.Serializable;

public class QuestDataTask implements Serializable {
    public QuestDataTask(boolean completed) {
		super();
		this.completed = completed;
	}

	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @param completed the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean completed;

    public QuestDataTask(QuestTask task) {}
}
