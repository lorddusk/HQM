package hardcorequesting.quests.reward;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandReward extends QuestReward<CommandReward.Command> {

    public static class Command {

        private String commandString;

        public Command(String commandString) {
            this.commandString = commandString;
        }

        public void execute(EntityPlayer player) {
            MinecraftServer server = MinecraftServer.getServer();
            server.getCommandManager().executeCommand(server, commandString.replaceAll("@p", player.getCommandSenderName()));
        }

        public String asString() {
            return this.commandString;
        }
    }

    public CommandReward(Command command) {
        super(command);
    }

    public void execute(EntityPlayer player) {
        getReward().execute(player);
    }
}
