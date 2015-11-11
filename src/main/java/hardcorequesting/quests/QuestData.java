package hardcorequesting.quests;

import java.io.Serializable;

import hardcorequesting.QuestingData;
import hardcorequesting.Team;
import net.minecraft.entity.player.EntityPlayer;

public class QuestData implements Serializable {
	public boolean[] reward;
	public boolean completed;
	public boolean claimed;
	public QuestDataTask[] tasks;
	public boolean available = true;
	public int time;

	public QuestData(boolean[] reward, boolean completed, boolean claimed, QuestDataTask[] tasks, boolean available,
			int time) {
		super();
		this.reward = reward;
		this.completed = completed;
		this.claimed = claimed;
		this.tasks = tasks;
		this.available = available;
		this.time = time;
	}

	public QuestData(int players) {
		reward = new boolean[players];
	}
	
	@Override
	public String toString(){
		return "Completed: "+ completed +"  claimed: "+claimed;
	}

	public boolean getReward(EntityPlayer player) {
		int id = getId(player);
		if (id >= 0 && id < reward.length) {
			return reward[id];
		} else {
			return true;
		}
	}

	public void claimReward(EntityPlayer player) {
		int id = getId(player);
		if (id >= 0 && id < reward.length) {
			reward[id] = false;
		}
	}

	private int getId(EntityPlayer player) {
		Team team = QuestingData.getQuestingData(player).getTeam();
		int id = 0;
		for (Team.PlayerEntry entry : team.getPlayers()) {
			if (entry.isInTeam()) {
				if (entry.getName().equals(QuestingData.getUserName(player))) {
					return id;
				}
				id++;
			}
		}

		return -1;
	}

	public boolean canClaim() {
		return completed && !claimed;
	}

	/**
	 * @return the reward
	 */
	public boolean[] getReward() {
		return reward;
	}

	/**
	 * @param reward
	 *            the reward to set
	 */
	public void setReward(boolean[] reward) {
		this.reward = reward;
	}

	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @param completed
	 *            the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	/**
	 * @return the claimed
	 */
	public boolean isClaimed() {
		return claimed;
	}

	/**
	 * @param claimed
	 *            the claimed to set
	 */
	public void setClaimed(boolean claimed) {
		this.claimed = claimed;
	}

	/**
	 * @return the tasks
	 */
	public QuestDataTask[] getTasks() {
		return tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void setTasks(QuestDataTask[] tasks) {
		this.tasks = tasks;
	}

	/**
	 * @return the available
	 */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * @param available
	 *            the available to set
	 */
	public void setAvailable(boolean available) {
		this.available = available;
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @param time
	 *            the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}
}
