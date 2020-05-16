package hardcorequesting.quests.reward;

import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class CommandRewardList extends QuestRewardList<CommandReward.Command> {
    
    public void executeAll(PlayerEntity player) {
        for (QuestReward<CommandReward.Command> reward : list)
            reward.getReward().execute(player);
    }
    
    public void set(String[] commands) {
        clear();
        if (commands != null)
            for (String command : commands)
                add(new CommandReward(new CommandReward.Command(command)));
    }
    
    public String[] asStrings() {
        List<String> commands = new ArrayList<>();
        for (QuestReward<CommandReward.Command> reward : list)
            commands.add(reward.getReward().asString());
        return commands.toArray(new String[0]);
    }
    
    public void add(String command) {
        list.add(new CommandReward(new CommandReward.Command(command)));
    }
    
    public void set(int id, String command) {
        list.set(id, new CommandReward(new CommandReward.Command(command)));
    }
}
