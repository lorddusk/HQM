package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static hardcorequesting.common.commands.CommandHandler.Utils.*;
import static net.minecraft.commands.Commands.literal;

public class HardcoreSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        Command<CommandSourceStack> enable = context -> {
            if (context.getSource().getLevel().getLevelData().isHardcore())
                context.getSource().sendSuccess(() -> Component.translatable("hqm.message.vanillaHardcoreOn"), true);
            else
                context.getSource().sendSuccess(() -> Component.translatable(QuestingDataManager.getInstance().isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore"), true);
            QuestingDataManager.getInstance().activateHardcore();

            ServerPlayer player = context.getSource().getPlayer();
            if (player != null)
                currentLives(player);
            return 1;
        };
        return builder
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("enable").executes(enable))
                .then(literal("disable")
                        .executes(context -> {
                            QuestingDataManager.getInstance().disableHardcore();
                            context.getSource().sendSuccess(() -> Component.translatable("hqm.message.hardcoreDisabled"), true);
                            return 1;
                        })
                )
                .executes(enable);
    }
}
