package hardcorequesting.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.common.commands.sub.*;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.commands.Commands.literal;


public class CommandHandler {
    public static final Map<String, SubCommand> SUB_COMMANDS;
    
    static {
        SUB_COMMANDS = new HashMap<>();
        SUB_COMMANDS.put("help", new HelpSubCommand());
        SUB_COMMANDS.put("hardcore", new HardcoreSubCommand());
        SUB_COMMANDS.put("lives", new LivesSubCommand());
        SUB_COMMANDS.put("op", new OpSubCommand());
        SUB_COMMANDS.put("edit", new EditSubCommand());
        SUB_COMMANDS.put("quest", new QuestSubCommand());
        SUB_COMMANDS.put("enable", new EnableSubCommand());
        SUB_COMMANDS.put("version", new VersionSubCommand());
    }
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal("hqm");
        for (String s : SUB_COMMANDS.keySet()) {
            builder = builder.then(SUB_COMMANDS.get(s).build(literal(s)));
        }
        dispatcher.register(builder.executes(context -> {
            return 1;
        }));
    }
    
    public interface SubCommand {
        ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder);
        
        default int[] getSyntaxOptions(CommandContext<CommandSourceStack> context) {
            return new int[0];
        }
        
        default void currentLives(Player player) {
            player.createCommandSourceStack().sendSuccess(Component.literal("You currently have " + QuestingDataManager.getInstance().getQuestingData(player).getLives() + " live(s) left."), false);
        }
        
        default void currentLives(CommandSourceStack source, Player player) {
            source.sendSuccess(Component.literal(player.getScoreboardName() + " currently has " + QuestingDataManager.getInstance().getQuestingData(player).getLives() + " live(s) left."), false);
        }
        
        default void sendChat(CommandSourceStack sender, Component text) {
            sender.sendSuccess(text, false);
        }
    }
}
