package hardcorequesting.common.quests.reward;

import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

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
    
    public FormattedText getLabel() {
        if (reward != null) {
            MutableComponent component = reward.getName().append(": ");
            
            MutableComponent valueComp = Translator.text(value > 0 ? "+" + value : String.valueOf(value));
            if (value != 0) {
                valueComp.withStyle(value > 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED);
            }
            
            return component.append(valueComp);
        }
        return FormattedText.EMPTY;
    }
}
