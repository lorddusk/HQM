package hardcorequesting.common.quests.reward;

import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandRewardList extends QuestRewardList<CommandReward.Command> {
    
    public void executeAll(Player player) {
        for (QuestReward<CommandReward.Command> reward : list)
            reward.getReward().execute(player);
    }
    
    public void set(List<String> commands) {
        clear();
        if (commands != null)
            for (String command : commands)
                add(new CommandReward(new CommandReward.Command(command)));
    }
    
    public List<String> asStrings() {
        List<String> commands = new ArrayList<>();
        for (QuestReward<CommandReward.Command> reward : list)
            commands.add(reward.getReward().asString());
        return commands;
    }
    
    public void add(String command) {
        list.add(new CommandReward(new CommandReward.Command(command)));
    }
    
    public void set(int id, String command) {
        list.set(id, new CommandReward(new CommandReward.Command(command)));
    }
}
