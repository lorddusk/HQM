package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.Translator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static net.minecraft.commands.Commands.literal;

public class LivesSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                                context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                                return 1;
                                            }
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                                addLivesTo(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                            }
                                            return 1;
                                        }))
                                .executes(context -> {
                                    if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                        context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                        return 1;
                                    }
                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                        addLivesTo(context.getSource(), player, 1);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            addLivesTo(context.getSource(), context.getSource().getPlayerOrException(), 1);
                            return 1;
                        })
                )
                .then(literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                                context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                                return 1;
                                            }
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                                removeLivesFrom(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                            }
                                            return 1;
                                        }))
                                .executes(context -> {
                                    if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                        context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                        return 1;
                                    }
                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                        removeLivesFrom(context.getSource(), player, 1);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            removeLivesFrom(context.getSource(), context.getSource().getPlayerOrException(), 1);
                            return 1;
                        })
                )
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> {
                            if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                                context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets"))
                                currentLives(context.getSource(), player);
                            return 1;
                        }))
                .executes(context -> {
                    if (!QuestingDataManager.getInstance().isHardcoreActive()) {
                        context.getSource().sendFailure(Component.translatable("hqm.message.noHardcoreYet"));
                        return 1;
                    }
                    currentLives(context.getSource().getPlayerOrException());
                    return 1;
                });
    }
    
    @Override
    public int[] getSyntaxOptions(CommandContext<CommandSourceStack> context) {
        return new int[]{0, 1, 2, 3};
    }
    
    private void removeLivesFrom(CommandSourceStack source, Player player, int amount) {
        QuestingDataManager.getInstance().getQuestingData(player).removeLives(player, amount);
        sendChat(source, Translator.translatable("hqm.message.removeLivesFrom", Translator.lives(amount), player.getScoreboardName()));
        if (source.getEntity() != player)
            sendChat(player.createCommandSourceStack(), Translator.translatable("hqm.message.removeLivesBy", Translator.lives(amount), source.getTextName()));
        currentLives(player);
    }
    
    private void addLivesTo(CommandSourceStack source, Player player, int amount) {
        QuestingDataManager questingDataManager = QuestingDataManager.getInstance();
        if (questingDataManager.getQuestingData(player).getRawLives() + amount <= HQMConfig.getInstance().Hardcore.MAX_LIVES) {
            questingDataManager.getQuestingData(player).addLives(player, amount);
            sendChat(source, Translator.translatable("hqm.message.addLivesTo", Translator.lives(amount), player.getScoreboardName()));
            if (source.getEntity() != player)
                sendChat(player.createCommandSourceStack(), Translator.translatable("hqm.message.addLivesBy", Translator.lives(amount), source.getTextName()));
        } else {
            questingDataManager.getQuestingData(player).addLives(player, amount);
            sendChat(source, Translator.translatable("hqm.message.cantGiveMoreLives", player.getScoreboardName(), HQMConfig.getInstance().Hardcore.MAX_LIVES));
            sendChat(source, Translator.translatable("hqm.massage.setLivesInstead", player.getScoreboardName(), HQMConfig.getInstance().Hardcore.MAX_LIVES));
            if (source.getEntity() != player)
                sendChat(player.createCommandSourceStack(), Translator.translatable("hqm.massage.setLivesBy", HQMConfig.getInstance().Hardcore.MAX_LIVES, source.getTextName()));
        }
        currentLives(player);
    }
}
