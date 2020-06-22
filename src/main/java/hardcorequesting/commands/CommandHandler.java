package hardcorequesting.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.commands.sub.*;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

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
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("hqm");
        for (String s : SUB_COMMANDS.keySet()) {
            builder = builder.then(SUB_COMMANDS.get(s).build(literal(s)));
        }
        dispatcher.register(builder.executes(context -> {
            return 1;
        }));
    }
    
    public interface SubCommand {
        ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder);
        
        default int[] getSyntaxOptions(CommandContext<ServerCommandSource> context) {
            return new int[0];
        }
        
        default void currentLives(PlayerEntity player) {
            player.getCommandSource().sendFeedback(new LiteralText("You currently have " + QuestingData.getQuestingData(player).getLives() + " live(s) left."), false);
        }
        
        default void currentLives(ServerCommandSource source, PlayerEntity player) {
            source.sendFeedback(new LiteralText(player.getEntityName() + " currently has " + QuestingData.getQuestingData(player).getLives() + " live(s) left."), false);
        }
    
        default void sendChat(ServerCommandSource sender, Text text) {
            sender.sendFeedback(text, false);
        }
        
        default void sendTranslatableChat(ServerCommandSource sender, String key, Object... args) {
            sender.sendFeedback(Translator.translatable(key, args), false);
        }
        
        default void sendTranslatableChat(ServerCommandSource sender, boolean plural, String key, Object... args) {
            sender.sendFeedback(Translator.translatable(Formatting.RESET, plural, key, args), false);
        }
    }
}
