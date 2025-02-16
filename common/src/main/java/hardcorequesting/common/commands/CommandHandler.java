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

import java.util.Set;

import static net.minecraft.commands.Commands.literal;


public class CommandHandler {
    public static final Set<SubCommand> SUB_COMMANDS = Set.of(
            new HelpSubCommand(),
            new HardcoreSubCommand(),
            new LivesSubCommand(),
            new OpSubCommand(),
            new EditSubCommand(),
            new QuestSubCommand(),
            new EnableSubCommand(),
            new VersionSubCommand(),
            new ResetPlayerSubCommand());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal("hqm");
        for (SubCommand command : SUB_COMMANDS) {
            builder = builder.then(command.build(literal(command.name())));
        }
        dispatcher.register(builder);
    }
    
    public interface SubCommand {
        String name();

        ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder);
        
        default int[] getSyntaxOptions(CommandContext<CommandSourceStack> context) {
            return new int[0];
        }
    }

    public static final class Utils {
        public static void currentLives(Player player) {
            player.createCommandSourceStack().sendSuccess(() -> Component.literal("You currently have " + QuestingDataManager.getInstance().getQuestingData(player).getLives() + " live(s) left."), false);
        }

        public static void currentLives(CommandSourceStack source, Player player) {
            source.sendSuccess(() -> Component.literal(player.getScoreboardName() + " currently has " + QuestingDataManager.getInstance().getQuestingData(player).getLives() + " live(s) left."), false);
        }

        public static void sendChat(CommandSourceStack sender, Component text) {
            sender.sendSuccess(() -> text, false);
        }
    }
}
