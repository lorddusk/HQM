package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.event.WorldEventListener;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class ResetPlayerSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("yes-this-is-really-what-i-want")
                                .executes(context -> run(context, EntityArgument.getPlayers(context, "targets")))));
    }

    private int run(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players){
        for (Player player : players) {
            if (!QuestingDataManager.getInstance().hasData(player)) {
                sendChat(context.getSource(), Component.literal("No data available for " + player.getDisplayName() + " (" + player.getName() + ")."));
            } else {
                QuestingDataManager.getInstance().remove(player);
            }
        }
        WorldEventListener.onSave(context.getSource().getLevel());
        sendChat(context.getSource(), Component.literal("Deletion done."));
        return Command.SINGLE_SUCCESS;
    }
}
