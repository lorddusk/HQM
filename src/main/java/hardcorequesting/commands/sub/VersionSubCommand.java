package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.util.Translator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

public class VersionSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .executes(context -> {
                    sendChat(context.getSource(), Translator.translatable("hqm.message.version", FabricLoader.getInstance().getModContainer(HardcoreQuesting.ID).get().getMetadata().getVersion().getFriendlyString()).withStyle(ChatFormatting.GREEN));
                    return 1;
                });
    }
}
