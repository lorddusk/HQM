package hardcorequesting.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.quests.QuestingData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.literal;

public class HardcoreSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        Command<ServerCommandSource> enable = context -> {
            QuestingData.disableVanillaHardcore(context.getSource());
            if (context.getSource().getWorld().getLevelProperties().isHardcore())
                context.getSource().sendFeedback(new TranslatableText("hqm.message.vanillaHardcoreOn"), true);
            else
                context.getSource().sendFeedback(new TranslatableText(QuestingData.isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore"), true);
            QuestingData.activateHardcore();
            if (context.getSource().getEntity() instanceof PlayerEntity)
                currentLives((PlayerEntity) context.getSource().getEntity());
            return 1;
        };
        return builder
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("enable").executes(enable))
                .then(literal("disable")
                        .executes(context -> {
                            QuestingData.disableHardcore();
                            context.getSource().sendFeedback(new TranslatableText("hqm.message.hardcoreDisabled"), true);
                            return 1;
                        })
                )
                .executes(enable);
    }
}
