package hardcorequesting.common.quests.reward;

import hardcorequesting.common.reputation.Reputation;
import net.minecraft.ChatFormatting;

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
    
    public String getLabel() {  //TODO text component
        if (reward != null) {
            String result = reward.getName() + ": ";
            
            if (value != 0) {
                result += value > 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;
            }
            if (value > 0) {
                result += "+";
            }
            result += value;
            return result;
        }
        return "";
    }
}
