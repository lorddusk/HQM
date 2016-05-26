package hardcorequesting.quests.reward;

import net.minecraft.entity.player.EntityPlayer;

public class CommandReward extends QuestReward<CommandReward.Command> {

    public static class Command {

        private String commandString;

        public Command(String commandString) {
            this.commandString = commandString;
        }

        public void execute(EntityPlayer player) {
            player.getServer().getCommandManager().executeCommand(player.getServer(), commandString.replaceAll("@p", player.getDisplayNameString()));
        }

        public String asString()
        {
            return this.commandString;
        }
    }

    public CommandReward(Command command) {
        super(command);
    }

    public void execute(EntityPlayer player)
    {
        getReward().execute(player);
    }
}
