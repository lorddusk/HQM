package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.items.QuestBookItem;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class OpSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4) && source.getEntity() instanceof Player)
                .then(Commands.argument("targets", EntityArgument.player())
                        .executes(context -> {
                            Player player = EntityArgument.getPlayer(context, "targets");
                            if (QuestingDataManager.getInstance().hasData(player)) {
                                player.getInventory().add(QuestBookItem.getOPBook(player));
                            } else context.getSource().sendFailure(Component.translatable("hqm.message.noPlayer"));
                            return 1;
                        }))
                .executes(context -> {
                    Player player = (Player) context.getSource().getEntity();
                    if (QuestingDataManager.getInstance().hasData(player)) {
                        player.getInventory().add(QuestBookItem.getOPBook(player));
                    } else context.getSource().sendFailure(Component.translatable("hqm.message.noPlayer"));
                    return 1;
                });
    }
}
