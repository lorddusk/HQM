package hardcorequesting.reward;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.reputation.Reputation;

public class ReputationReward extends QuestReward<Reputation> {
    private int value;

    public ReputationReward(Reputation reputation, int value) {
        super(reputation);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLabel() {
        String result = reward.getName() + ": ";

        if (value != 0) {
            result += value > 0 ? GuiColor.GREEN : GuiColor.RED;
        }
        if (value > 0) {
            result += "+";
        }
        result += value;

        return result;
    }
}
