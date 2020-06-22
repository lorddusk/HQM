package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.util.Translator;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class VersionSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .executes(context -> {
                    sendChat(context.getSource(), Translator.translatable("hqm.message.version", HardcoreQuesting.VERSION).formatted(Formatting.GREEN));
                    return 1;
                });
    }
}
