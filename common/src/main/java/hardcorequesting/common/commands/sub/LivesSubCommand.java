package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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

import static hardcorequesting.common.commands.CommandHandler.Utils.*;
import static net.minecraft.commands.Commands.literal;

public final class LivesSubCommand implements CommandHandler.SubCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_HARDCORE = new SimpleCommandExceptionType(Component.translatable("hqm.message.noHardcoreYet"));

    private static void requireHardmodeIsActive() throws CommandSyntaxException {
        if (!QuestingDataManager.getInstance().isHardcoreActive()) {
            throw ERROR_NOT_HARDCORE.create();
        }
    }

    @Override
    public String name() {
        return "lives";
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(makeAddBuilder())
                .then(makeRemoveBuilder())
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> {
                            requireHardmodeIsActive();
                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets"))
                                currentLives(context.getSource(), player);
                            return 1;
                        }))
                .executes(context -> {
                    requireHardmodeIsActive();
                    currentLives(context.getSource().getPlayerOrException());
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeAddBuilder() {
        return literal("add")
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    requireHardmodeIsActive();
                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                        addLivesTo(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            requireHardmodeIsActive();
                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                addLivesTo(context.getSource(), player, 1);
                            }
                            return 1;
                        }))
                .executes(context -> {
                    requireHardmodeIsActive();
                    addLivesTo(context.getSource(), context.getSource().getPlayerOrException(), 1);
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> makeRemoveBuilder() {
        return literal("remove")
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    requireHardmodeIsActive();
                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                        removeLivesFrom(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            requireHardmodeIsActive();
                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                removeLivesFrom(context.getSource(), player, 1);
                            }
                            return 1;
                        }))
                .executes(context -> {
                    requireHardmodeIsActive();
                    removeLivesFrom(context.getSource(), context.getSource().getPlayerOrException(), 1);
                    return 1;
                });
    }

    @Override
    public void sendHelpMessages(CommandSourceStack source) {
        sendHelpMessagesForCommand(source, this.name(), 0, 1, 2, 3);
    }

    private static void removeLivesFrom(CommandSourceStack source, Player player, int amount) {
        QuestingDataManager.getInstance().getQuestingData(player).removeLives(player, amount);
        sendChat(source, Translator.translatable("hqm.message.removeLivesFrom", Translator.lives(amount), player.getScoreboardName()));
        if (source.getEntity() != player)
            sendChat(player.createCommandSourceStack(), Translator.translatable("hqm.message.removeLivesBy", Translator.lives(amount), source.getTextName()));
        currentLives(player);
    }
    
    private static void addLivesTo(CommandSourceStack source, Player player, int amount) {
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
