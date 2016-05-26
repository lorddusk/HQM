package hardcorequesting.quests.reward;

public abstract class QuestReward<T> {

    protected T reward;

    public QuestReward(T reward)
    {
        this.reward = reward;
    }

    public T getReward()
    {
        return reward;
    }

    public void setReward(T reward)
    {
        this.reward = reward;
    }
}
