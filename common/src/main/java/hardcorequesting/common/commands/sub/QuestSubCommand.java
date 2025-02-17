package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.Translator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static hardcorequesting.common.commands.CommandHandler.Utils.*;

public record QuestSubCommand() implements CommandHandler.SubCommand {
    @Override
    public String name() {
        return "quest";
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> {
                    String key = QuestingDataManager.getInstance().isQuestActive() ? "hqm.message.questAlreadyActivated" : "hqm.message.questActivated";
                    sendChat(context.getSource(), Translator.translatable(key));
                    QuestingDataManager.getInstance().activateQuest(true);
                    return 1;
                });
    }
}
