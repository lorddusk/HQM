package hardcorequesting.quests.reward;

import net.minecraft.entity.player.PlayerEntity;

public class CommandReward extends QuestReward<CommandReward.Command> {
    
    public CommandReward(Command command) {
        super(command);
    }
    
    public void execute(PlayerEntity player) {
        getReward().execute(player);
    }
    
    public static class Command {
        private String commandString;
        
        public Command(String commandString) {
            this.commandString = commandString;
        }
        
        public void execute(PlayerEntity player) {
            player.getServer().getCommandManager().execute(player.getCommandSource(), commandString.replaceAll("@p", player.getEntityName()));
        }
        
        public String asString() {
            return this.commandString;
        }
    }
}
