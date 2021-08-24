package hardcorequesting.common.quests.reward;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

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
            CommandSourceStack sourceStack = new CommandSourceStack(new WrapperCommandSource(player), player.position(), player.getRotationVector(),
                    player.level instanceof ServerLevel ? (ServerLevel)player.level : null, PERMISSION_LEVEL, player.getName().getString(), player.getDisplayName(), player.level.getServer(), player);
            player.getServer().getCommands().performCommand(sourceStack, commandString);
        }
        
        public String asString() {
            return this.commandString;
        }
    }
    
    private static class WrapperCommandSource implements CommandSource {
        private final Player player;
    
        private WrapperCommandSource(Player player) {
            this.player = player;
        }
    
        @Override
        public void sendMessage(Component component, UUID uuid) {
        player.sendMessage(component, uuid);
        }
    
        @Override
        public boolean acceptsSuccess() {
            return false;
        }
    
        @Override
        public boolean acceptsFailure() {
            return true;
        }
    
        @Override
        public boolean shouldInformAdmins() {
            return true;
        }
    }
}