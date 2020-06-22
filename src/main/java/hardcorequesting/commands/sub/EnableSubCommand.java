package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.quests.QuestingData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class EnableSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    if (context.getSource().getWorld().getLevelProperties().isHardcore())
                        context.getSource().sendFeedback(new TranslatableText("hqm.message.vanillaHardcoreOn"), true);
                    else
                        context.getSource().sendFeedback(new TranslatableText(QuestingData.isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore"), true);
                    QuestingData.activateHardcore();
                    QuestingData.activateQuest(true);
                    if (context.getSource().getEntity() instanceof PlayerEntity)
                        currentLives((PlayerEntity) context.getSource().getEntity());
                    return 1;
                });
    }
}
