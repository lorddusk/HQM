package hardcorequesting.commands.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.quests.QuestingData;
import net.minecraft.command.CommandException;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.literal;

public class LivesSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("add")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            if (!QuestingData.isHardcoreActive()) {
                                                context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                                return 1;
                                            }
                                            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                                addLivesTo(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                            }
                                            return 1;
                                        }))
                                .executes(context -> {
                                    if (!QuestingData.isHardcoreActive()) {
                                        context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                        return 1;
                                    }
                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                        addLivesTo(context.getSource(), player, 1);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            if (!QuestingData.isHardcoreActive()) {
                                context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            if (context.getSource().getEntity() instanceof PlayerEntity)
                                addLivesTo(context.getSource(), (PlayerEntity) context.getSource().getEntity(), 1);
                            return 1;
                        })
                )
                .then(literal("remove")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            if (!QuestingData.isHardcoreActive()) {
                                                context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                                return 1;
                                            }
                                            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                                removeLivesFrom(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                            }
                                            return 1;
                                        }))
                                .executes(context -> {
                                    if (!QuestingData.isHardcoreActive()) {
                                        context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                        return 1;
                                    }
                                    for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                        removeLivesFrom(context.getSource(), player, 1);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            if (!QuestingData.isHardcoreActive()) {
                                context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            if (context.getSource().getEntity() instanceof PlayerEntity)
                                removeLivesFrom(context.getSource(), (PlayerEntity) context.getSource().getEntity(), 1);
                            return 1;
                        })
                )
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .executes(context -> {
                            if (!QuestingData.isHardcoreActive()) {
                                context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            currentLives(context.getSource(), EntityArgumentType.getPlayer(context, "targets"));
                            return 1;
                        }))
                .executes(context -> {
                    if (!QuestingData.isHardcoreActive()) {
                        context.getSource().sendError(new TranslatableText("hqm.message.noHardcoreYet"));
                        return 1;
                    }
                    if (context.getSource().getEntity() instanceof PlayerEntity)
                        currentLives((PlayerEntity) context.getSource().getEntity());
                    return 1;
                });
    }
    
    @Override
    public int[] getSyntaxOptions(CommandContext<ServerCommandSource> context) {
        return new int[]{0, 1, 2, 3};
    }
    
    private void removeLivesFrom(ServerCommandSource source, PlayerEntity player, int amount) {
        QuestingData.getQuestingData(player).removeLives(player, amount);
        sendTranslatableChat(source, amount != 1, "hqm.message.removeLivesFrom", amount, player.getEntityName());
        if (source.getEntity() != player)
            sendTranslatableChat(player.getCommandSource(), amount != 1, "hqm.message.removeLivesBy", amount, source.getName());
        currentLives(player);
    }
    
    private void addLivesTo(ServerCommandSource source, PlayerEntity player, int amount) {
        if (QuestingData.getQuestingData(player).getRawLives() + amount <= HQMConfig.getInstance().Hardcore.MAX_LIVES) {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendTranslatableChat(source, amount != 1, "hqm.message.addLivesTo", amount, player.getEntityName());
            if (source.getEntity() != player)
                sendTranslatableChat(player.getCommandSource(), amount != 1, "hqm.message.addLivesBy", amount, source.getName());
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendTranslatableChat(source, "hqm.message.cantGiveMoreLives", player.getEntityName(), HQMConfig.getInstance().Hardcore.MAX_LIVES);
            sendTranslatableChat(source, "hqm.massage.setLivesInstead", player.getEntityName(), HQMConfig.getInstance().Hardcore.MAX_LIVES);
            if (source.getEntity() != player)
                sendTranslatableChat(player.getCommandSource(), "hqm.massage.setLivesBy", HQMConfig.getInstance().Hardcore.MAX_LIVES, source.getName());
            currentLives(player);
        }
    }
    
    private void getPlayerLives(ServerCommandSource source, String playerName) throws CommandException {
        PlayerEntity player = HardcoreQuesting.getServer().getPlayerManager().getPlayer(playerName);
        if (player != null) {
            int lives = QuestingData.getQuestingData(player).getLives();
            sendTranslatableChat(source, lives != 1, "hqm.message.hasLivesRemaining", playerName, lives);
        } else {
            throw new CommandException(new TranslatableText("hqm.message.noPlayer"));
        }
    }
}
