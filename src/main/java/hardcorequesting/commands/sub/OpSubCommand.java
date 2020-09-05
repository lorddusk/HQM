package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.items.QuestBookItem;
import hardcorequesting.quests.QuestingData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class OpSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4) && source.getEntity() instanceof Player)
                .then(Commands.argument("targets", EntityArgument.player())
                        .executes(context -> {
                            Player player = EntityArgument.getPlayer(context, "targets");
                            if (QuestingData.hasData(player)) {
                                player.inventory.add(QuestBookItem.getOPBook(player));
                            } else context.getSource().sendFailure(new TranslatableComponent("hqm.message.noPlayer"));
                            return 1;
                        }))
                .executes(context -> {
                    Player player = (Player) context.getSource().getEntity();
                    if (QuestingData.hasData(player)) {
                        player.inventory.add(QuestBookItem.getOPBook(player));
                    } else context.getSource().sendFailure(new TranslatableComponent("hqm.message.noPlayer"));
                    return 1;
                });
    }
}
