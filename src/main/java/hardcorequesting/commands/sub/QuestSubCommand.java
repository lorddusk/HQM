package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.quests.QuestingData;
import net.minecraft.server.command.ServerCommandSource;

public class QuestSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    sendTranslatableChat(context.getSource(), QuestingData.isQuestActive() ? "hqm.message.questAlreadyActivated" : "hqm.message.questActivated");
                    QuestingData.activateQuest(true);
                    return 1;
                });
    }
}
