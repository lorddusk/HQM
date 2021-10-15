package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.Translator;
import net.minecraft.commands.CommandSourceStack;

public class QuestSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    String key = QuestingDataManager.getInstance().isQuestActive() ? "hqm.message.questAlreadyActivated" : "hqm.message.questActivated";
                    sendChat(context.getSource(), Translator.translatable(key));
                    QuestingDataManager.getInstance().activateQuest(true);
                    return 1;
                });
    }
}
