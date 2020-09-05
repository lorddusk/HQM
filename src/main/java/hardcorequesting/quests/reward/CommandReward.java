package hardcorequesting.quests.reward;

import net.minecraft.world.entity.player.Player;

public class CommandReward extends QuestReward<CommandReward.Command> {
    
    public CommandReward(Command command) {
        super(command);
    }
    
    public void execute(Player player) {
        getReward().execute(player);
    }
    
    public static class Command {
        private String commandString;
        
        public Command(String commandString) {
            this.commandString = commandString;
        }
        
        public void execute(Player player) {
            player.getServer().getCommands().performCommand(player.createCommandSourceStack(), commandString.replaceAll("@p", player.getScoreboardName()));
        }
        
        public String asString() {
            return this.commandString;
        }
    }
}
