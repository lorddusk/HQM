package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.commands.CommandStrings;
import hardcorequesting.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class HelpSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (String s : CommandHandler.SUB_COMMANDS.keySet()) {
            CommandHandler.SubCommand command = CommandHandler.SUB_COMMANDS.get(s);
            builder = builder.then(literal(s).executes(context -> {
                for (int i : command.getSyntaxOptions(context))
                    context.getSource().sendSuccess(Translator.translatable(CommandStrings.COMMAND_PREFIX + s + CommandStrings.SYNTAX_SUFFIX + i).withStyle(ChatFormatting.YELLOW)
                            .append(new TextComponent(" - ")).append(Translator.translatable(CommandStrings.COMMAND_PREFIX + s + CommandStrings.INFO_SUFFIX + i)), false);
                return 1;
            }));
        }
        return builder.executes(context -> {
            MutableComponent output = new TextComponent("");
            output = output.append(Translator.translatable(CommandStrings.HELP_START));
            output = output.append(" ");
            List<String> commands = new ArrayList<>(CommandHandler.SUB_COMMANDS.keySet());
            
            for (int i = 0; i < commands.size() - 1; i++) {
                output = output.append("/").append("hqm").append(" ").append(new TextComponent(commands.get(i)).withStyle(ChatFormatting.YELLOW));
                if (i != commands.size() - 2) {
                    output = output.append(", ");
                }
            }
            output = output.append(" and /").append("hqm").append(" ").append(new TextComponent(commands.get(commands.size() - 1)).withStyle(ChatFormatting.YELLOW)).append(".");
            context.getSource().sendSuccess(output, false);
            return 1;
        });
    }
}
