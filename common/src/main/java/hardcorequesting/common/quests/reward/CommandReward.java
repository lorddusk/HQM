package hardcorequesting.common.quests.reward;

import net.minecraft.world.entity.player.Player;

public class CommandReward extends QuestReward<CommandReward.Command> {
    
    private static final int PERMISSION_LEVEL = 2;
    
    public CommandReward(Command command) {
        super(command);
    }
    
    public static class Command {
        private final String commandString;
        
        public Command(String commandString) {
            this.commandString = commandString;
        }
        
        public void execute(Player player) {
            player.getServer().getCommands().performCommand(player.createCommandSourceStack().withPermission(PERMISSION_LEVEL), commandString);
        }
        
        public String asString() {
            return this.commandString;
        }
    }
}
