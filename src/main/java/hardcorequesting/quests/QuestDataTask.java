package hardcorequesting.quests;

import java.io.Serializable;

public class QuestDataTask implements Serializable {
	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	public QuestDataTask(boolean completed) {
		super();
		this.completed = completed;
	}

	/**
	 * @param completed
	 *            the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean completed;

	public QuestDataTask(QuestTask task) {
	}
}
