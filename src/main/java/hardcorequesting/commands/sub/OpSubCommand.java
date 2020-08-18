package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.items.QuestBookItem;
import hardcorequesting.quests.QuestingData;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public class OpSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(source -> source.hasPermissionLevel(4) && source.getEntity() instanceof PlayerEntity)
                .then(CommandManager.argument("targets", EntityArgumentType.player())
                        .executes(context -> {
                            PlayerEntity player = EntityArgumentType.getPlayer(context, "targets");
                            if (QuestingData.hasData(player)) {
                                player.inventory.insertStack(QuestBookItem.getOPBook(player));
                            } else context.getSource().sendError(new TranslatableText("hqm.message.noPlayer"));
                            return 1;
                        }))
                .executes(context -> {
                    PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
                    if (QuestingData.hasData(player)) {
                        player.inventory.insertStack(QuestBookItem.getOPBook(player));
                    } else context.getSource().sendError(new TranslatableText("hqm.message.noPlayer"));
                    return 1;
                });
    }
}
