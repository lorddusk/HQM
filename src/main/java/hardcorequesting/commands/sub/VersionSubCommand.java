package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.util.Translator;
import net.minecraft.server.command.ServerCommandSource;

public class VersionSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .executes(context -> {
                    sendChat(context.getSource(), "\u00A7a" + Translator.translate("hqm.message.version", HardcoreQuesting.VERSION));
                    return 1;
                });
    }
}
