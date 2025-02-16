package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

import static hardcorequesting.common.commands.CommandHandler.Utils.*;

public class VersionSubCommand implements CommandHandler.SubCommand {
    @Override
    public String name() {
        return "version";
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .executes(context -> {
                    sendChat(context.getSource(), Translator.translatable("hqm.message.version", HardcoreQuestingCore.platform.getModVersion()).withStyle(ChatFormatting.GREEN));
                    return 1;
                });
    }
}
