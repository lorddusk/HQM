package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.commands.CommandStrings;
import hardcorequesting.util.Translator;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
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
                    context.getSource().sendFeedback(new TranslatableText(Formatting.YELLOW + Translator.translate(CommandStrings.COMMAND_PREFIX + s + CommandStrings.SYNTAX_SUFFIX + i)
                                                                          + Formatting.WHITE + " - " + Translator.translate(CommandStrings.COMMAND_PREFIX + s + CommandStrings.INFO_SUFFIX + i)), false);
                return 1;
            }));
        }
        return builder.executes(context -> {
            StringBuilder output = new StringBuilder(Translator.translate(CommandStrings.HELP_START) + " ");
            List<String> commands = new ArrayList<>(CommandHandler.SUB_COMMANDS.keySet());
            
            for (int i = 0; i < commands.size() - 1; i++) {
                output.append("/").append("hqm").append(" ").append(Formatting.YELLOW).append(commands.get(i)).append(Formatting.WHITE).append(", ");
            }
            output.delete(output.length() - 2, output.length());
            output.append(" and /").append("hqm").append(" ").append(Formatting.YELLOW).append(commands.get(commands.size() - 1)).append(Formatting.WHITE).append(".");
            context.getSource().sendFeedback(new LiteralText(output.toString()), false);
            return 1;
        });
    }
}
