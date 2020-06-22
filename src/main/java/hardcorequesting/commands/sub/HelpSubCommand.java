package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.commands.CommandStrings;
import hardcorequesting.util.Translator;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class HelpSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        for (String s : CommandHandler.SUB_COMMANDS.keySet()) {
            CommandHandler.SubCommand command = CommandHandler.SUB_COMMANDS.get(s);
            builder = builder.then(literal(s).executes(context -> {
                for (int i : command.getSyntaxOptions(context))
                    context.getSource().sendFeedback(Translator.translatable(CommandStrings.COMMAND_PREFIX + s + CommandStrings.SYNTAX_SUFFIX + i).formatted(Formatting.YELLOW)
                            .append(new LiteralText(" - ")).append(Translator.translatable(CommandStrings.COMMAND_PREFIX + s + CommandStrings.INFO_SUFFIX + i)), false);
                return 1;
            }));
        }
        return builder.executes(context -> {
            MutableText output = new LiteralText("");
            output = output.append(Translator.translatable(CommandStrings.HELP_START));
            output = output.append(" ");
            List<String> commands = new ArrayList<>(CommandHandler.SUB_COMMANDS.keySet());
            
            for (int i = 0; i < commands.size() - 1; i++) {
                output = output.append("/").append("hqm").append(" ").append(new LiteralText(commands.get(i)).formatted(Formatting.YELLOW));
                if (i != commands.size() - 2) {
                    output = output.append(", ");
                }
            }
            output = output.append(" and /").append("hqm").append(" ").append(new LiteralText(commands.get(commands.size() - 1)).formatted(Formatting.YELLOW)).append(".");
            context.getSource().sendFeedback(output, false);
            return 1;
        });
    }
}
