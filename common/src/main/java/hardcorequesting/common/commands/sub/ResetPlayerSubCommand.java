package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.event.WorldEventListener;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.PlayerDataSyncMessage;
import hardcorequesting.common.quests.QuestLine;
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

    private int run(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        int successes = 0;
        for (ServerPlayer player : players) {
            if (!QuestingDataManager.getInstance().hasData(player)) {
                context.getSource().sendFailure(Component.translatable("hqm.message.resetFail", player.getDisplayName()));
            } else {
                QuestingDataManager.getInstance().remove(player);
                NetworkManager.sendToPlayer(new PlayerDataSyncMessage(QuestLine.getActiveQuestLine(), player), player);
                logSuccess(context.getSource(), player);
                successes++;
            }
        }
        WorldEventListener.onSave(context.getSource().getLevel());
        return successes;
    }
    
    private static void logSuccess(CommandSourceStack source, Player player) {
        source.sendSuccess(() -> Component.translatable("hqm.message.resetPlayer", player.getDisplayName()), true);
        player.sendSystemMessage(Component.translatable("hqm.message.reset"));
    }
}
